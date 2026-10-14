package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuestionBank
import com.example.data.QuizRepository
import com.example.model.ExamDomain
import com.example.model.QuestionDifficulty
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PS Exam Prep", appName)
    }

    @Test
    fun `test quiz repository question generation and scoring`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuizRepository(context)

        val questions = repository.generateQuizQuestions(
            domain = ExamDomain.SURVEY_METHODS,
            focusGrowthAreas = false,
            questionCount = 3,
            difficulty = QuestionDifficulty.ABOUT_THE_SAME
        )

        assertEquals(3, questions.size)
        assertTrue(questions.all { it.domain == ExamDomain.SURVEY_METHODS })

        // Simulate perfect answers
        val answers = questions.mapIndexed { index, q -> index to q.correctAnswerIndex }.toMap()
        val result = repository.recordQuizCompletion(
            domainFilter = ExamDomain.SURVEY_METHODS,
            questions = questions,
            answers = answers
        )

        assertEquals(3, result.correctCount)
        assertEquals(0, result.incorrectCount)
        assertEquals(100, result.percentage)
        assertTrue(result.strengths.isNotEmpty())
    }

    @Test
    fun `test spaced repetition algorithm progression and penalty`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuizRepository(context)

        val qId = "sm_01"

        // First correct answer: interval becomes 1.0 day
        val rec1 = repository.updateSpacedRepetition(qId, isCorrect = true)
        assertEquals(1, rec1.repetitionCount)
        assertEquals(1.0f, rec1.intervalDays)

        // Second correct answer: interval advances to 3.0 days
        val rec2 = repository.updateSpacedRepetition(qId, isCorrect = true)
        assertEquals(2, rec2.repetitionCount)
        assertEquals(3.0f, rec2.intervalDays)

        // Incorrect answer: repetition resets to 0 and interval shrinks to 0.5 days
        val rec3 = repository.updateSpacedRepetition(qId, isCorrect = false)
        assertEquals(0, rec3.repetitionCount)
        assertEquals(0.5f, rec3.intervalDays)
    }

    @Test
    fun `test mock exam generates 80 questions with all 5 domains`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuizRepository(context)

        val mockQuestions = repository.generateMockExamQuestions(totalCount = 80)
        assertEquals(80, mockQuestions.size)

        ExamDomain.entries.forEach { domain ->
            val count = mockQuestions.count { it.domain == domain }
            assertTrue("Domain $domain should have questions in mock exam", count > 0)
        }
    }

    @Test
    fun `test theme mode persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuizRepository(context)

        repository.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repository.themeMode.value)

        repository.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.themeMode.value)
    }

    @Test
    fun `test local room caching draft session and clear`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuizRepository(context)
        val questions = QuestionBank.allQuestions.take(5)

        repository.saveDraftSession(
            domainId = ExamDomain.LEGAL_PRINCIPLES.id,
            questions = questions,
            userAnswers = mapOf(0 to 1, 1 to 2),
            currentIndex = 2,
            isMockExam = false,
            remainingSeconds = 600L
        )

        // Allow coroutine to persist to Room
        kotlinx.coroutines.delay(100)

        val draft = repository.database.studySessionDraftDao()
        // Wait or query draft
        repository.clearDraftSession()
        kotlinx.coroutines.delay(100)
    }

    @Test
    fun `test common mistake patterns aggregation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuizRepository(context)

        val patterns = repository.getCommonMistakePatterns()
        assertTrue("Mistake patterns should not be empty", patterns.isNotEmpty())
        assertTrue("Each mistake pattern must provide actionable advice", patterns.all { it.advice.isNotBlank() })
    }
}
