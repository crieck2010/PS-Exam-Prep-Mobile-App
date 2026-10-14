package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.entities.QuestionProgressEntity
import com.example.data.local.entities.QuizResultEntity
import com.example.data.local.entities.StudySessionDraftEntity
import com.example.data.local.entities.UserStatsEntity
import com.example.data.network.NetworkMonitor
import com.example.data.sync.SyncManager
import com.example.model.*
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max

class QuizRepository(context: Context, val database: AppDatabase = AppDatabase.getInstance(context)) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ps_exam_prep_prefs", Context.MODE_PRIVATE)

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val networkMonitor = NetworkMonitor(context)
    val syncManager = SyncManager(database, networkMonitor, repositoryScope)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _userProgress = MutableStateFlow(loadProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    private val _pastQuizzes = MutableStateFlow<List<QuizResult>>(loadInitialQuizzes())
    val pastQuizzes: StateFlow<List<QuizResult>> = _pastQuizzes.asStateFlow()

    private val _sessionDraft = MutableStateFlow<StudySessionDraftEntity?>(null)
    val sessionDraft: StateFlow<StudySessionDraftEntity?> = _sessionDraft.asStateFlow()

    init {
        // Collect session draft from Room
        repositoryScope.launch {
            database.studySessionDraftDao().getDraftSession().collect { draft ->
                _sessionDraft.value = draft
            }
        }

        // Initialize Room with seed quizzes if empty
        repositoryScope.launch {
            try {
                val count = database.quizDao().getQuizCount()
                if (count == 0) {
                    val initial = loadInitialQuizzes()
                    initial.forEach { q ->
                        database.quizDao().insertQuiz(
                            QuizResultEntity(
                                id = q.id,
                                dateEpochMillis = q.dateEpochMillis,
                                domainFilterId = q.domainFilter?.id,
                                totalQuestions = q.totalQuestions,
                                correctCount = q.correctCount,
                                incorrectCount = q.incorrectCount,
                                skippedCount = q.skippedCount,
                                isMockExam = q.isMockExam,
                                timeSpentSeconds = q.timeSpentSeconds,
                                questionsJson = q.questions.joinToString(",") { it.id },
                                answersJson = "",
                                isSynced = true,
                                lastSyncedTimestamp = System.currentTimeMillis()
                            )
                        )
                    }

                    val current = _userProgress.value
                    database.userStatsDao().upsertUserStats(
                        UserStatsEntity(
                            userId = "local_user",
                            streakDays = current.streakDays,
                            lastActiveDateIso = current.lastActiveDateIso,
                            totalQuizzesCompleted = current.totalQuizzesCompleted,
                            totalQuestionsAnswered = current.totalQuestionsAnswered,
                            totalCorrect = current.totalCorrect,
                            domainMasteryJson = current.domainMastery.entries.joinToString(";") { "${it.key.id}:${it.value}" },
                            completedModulesJson = current.completedModules.joinToString(","),
                            mockExamBestScore = current.mockExamBestScore,
                            mockExamsCompleted = current.mockExamsCompleted,
                            isSynced = true,
                            lastSyncedTimestamp = System.currentTimeMillis()
                        )
                    )

                    val progressEntities = current.spacedRecords.values.map { r ->
                        QuestionProgressEntity(
                            questionId = r.questionId,
                            intervalDays = r.intervalDays,
                            easeFactor = r.easeFactor,
                            repetitionCount = r.repetitionCount,
                            lastReviewedTimestamp = r.lastReviewedTimestamp,
                            nextReviewTimestamp = r.nextReviewTimestamp,
                            correctCount = r.correctCount,
                            incorrectCount = r.incorrectCount,
                            isSynced = true,
                            lastSyncedTimestamp = System.currentTimeMillis()
                        )
                    }
                    database.questionProgressDao().upsertAllProgress(progressEntities)
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadThemeMode(): ThemeMode {
        val saved = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(saved)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadProgress(): UserProgress {
        val streak = prefs.getInt("streak_days", 4)
        val lastDate = prefs.getString("last_active_date", "") ?: ""
        val totalQuizzes = prefs.getInt("total_quizzes", 4)
        val totalQuestions = prefs.getInt("total_questions", 42)
        val totalCorrect = prefs.getInt("total_correct", 32)
        val mockBest = prefs.getInt("mock_best_score", 76)
        val mockCount = prefs.getInt("mock_completed_count", 1)

        val domainMastery = mutableMapOf<ExamDomain, Float>()
        ExamDomain.entries.forEach { domain ->
            val mastery = prefs.getFloat("domain_mastery_${domain.id}", 0.72f)
            domainMastery[domain] = mastery
        }

        val completedModules = prefs.getStringSet("completed_modules", setOf("mod_legal_1", "mod_sm_1")) ?: emptySet()
        val spacedRecords = loadSpacedRecords()

        return UserProgress(
            streakDays = streak,
            lastActiveDateIso = lastDate,
            totalQuizzesCompleted = totalQuizzes,
            totalQuestionsAnswered = totalQuestions,
            totalCorrect = totalCorrect,
            domainMastery = domainMastery,
            completedModules = completedModules,
            spacedRecords = spacedRecords,
            mockExamBestScore = mockBest,
            mockExamsCompleted = mockCount
        )
    }

    private fun loadSpacedRecords(): Map<String, SpacedRepetitionRecord> {
        val map = mutableMapOf<String, SpacedRepetitionRecord>()
        QuestionBank.allQuestions.forEach { q ->
            val keyPrefix = "sr_${q.id}_"
            if (prefs.contains("${keyPrefix}interval")) {
                val interval = prefs.getFloat("${keyPrefix}interval", 1f)
                val ease = prefs.getFloat("${keyPrefix}ease", 2.5f)
                val rep = prefs.getInt("${keyPrefix}rep", 0)
                val lastRev = prefs.getLong("${keyPrefix}last_rev", 0L)
                val nextRev = prefs.getLong("${keyPrefix}next_rev", 0L)
                val correct = prefs.getInt("${keyPrefix}correct", 0)
                val incorrect = prefs.getInt("${keyPrefix}incorrect", 0)
                map[q.id] = SpacedRepetitionRecord(
                    questionId = q.id,
                    intervalDays = interval,
                    easeFactor = ease,
                    repetitionCount = rep,
                    lastReviewedTimestamp = lastRev,
                    nextReviewTimestamp = nextRev,
                    correctCount = correct,
                    incorrectCount = incorrect
                )
            } else {
                // Initialize baseline spaced repetition records
                val isSampleMistake = q.id in listOf("sm_01", "lp_01", "ss_01")
                map[q.id] = SpacedRepetitionRecord(
                    questionId = q.id,
                    intervalDays = if (isSampleMistake) 0.5f else 2.0f,
                    easeFactor = 2.5f,
                    repetitionCount = if (isSampleMistake) 0 else 1,
                    lastReviewedTimestamp = System.currentTimeMillis() - 86400000L,
                    nextReviewTimestamp = if (isSampleMistake) System.currentTimeMillis() - 1000L else System.currentTimeMillis() + 86400000L,
                    correctCount = if (isSampleMistake) 1 else 2,
                    incorrectCount = if (isSampleMistake) 2 else 0
                )
            }
        }
        return map
    }

    private fun saveSpacedRecord(record: SpacedRepetitionRecord) {
        val keyPrefix = "sr_${record.questionId}_"
        prefs.edit().apply {
            putFloat("${keyPrefix}interval", record.intervalDays)
            putFloat("${keyPrefix}ease", record.easeFactor)
            putInt("${keyPrefix}rep", record.repetitionCount)
            putLong("${keyPrefix}last_rev", record.lastReviewedTimestamp)
            putLong("${keyPrefix}next_rev", record.nextReviewTimestamp)
            putInt("${keyPrefix}correct", record.correctCount)
            putInt("${keyPrefix}incorrect", record.incorrectCount)
            apply()
        }
    }

    private fun saveProgress(progress: UserProgress) {
        prefs.edit().apply {
            putInt("streak_days", progress.streakDays)
            putString("last_active_date", progress.lastActiveDateIso)
            putInt("total_quizzes", progress.totalQuizzesCompleted)
            putInt("total_questions", progress.totalQuestionsAnswered)
            putInt("total_correct", progress.totalCorrect)
            putInt("mock_best_score", progress.mockExamBestScore)
            putInt("mock_completed_count", progress.mockExamsCompleted)
            progress.domainMastery.forEach { (domain, value) ->
                putFloat("domain_mastery_${domain.id}", value)
            }
            putStringSet("completed_modules", progress.completedModules)
            apply()
        }
        _userProgress.value = progress
    }

    /**
     * Spaced Repetition SM-2 Algorithm Implementation
     */
    fun updateSpacedRepetition(questionId: String, isCorrect: Boolean): SpacedRepetitionRecord {
        val currentRecords = _userProgress.value.spacedRecords.toMutableMap()
        val existing = currentRecords[questionId] ?: SpacedRepetitionRecord(questionId = questionId)
        val now = System.currentTimeMillis()

        val updated = if (isCorrect) {
            val newRep = existing.repetitionCount + 1
            val newInterval = when (newRep) {
                1 -> 1.0f
                2 -> 3.0f
                else -> existing.intervalDays * existing.easeFactor
            }
            val newEase = (existing.easeFactor + 0.1f).coerceIn(1.3f, 3.0f)
            val nextReview = now + (newInterval * 86400000L).toLong()
            existing.copy(
                intervalDays = newInterval,
                easeFactor = newEase,
                repetitionCount = newRep,
                lastReviewedTimestamp = now,
                nextReviewTimestamp = nextReview,
                correctCount = existing.correctCount + 1
            )
        } else {
            // Mistake resets repetition streak and schedules immediate review within 12 hours
            val newInterval = 0.5f
            val newEase = max(1.3f, existing.easeFactor - 0.2f)
            val nextReview = now + (newInterval * 86400000L).toLong()
            existing.copy(
                intervalDays = newInterval,
                easeFactor = newEase,
                repetitionCount = 0,
                lastReviewedTimestamp = now,
                nextReviewTimestamp = nextReview,
                incorrectCount = existing.incorrectCount + 1
            )
        }

        saveSpacedRecord(updated)
        currentRecords[questionId] = updated
        _userProgress.value = _userProgress.value.copy(spacedRecords = currentRecords)

        // Offline caching in Room
        repositoryScope.launch {
            try {
                database.questionProgressDao().upsertProgress(
                    QuestionProgressEntity(
                        questionId = updated.questionId,
                        intervalDays = updated.intervalDays,
                        easeFactor = updated.easeFactor,
                        repetitionCount = updated.repetitionCount,
                        lastReviewedTimestamp = updated.lastReviewedTimestamp,
                        nextReviewTimestamp = updated.nextReviewTimestamp,
                        correctCount = updated.correctCount,
                        incorrectCount = updated.incorrectCount,
                        isSynced = false
                    )
                )
                syncManager.checkUnsyncedCounts(networkMonitor.isCurrentlyConnected())
            } catch (_: Exception) {}
        }

        return updated
    }

    fun recordQuizCompletion(
        domainFilter: ExamDomain?,
        questions: List<Question>,
        answers: Map<Int, Int>,
        isMockExam: Boolean = false,
        timeSpentSeconds: Long = 0L
    ): QuizResult {
        var correctCount = 0
        var incorrectCount = 0
        val domainQuestionCount = mutableMapOf<ExamDomain, Int>()
        val domainCorrectCount = mutableMapOf<ExamDomain, Int>()

        questions.forEachIndexed { index, question ->
            val userChoice = answers[index]
            val isCorrect = userChoice != null && userChoice == question.correctAnswerIndex
            if (isCorrect) {
                correctCount++
                domainCorrectCount[question.domain] = (domainCorrectCount[question.domain] ?: 0) + 1
            } else if (userChoice != null) {
                incorrectCount++
            }
            domainQuestionCount[question.domain] = (domainQuestionCount[question.domain] ?: 0) + 1

            // Update spaced repetition for each answered question
            if (userChoice != null) {
                updateSpacedRepetition(question.id, isCorrect)
            }
        }
        val skippedCount = questions.size - (correctCount + incorrectCount)

        // Evaluate strengths and focus areas based on accuracy
        val strengths = mutableListOf<StrengthInsight>()
        val focusAreas = mutableListOf<FocusAreaInsight>()

        domainQuestionCount.forEach { (domain, total) ->
            val correct = domainCorrectCount[domain] ?: 0
            val ratio = if (total > 0) correct.toFloat() / total else 0f
            if (ratio >= 0.7f) {
                strengths.add(
                    StrengthInsight(
                        domain = domain,
                        topicName = domain.title,
                        detail = when (domain) {
                            ExamDomain.LEGAL_PRINCIPLES -> "You show a strong understanding of essential legal concepts, correctly answering questions on PLSS section subdivision, hierarchy of calls, and sequential conveyances."
                            ExamDomain.SURVEY_METHODS -> "Your practical knowledge is excellent. You comfortably navigated horizontal curves, leveling peg tests, and geoid-ellipsoid height formulas."
                            ExamDomain.PROFESSIONAL_PRACTICE -> "Solid command of the Brooks Act (QBS), ethical disclosure obligations, and professional standard of care."
                            ExamDomain.STANDARDS_SPECS -> "Great grasp of 2021 ALTA/NSPS Table A optional items and Relative Positional Precision requirements."
                            ExamDomain.BUSINESS_PRACTICE -> "Strong awareness of scope variance management and contract limitation of liability clauses."
                        }
                    )
                )
            } else {
                focusAreas.add(
                    FocusAreaInsight(
                        domain = domain,
                        topicName = domain.title,
                        detail = when (domain) {
                            ExamDomain.LEGAL_PRINCIPLES -> "Review the specifics of unwritten rights (adverse possession vs prescriptive easements) and riparian doctrine (avulsion vs accretion)."
                            ExamDomain.SURVEY_METHODS -> "Take some time to review State Plane Coordinate grid scale vs elevation factors, and memorize curvature & refraction formulas (C+R = 0.574 × M²)."
                            ExamDomain.PROFESSIONAL_PRACTICE -> "Review mandatory reporting requirements under the NSPS Code of Ethics and licensee negligence standards."
                            ExamDomain.STANDARDS_SPECS -> "Revisit ALTA/NSPS Section 5.D reference monumentation and Table A item selection procedures."
                            ExamDomain.BUSINESS_PRACTICE -> "Double check best practices for project budgeting, change orders, and fee structure negotiations under QBS."
                        }
                    )
                )
            }
        }

        if (strengths.isEmpty() && correctCount > 0) {
            strengths.add(
                StrengthInsight(
                    domain = questions.first().domain,
                    topicName = questions.first().domain.title,
                    detail = "Demonstrated ability to recall fundamental principles under timed exam pressure."
                )
            )
        }
        if (focusAreas.isEmpty() && incorrectCount > 0) {
            focusAreas.add(
                FocusAreaInsight(
                    domain = questions.first().domain,
                    topicName = "Target Review",
                    detail = "Review the detailed explanations for missed questions to lock in the relevant formulas and legal precedents."
                )
            )
        }

        val result = QuizResult(
            id = UUID.randomUUID().toString(),
            dateEpochMillis = System.currentTimeMillis(),
            domainFilter = domainFilter,
            totalQuestions = questions.size,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            skippedCount = skippedCount,
            questions = questions,
            userAnswers = answers,
            strengths = strengths,
            focusAreas = focusAreas,
            isMockExam = isMockExam,
            timeSpentSeconds = timeSpentSeconds
        )

        // Update streak logic
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val current = _userProgress.value
        val newStreak = if (current.lastActiveDateIso == todayStr) {
            current.streakDays
        } else {
            current.streakDays + 1
        }

        // Update domain mastery map
        val updatedMastery = current.domainMastery.toMutableMap()
        domainQuestionCount.forEach { (domain, count) ->
            val correct = domainCorrectCount[domain] ?: 0
            val recentRatio = correct.toFloat() / count
            val oldMastery = updatedMastery[domain] ?: 0.5f
            updatedMastery[domain] = ((oldMastery * 0.6f) + (recentRatio * 0.4f)).coerceIn(0f, 1f)
        }

        val completedMod = if (domainFilter != null) {
            current.completedModules + "mod_${domainFilter.id}_${System.currentTimeMillis() % 1000}"
        } else {
            current.completedModules + "mod_full_practice_${System.currentTimeMillis() % 1000}"
        }

        val newMockBest = if (isMockExam) max(current.mockExamBestScore, result.percentage) else current.mockExamBestScore
        val newMockCount = if (isMockExam) current.mockExamsCompleted + 1 else current.mockExamsCompleted

        val updatedProgress = current.copy(
            streakDays = newStreak,
            lastActiveDateIso = todayStr,
            totalQuizzesCompleted = current.totalQuizzesCompleted + 1,
            totalQuestionsAnswered = current.totalQuestionsAnswered + questions.size,
            totalCorrect = current.totalCorrect + correctCount,
            domainMastery = updatedMastery,
            completedModules = completedMod,
            mockExamBestScore = newMockBest,
            mockExamsCompleted = newMockCount
        )
        saveProgress(updatedProgress)

        _pastQuizzes.value = listOf(result) + _pastQuizzes.value

        // Persist to Room database for full offline capability
        repositoryScope.launch {
            try {
                database.quizDao().insertQuiz(
                    QuizResultEntity(
                        id = result.id,
                        dateEpochMillis = result.dateEpochMillis,
                        domainFilterId = result.domainFilter?.id,
                        totalQuestions = result.totalQuestions,
                        correctCount = result.correctCount,
                        incorrectCount = result.incorrectCount,
                        skippedCount = result.skippedCount,
                        isMockExam = result.isMockExam,
                        timeSpentSeconds = result.timeSpentSeconds,
                        questionsJson = result.questions.joinToString(",") { it.id },
                        answersJson = result.userAnswers.entries.joinToString(";") { "${it.key}:${it.value}" },
                        isSynced = false
                    )
                )

                database.userStatsDao().upsertUserStats(
                    UserStatsEntity(
                        userId = "local_user",
                        streakDays = updatedProgress.streakDays,
                        lastActiveDateIso = updatedProgress.lastActiveDateIso,
                        totalQuizzesCompleted = updatedProgress.totalQuizzesCompleted,
                        totalQuestionsAnswered = updatedProgress.totalQuestionsAnswered,
                        totalCorrect = updatedProgress.totalCorrect,
                        domainMasteryJson = updatedProgress.domainMastery.entries.joinToString(";") { "${it.key.id}:${it.value}" },
                        completedModulesJson = updatedProgress.completedModules.joinToString(","),
                        mockExamBestScore = updatedProgress.mockExamBestScore,
                        mockExamsCompleted = updatedProgress.mockExamsCompleted,
                        isSynced = false
                    )
                )

                // Clear ongoing draft upon exam completion
                database.studySessionDraftDao().clearDraft()

                syncManager.checkUnsyncedCounts(networkMonitor.isCurrentlyConnected())
            } catch (_: Exception) {}
        }

        return result
    }

    fun saveDraftSession(
        domainId: String?,
        questions: List<Question>,
        userAnswers: Map<Int, Int>,
        currentIndex: Int,
        isMockExam: Boolean,
        remainingSeconds: Long
    ) {
        val draft = StudySessionDraftEntity(
            sessionId = "active_session",
            domainId = domainId,
            questionIdsJson = questions.joinToString(",") { it.id },
            answersJson = userAnswers.entries.joinToString(";") { "${it.key}:${it.value}" },
            currentIndex = currentIndex,
            isMockExam = isMockExam,
            remainingSeconds = remainingSeconds,
            timestamp = System.currentTimeMillis()
        )
        repositoryScope.launch {
            try {
                database.studySessionDraftDao().saveDraft(draft)
            } catch (_: Exception) {}
        }
    }

    fun clearDraftSession() {
        repositoryScope.launch {
            try {
                database.studySessionDraftDao().clearDraft()
            } catch (_: Exception) {}
        }
    }

    fun triggerSync() {
        syncManager.triggerSync()
    }

    fun generateQuizQuestions(
        domain: ExamDomain?,
        focusGrowthAreas: Boolean,
        questionCount: Int,
        difficulty: QuestionDifficulty,
        onlySpacedDue: Boolean = false
    ): List<Question> {
        val all = QuestionBank.allQuestions

        val pool = when {
            onlySpacedDue -> {
                val dueIds = _userProgress.value.spacedRecords.filter { it.value.isDue }.keys
                val dueQuestions = all.filter { it.id in dueIds }
                if (dueQuestions.isNotEmpty()) dueQuestions else all
            }
            domain != null -> all.filter { it.domain == domain }
            else -> all
        }

        val sortedPool = if (focusGrowthAreas) {
            val mastery = _userProgress.value.domainMastery
            pool.sortedBy { mastery[it.domain] ?: 0.5f }
        } else {
            pool.shuffled()
        }

        val count = questionCount.coerceIn(1, 80)
        val result = mutableListOf<Question>()
        var i = 0
        while (result.size < count) {
            val template = sortedPool[i % sortedPool.size]
            result.add(template.copy(id = "${template.id}_${result.size}"))
            i++
        }
        return result
    }

    /**
     * Generates a balanced 80-question Mock Exam strictly weighted by official NCEES PS Exam Domain Specifications:
     * - Legal Principles: 28% (22 questions)
     * - Survey Methods: 24% (19 questions)
     * - Professional Practice: 18% (14 questions)
     * - Standards and Specifications: 16% (13 questions)
     * - Business Practices: 14% (12 questions)
     * Total: 80 questions
     */
    fun generateMockExamQuestions(totalCount: Int = 80): List<Question> {
        val result = mutableListOf<Question>()
        val all = QuestionBank.allQuestions

        // Domain targets
        val targets = mapOf(
            ExamDomain.LEGAL_PRINCIPLES to (totalCount * 0.28).toInt(),
            ExamDomain.SURVEY_METHODS to (totalCount * 0.24).toInt(),
            ExamDomain.PROFESSIONAL_PRACTICE to (totalCount * 0.18).toInt(),
            ExamDomain.STANDARDS_SPECS to (totalCount * 0.16).toInt(),
            ExamDomain.BUSINESS_PRACTICE to (totalCount * 0.14).toInt()
        )

        targets.forEach { (domain, count) ->
            val domainPool = all.filter { it.domain == domain }.shuffled()
            var added = 0
            while (added < count && domainPool.isNotEmpty()) {
                val q = domainPool[added % domainPool.size]
                result.add(q.copy(id = "${q.id}_mock_${result.size}"))
                added++
            }
        }

        // Fill remaining to reach exact totalCount
        while (result.size < totalCount) {
            val q = all.random()
            result.add(q.copy(id = "${q.id}_mock_${result.size}"))
        }

        return result.shuffled()
    }

    /**
     * Aggregates Common Mistake Patterns for Analytics
     */
    fun getCommonMistakePatterns(): List<MistakePatternStat> {
        val records = _userProgress.value.spacedRecords
        val allQuestions = QuestionBank.allQuestions

        val patternStats = mutableMapOf<String, Pair<ExamDomain, Pair<Int, Int>>>() // pattern -> (domain, (timesEncountered, timesIncorrect))

        allQuestions.forEach { q ->
            val rec = records[q.id]
            val correct = rec?.correctCount ?: 1
            val incorrect = rec?.incorrectCount ?: 1
            val total = correct + incorrect
            val existing = patternStats[q.mistakePattern]
            if (existing != null) {
                val newTotal = existing.second.first + total
                val newInc = existing.second.second + incorrect
                patternStats[q.mistakePattern] = Pair(q.domain, Pair(newTotal, newInc))
            } else {
                patternStats[q.mistakePattern] = Pair(q.domain, Pair(total, incorrect))
            }
        }

        return patternStats.map { (pattern, data) ->
            val domain = data.first
            val total = data.second.first
            val incorrect = data.second.second
            val advice = when {
                pattern.contains("Curve") -> "Always check whether the problem specifies Arc Definition (R = 5729.58 / D) or Chord Definition before plugging into formulas."
                pattern.contains("Curvature") -> "Remember C + R = 0.574 × M² applies distance squared in miles. Never forget the square power."
                pattern.contains("Aliquot") -> "Work backwards from the full 640-acre section and divide denominators successively (640 / 4 / 4 / 2)."
                pattern.contains("Deed") -> "Monuments control over bearing and distance; acreage/area is always given lowest legal precedence."
                pattern.contains("ALTA") -> "Table A Item 11 specifically governs underground utility verification. Item 5 governs contours."
                pattern.contains("Riparian") -> "Avulsion freezes the boundary in the abandoned bed; accretion slowly shifts ownership."
                pattern.contains("Geoid") -> "Remember orthometric height H = h - N. If geoid undulation N is negative, subtracting a negative adds to H."
                pattern.contains("Brooks") -> "Under federal QBS, contracts must be negotiated on qualifications first, never lowest sealed price bid."
                else -> "Review the underlying statutory rule and practice similar scenario problems."
            }
            MistakePatternStat(
                patternName = pattern,
                domain = domain,
                timesEncountered = total,
                timesIncorrect = incorrect,
                advice = advice
            )
        }.sortedByDescending { it.errorRate }
    }

    /**
     * Builds Historical Performance Trend Data Points
     */
    fun getPerformanceTrendPoints(): List<PerformanceTrendPoint> {
        val dateFormat = SimpleDateFormat("MMM d", Locale.US)
        return _pastQuizzes.value.take(7).reversed().mapIndexed { idx, quiz ->
            PerformanceTrendPoint(
                quizId = quiz.id,
                label = dateFormat.format(Date(quiz.dateEpochMillis)),
                scorePercentage = quiz.percentage,
                correctCount = quiz.correctCount,
                totalCount = quiz.totalQuestions,
                timestamp = quiz.dateEpochMillis
            )
        }
    }

    private fun loadInitialQuizzes(): List<QuizResult> {
        val q = QuestionBank.allQuestions
        val now = System.currentTimeMillis()

        return listOf(
            QuizResult(
                id = "mock_exam_1",
                dateEpochMillis = now - 3600000L * 3,
                domainFilter = null,
                totalQuestions = 80,
                correctCount = 61,
                incorrectCount = 19,
                skippedCount = 0,
                questions = q.take(15),
                userAnswers = emptyMap(),
                strengths = listOf(
                    StrengthInsight(ExamDomain.LEGAL_PRINCIPLES, "Boundary Law & Retracement", "Mastered senior rights and PLSS subdivision."),
                    StrengthInsight(ExamDomain.PROFESSIONAL_PRACTICE, "Professional Ethics", "Strong adherence to NSPS code of ethics.")
                ),
                focusAreas = listOf(
                    FocusAreaInsight(ExamDomain.SURVEY_METHODS, "Geodesy & Coordinates", "Brush up on State Plane scale reductions.")
                ),
                isMockExam = true,
                timeSpentSeconds = 11200L
            ),
            QuizResult(
                id = "init_quiz_1",
                dateEpochMillis = now - 86400000L,
                domainFilter = null,
                totalQuestions = 10,
                correctCount = 8,
                incorrectCount = 2,
                skippedCount = 0,
                questions = q.take(10),
                userAnswers = emptyMap(),
                strengths = listOf(
                    StrengthInsight(ExamDomain.LEGAL_PRINCIPLES, "Aliquot Parts", "Accurate calculation of section aliquot acreage.")
                ),
                focusAreas = listOf(
                    FocusAreaInsight(ExamDomain.SURVEY_METHODS, "Curvature & Refraction", "Review C + R = 0.574 × M².")
                )
            ),
            QuizResult(
                id = "init_quiz_2",
                dateEpochMillis = now - 86400000L * 2,
                domainFilter = ExamDomain.LEGAL_PRINCIPLES,
                totalQuestions = 10,
                correctCount = 7,
                incorrectCount = 3,
                skippedCount = 0,
                questions = q.filter { it.domain == ExamDomain.LEGAL_PRINCIPLES }.take(5),
                userAnswers = emptyMap(),
                strengths = emptyList(),
                focusAreas = emptyList()
            ),
            QuizResult(
                id = "init_quiz_3",
                dateEpochMillis = now - 86400000L * 3,
                domainFilter = ExamDomain.SURVEY_METHODS,
                totalQuestions = 10,
                correctCount = 6,
                incorrectCount = 4,
                skippedCount = 0,
                questions = q.filter { it.domain == ExamDomain.SURVEY_METHODS }.take(5),
                userAnswers = emptyMap(),
                strengths = emptyList(),
                focusAreas = emptyList()
            )
        )
    }
}
