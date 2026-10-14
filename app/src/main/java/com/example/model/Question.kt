package com.example.model

data class Question(
    val id: String,
    val domain: ExamDomain,
    val subtopic: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: QuestionDifficulty = QuestionDifficulty.ABOUT_THE_SAME,
    val mistakePattern: String = "Fundamental Principles"
)

data class DomainScore(
    val domain: ExamDomain,
    val totalQuestions: Int,
    val correctCount: Int
) {
    val accuracy: Float
        get() = if (totalQuestions > 0) correctCount.toFloat() / totalQuestions else 0f
}

data class StrengthInsight(
    val domain: ExamDomain,
    val topicName: String,
    val detail: String
)

data class FocusAreaInsight(
    val domain: ExamDomain,
    val topicName: String,
    val detail: String
)

data class QuizResult(
    val id: String,
    val dateEpochMillis: Long,
    val domainFilter: ExamDomain?,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val skippedCount: Int,
    val questions: List<Question>,
    val userAnswers: Map<Int, Int>, // question index -> option index
    val strengths: List<StrengthInsight>,
    val focusAreas: List<FocusAreaInsight>,
    val isMockExam: Boolean = false,
    val timeSpentSeconds: Long = 0L
) {
    val percentage: Int
        get() = if (totalQuestions > 0) ((correctCount * 100) / totalQuestions) else 0

    val isPassed: Boolean
        get() = percentage >= 70

    val headline: String
        get() = when {
            percentage >= 85 -> "Outstanding mastery! You are well-prepared for the exam."
            percentage >= 70 -> "Solid progress! Keep up the good work."
            percentage >= 50 -> "Good effort! Target your focus areas for big gains."
            else -> "Needs reinforcement. Review the explanations and retry."
        }
}

// Spaced Repetition item state (SuperMemo SM-2 adaptation)
data class SpacedRepetitionRecord(
    val questionId: String,
    val intervalDays: Float = 1.0f,
    val easeFactor: Float = 2.5f,
    val repetitionCount: Int = 0,
    val lastReviewedTimestamp: Long = 0L,
    val nextReviewTimestamp: Long = 0L,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
) {
    val isDue: Boolean
        get() = System.currentTimeMillis() >= nextReviewTimestamp

    val masteryStage: MasteryStage
        get() = when {
            repetitionCount == 0 && correctCount == 0 && incorrectCount == 0 -> MasteryStage.NEW
            repetitionCount in 1..2 -> MasteryStage.LEARNING
            repetitionCount >= 3 -> MasteryStage.MASTERED
            else -> MasteryStage.REVIEW_NEEDED
        }
}

enum class MasteryStage {
    NEW,
    LEARNING,
    REVIEW_NEEDED,
    MASTERED
}

data class MistakePatternStat(
    val patternName: String,
    val domain: ExamDomain,
    val timesEncountered: Int,
    val timesIncorrect: Int,
    val advice: String
) {
    val errorRate: Float
        get() = if (timesEncountered > 0) timesIncorrect.toFloat() / timesEncountered else 0f
}

data class PerformanceTrendPoint(
    val quizId: String,
    val label: String,
    val scorePercentage: Int,
    val correctCount: Int,
    val totalCount: Int,
    val timestamp: Long
)

data class UserProgress(
    val streakDays: Int = 4,
    val lastActiveDateIso: String = "",
    val totalQuizzesCompleted: Int = 4,
    val totalQuestionsAnswered: Int = 42,
    val totalCorrect: Int = 32,
    val domainMastery: Map<ExamDomain, Float> = mapOf(
        ExamDomain.LEGAL_PRINCIPLES to 0.78f,
        ExamDomain.PROFESSIONAL_PRACTICE to 0.85f,
        ExamDomain.STANDARDS_SPECS to 0.72f,
        ExamDomain.BUSINESS_PRACTICE to 0.65f,
        ExamDomain.SURVEY_METHODS to 0.68f
    ),
    val completedModules: Set<String> = setOf("mod_legal_1", "mod_field_1", "mod_ethics_1"),
    val spacedRecords: Map<String, SpacedRepetitionRecord> = emptyMap(),
    val mockExamBestScore: Int = 0,
    val mockExamsCompleted: Int = 0
) {
    val dueCount: Int
        get() = spacedRecords.values.count { it.isDue }
}

data class Flashcard(
    val id: String,
    val domain: ExamDomain,
    val title: String,
    val front: String,
    val back: String,
    val keyTakeaway: String
)

data class StudyGuideSection(
    val id: String,
    val domain: ExamDomain,
    val title: String,
    val keyRules: List<String>,
    val formulas: List<String>,
    val examWarning: String
)
