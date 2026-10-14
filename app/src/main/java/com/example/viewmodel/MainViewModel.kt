package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QuestionBank
import com.example.data.QuizRepository
import com.example.data.local.entities.StudySessionDraftEntity
import com.example.data.sync.SyncInfo
import com.example.model.*
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    ACTIVE_QUIZ,
    SCORE_SUMMARY,
    ANALYTICS,
    FLASHCARDS,
    STUDY_GUIDE
}

data class QuizConfig(
    val selectedDomain: ExamDomain? = null,
    val focusGrowthAreas: Boolean = false,
    val questionCount: Int = 10,
    val difficulty: QuestionDifficulty = QuestionDifficulty.ABOUT_THE_SAME,
    val isSpacedRepetitionDueOnly: Boolean = false
)

data class ActiveQuizUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // question index -> option index
    val isAnswerRevealedForCurrent: Boolean = false,
    val isReviewModeOnly: Boolean = false,
    val domainFilter: ExamDomain? = null,
    // Mock Exam specific states
    val isMockExam: Boolean = false,
    val remainingSeconds: Long = 0L,
    val initialSeconds: Long = 0L,
    val flaggedIndices: Set<Int> = emptySet(),
    val isTimerRunning: Boolean = false
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentIndex)

    val correctCount: Int
        get() = userAnswers.count { (idx, optIdx) ->
            questions.getOrNull(idx)?.correctAnswerIndex == optIdx
        }

    val incorrectCount: Int
        get() = userAnswers.count { (idx, optIdx) ->
            val q = questions.getOrNull(idx)
            q != null && q.correctAnswerIndex != optIdx
        }

    val formattedRemainingTime: String
        get() {
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    val isCurrentFlagged: Boolean
        get() = flaggedIndices.contains(currentIndex)

    val answeredCount: Int
        get() = userAnswers.size

    val unansweredCount: Int
        get() = questions.size - userAnswers.size
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizRepository(application)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
    val userProgress: StateFlow<UserProgress> = repository.userProgress
    val pastQuizzes: StateFlow<List<QuizResult>> = repository.pastQuizzes
    val syncInfo: StateFlow<SyncInfo> = repository.syncManager.syncInfo
    val sessionDraft: StateFlow<StudySessionDraftEntity?> = repository.sessionDraft

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _showConfigDialog = MutableStateFlow(false)
    val showConfigDialog: StateFlow<Boolean> = _showConfigDialog.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    private val _quizConfig = MutableStateFlow(QuizConfig())
    val quizConfig: StateFlow<QuizConfig> = _quizConfig.asStateFlow()

    private val _activeQuizState = MutableStateFlow(ActiveQuizUiState())
    val activeQuizState: StateFlow<ActiveQuizUiState> = _activeQuizState.asStateFlow()

    private val _latestQuizResult = MutableStateFlow<QuizResult?>(null)
    val latestQuizResult: StateFlow<QuizResult?> = _latestQuizResult.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.pastQuizzes.collect { list ->
                if (_latestQuizResult.value == null && list.isNotEmpty()) {
                    _latestQuizResult.value = list.first()
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun openThemeDialog() {
        _showThemeDialog.value = true
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openQuizConfig(
        domain: ExamDomain? = null,
        focusGrowthAreas: Boolean = false,
        count: Int = 10,
        isSpacedDue: Boolean = false
    ) {
        _quizConfig.value = QuizConfig(
            selectedDomain = domain,
            focusGrowthAreas = focusGrowthAreas,
            questionCount = count,
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            isSpacedRepetitionDueOnly = isSpacedDue
        )
        _showConfigDialog.value = true
    }

    fun dismissQuizConfig() {
        _showConfigDialog.value = false
    }

    fun updateQuizConfig(
        domain: ExamDomain? = _quizConfig.value.selectedDomain,
        focusGrowthAreas: Boolean = _quizConfig.value.focusGrowthAreas,
        count: Int = _quizConfig.value.questionCount,
        difficulty: QuestionDifficulty = _quizConfig.value.difficulty,
        isSpacedDue: Boolean = _quizConfig.value.isSpacedRepetitionDueOnly
    ) {
        _quizConfig.value = QuizConfig(
            selectedDomain = domain,
            focusGrowthAreas = focusGrowthAreas,
            questionCount = count,
            difficulty = difficulty,
            isSpacedRepetitionDueOnly = isSpacedDue
        )
    }

    fun startQuizFromConfig() {
        val config = _quizConfig.value
        val questions = repository.generateQuizQuestions(
            domain = config.selectedDomain,
            focusGrowthAreas = config.focusGrowthAreas,
            questionCount = config.questionCount,
            difficulty = config.difficulty,
            onlySpacedDue = config.isSpacedRepetitionDueOnly
        )

        stopTimer()

        _activeQuizState.value = ActiveQuizUiState(
            questions = questions,
            currentIndex = 0,
            userAnswers = emptyMap(),
            isAnswerRevealedForCurrent = false,
            isReviewModeOnly = false,
            domainFilter = config.selectedDomain,
            isMockExam = false
        )
        _showConfigDialog.value = false
        _currentScreen.value = Screen.ACTIVE_QUIZ
    }

    /**
     * Launch official 80-question timed Mock Exam
     */
    fun startMockExam(questionCount: Int = 80, durationMinutes: Int = 240) {
        val questions = repository.generateMockExamQuestions(totalCount = questionCount)
        val durationSeconds = durationMinutes * 60L

        stopTimer()

        _activeQuizState.value = ActiveQuizUiState(
            questions = questions,
            currentIndex = 0,
            userAnswers = emptyMap(),
            isAnswerRevealedForCurrent = false,
            isReviewModeOnly = false,
            domainFilter = null,
            isMockExam = true,
            remainingSeconds = durationSeconds,
            initialSeconds = durationSeconds,
            flaggedIndices = emptySet(),
            isTimerRunning = true
        )

        startTimer()
        _currentScreen.value = Screen.ACTIVE_QUIZ
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _activeQuizState.value
                if (current.isMockExam && current.isTimerRunning) {
                    if (current.remainingSeconds > 1) {
                        _activeQuizState.value = current.copy(
                            remainingSeconds = current.remainingSeconds - 1
                        )
                    } else {
                        // Timer expired! Auto submit exam
                        _activeQuizState.value = current.copy(remainingSeconds = 0L, isTimerRunning = false)
                        finishQuiz()
                        break
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun toggleFlagCurrentQuestion() {
        val state = _activeQuizState.value
        val currentIdx = state.currentIndex
        val updated = if (state.flaggedIndices.contains(currentIdx)) {
            state.flaggedIndices - currentIdx
        } else {
            state.flaggedIndices + currentIdx
        }
        _activeQuizState.value = state.copy(flaggedIndices = updated)
    }

    fun jumpToQuestion(index: Int) {
        val state = _activeQuizState.value
        if (index in state.questions.indices) {
            val hasAnswered = state.userAnswers.containsKey(index)
            _activeQuizState.value = state.copy(
                currentIndex = index,
                isAnswerRevealedForCurrent = (hasAnswered && !state.isMockExam) || state.isReviewModeOnly
            )
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val state = _activeQuizState.value
        if (state.isReviewModeOnly) return

        val currentIdx = state.currentIndex
        val updatedAnswers = state.userAnswers.toMutableMap()
        updatedAnswers[currentIdx] = optionIndex

        // In Mock Exam mode, don't reveal the answer immediately (simulating real exam environment)
        val shouldReveal = !state.isMockExam

        _activeQuizState.value = state.copy(
            userAnswers = updatedAnswers,
            isAnswerRevealedForCurrent = shouldReveal
        )

        // Cache session draft to Room database immediately
        repository.saveDraftSession(
            domainId = state.domainFilter?.id,
            questions = state.questions,
            userAnswers = updatedAnswers,
            currentIndex = state.currentIndex,
            isMockExam = state.isMockExam,
            remainingSeconds = state.remainingSeconds
        )
    }

    fun triggerSync() {
        repository.triggerSync()
    }

    fun resumeDraftSession() {
        val draft = repository.sessionDraft.value ?: return
        val allQuestions = QuestionBank.allQuestions
        val idList = draft.questionIdsJson.split(",").filter { it.isNotBlank() }

        // Reconstruct question list
        val reconstructed = idList.mapIndexed { idx, qId ->
            val baseId = qId.substringBefore("_mock_").substringBeforeLast("_")
            val found = allQuestions.find { it.id == baseId || it.id == qId }
                ?: allQuestions[idx % allQuestions.size]
            found.copy(id = qId)
        }

        // Reconstruct user answers
        val answersMap = mutableMapOf<Int, Int>()
        if (draft.answersJson.isNotBlank()) {
            draft.answersJson.split(";").forEach { pairStr ->
                val parts = pairStr.split(":")
                if (parts.size == 2) {
                    val qIdx = parts[0].toIntOrNull()
                    val optIdx = parts[1].toIntOrNull()
                    if (qIdx != null && optIdx != null) {
                        answersMap[qIdx] = optIdx
                    }
                }
            }
        }

        val domainFilter = draft.domainId?.let { domId ->
            ExamDomain.entries.find { it.id == domId }
        }

        stopTimer()

        _activeQuizState.value = ActiveQuizUiState(
            questions = if (reconstructed.isNotEmpty()) reconstructed else allQuestions.take(10),
            currentIndex = draft.currentIndex.coerceIn(0, (reconstructed.size - 1).coerceAtLeast(0)),
            userAnswers = answersMap,
            isAnswerRevealedForCurrent = !draft.isMockExam && answersMap.containsKey(draft.currentIndex),
            isReviewModeOnly = false,
            domainFilter = domainFilter,
            isMockExam = draft.isMockExam,
            remainingSeconds = draft.remainingSeconds,
            initialSeconds = if (draft.isMockExam) 240 * 60L else 0L,
            isTimerRunning = draft.isMockExam
        )

        if (draft.isMockExam) {
            startTimer()
        }

        _currentScreen.value = Screen.ACTIVE_QUIZ
    }

    fun discardDraftSession() {
        repository.clearDraftSession()
    }

    fun nextQuestion() {
        val state = _activeQuizState.value
        if (state.currentIndex < state.questions.size - 1) {
            val nextIdx = state.currentIndex + 1
            val hasAnsweredNext = state.userAnswers.containsKey(nextIdx)
            _activeQuizState.value = state.copy(
                currentIndex = nextIdx,
                isAnswerRevealedForCurrent = (hasAnsweredNext && !state.isMockExam) || state.isReviewModeOnly
            )
        } else {
            if (!state.isReviewModeOnly) {
                finishQuiz()
            } else {
                _currentScreen.value = Screen.SCORE_SUMMARY
            }
        }
    }

    fun previousQuestion() {
        val state = _activeQuizState.value
        if (state.currentIndex > 0) {
            val prevIdx = state.currentIndex - 1
            val hasAnsweredPrev = state.userAnswers.containsKey(prevIdx)
            _activeQuizState.value = state.copy(
                currentIndex = prevIdx,
                isAnswerRevealedForCurrent = (hasAnsweredPrev && !state.isMockExam) || state.isReviewModeOnly
            )
        }
    }

    fun finishQuiz() {
        stopTimer()
        val state = _activeQuizState.value
        val spentSeconds = state.initialSeconds - state.remainingSeconds

        val result = repository.recordQuizCompletion(
            domainFilter = state.domainFilter,
            questions = state.questions,
            answers = state.userAnswers,
            isMockExam = state.isMockExam,
            timeSpentSeconds = if (state.isMockExam) spentSeconds else 0L
        )
        _latestQuizResult.value = result
        _currentScreen.value = Screen.SCORE_SUMMARY
    }

    fun reviewQuizQuestions(result: QuizResult? = _latestQuizResult.value) {
        val target = result ?: return
        stopTimer()
        _activeQuizState.value = ActiveQuizUiState(
            questions = target.questions,
            currentIndex = 0,
            userAnswers = target.userAnswers,
            isAnswerRevealedForCurrent = true,
            isReviewModeOnly = true,
            domainFilter = target.domainFilter,
            isMockExam = target.isMockExam
        )
        _currentScreen.value = Screen.ACTIVE_QUIZ
    }

    fun openPastQuizResult(result: QuizResult) {
        _latestQuizResult.value = result
        _currentScreen.value = Screen.SCORE_SUMMARY
    }

    fun getMistakePatterns(): List<MistakePatternStat> {
        return repository.getCommonMistakePatterns()
    }

    fun getPerformanceTrendPoints(): List<PerformanceTrendPoint> {
        return repository.getPerformanceTrendPoints()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
