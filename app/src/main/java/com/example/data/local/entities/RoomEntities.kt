package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val domainFilterId: String?,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val skippedCount: Int,
    val isMockExam: Boolean,
    val timeSpentSeconds: Long,
    val questionsJson: String, // Question IDs list or summary
    val answersJson: String, // Map of question index -> answer index
    val isSynced: Boolean = false,
    val lastSyncedTimestamp: Long = 0L
)

@Entity(tableName = "question_progress")
data class QuestionProgressEntity(
    @PrimaryKey val questionId: String,
    val intervalDays: Float = 1.0f,
    val easeFactor: Float = 2.5f,
    val repetitionCount: Int = 0,
    val lastReviewedTimestamp: Long = 0L,
    val nextReviewTimestamp: Long = 0L,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isSynced: Boolean = false,
    val lastSyncedTimestamp: Long = 0L
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: String = "local_user",
    val streakDays: Int = 4,
    val lastActiveDateIso: String = "",
    val totalQuizzesCompleted: Int = 4,
    val totalQuestionsAnswered: Int = 42,
    val totalCorrect: Int = 32,
    val domainMasteryJson: String = "", // JSON map of domain ID -> mastery float
    val completedModulesJson: String = "", // Comma-delimited module IDs
    val mockExamBestScore: Int = 0,
    val mockExamsCompleted: Int = 0,
    val isSynced: Boolean = false,
    val lastSyncedTimestamp: Long = 0L
)

@Entity(tableName = "session_drafts")
data class StudySessionDraftEntity(
    @PrimaryKey val sessionId: String = "active_session",
    val domainId: String?,
    val questionIdsJson: String,
    val answersJson: String,
    val currentIndex: Int,
    val isMockExam: Boolean,
    val remainingSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)
