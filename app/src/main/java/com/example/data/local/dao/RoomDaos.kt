package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_results ORDER BY dateEpochMillis DESC")
    fun getAllQuizzes(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizResultEntity)

    @Query("SELECT * FROM quiz_results WHERE isSynced = 0")
    suspend fun getUnsyncedQuizzes(): List<QuizResultEntity>

    @Query("UPDATE quiz_results SET isSynced = 1, lastSyncedTimestamp = :timestamp WHERE id IN (:ids)")
    suspend fun markQuizzesSynced(ids: List<String>, timestamp: Long)

    @Query("SELECT COUNT(*) FROM quiz_results")
    suspend fun getQuizCount(): Int
}

@Dao
interface QuestionProgressDao {
    @Query("SELECT * FROM question_progress")
    fun getAllProgress(): Flow<List<QuestionProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: QuestionProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllProgress(list: List<QuestionProgressEntity>)

    @Query("SELECT * FROM question_progress WHERE isSynced = 0")
    suspend fun getUnsyncedProgress(): List<QuestionProgressEntity>

    @Query("UPDATE question_progress SET isSynced = 1, lastSyncedTimestamp = :timestamp WHERE questionId IN (:ids)")
    suspend fun markProgressSynced(ids: List<String>, timestamp: Long)

    @Query("SELECT * FROM question_progress WHERE questionId = :id")
    suspend fun getProgressForQuestion(id: String): QuestionProgressEntity?
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getUserStats(userId: String = "local_user"): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserStats(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE isSynced = 0")
    suspend fun getUnsyncedStats(): List<UserStatsEntity>

    @Query("UPDATE user_stats SET isSynced = 1, lastSyncedTimestamp = :timestamp WHERE userId = :userId")
    suspend fun markStatsSynced(userId: String = "local_user", timestamp: Long)
}

@Dao
interface StudySessionDraftDao {
    @Query("SELECT * FROM session_drafts WHERE sessionId = 'active_session' LIMIT 1")
    fun getDraftSession(): Flow<StudySessionDraftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: StudySessionDraftEntity)

    @Query("DELETE FROM session_drafts WHERE sessionId = 'active_session'")
    suspend fun clearDraft()
}
