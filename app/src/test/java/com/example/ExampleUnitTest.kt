package com.example

import com.example.data.QuestionBank
import com.example.model.ExamDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test QuestionBank has questions for all 5 domains`() {
        val allQuestions = QuestionBank.allQuestions
        assertTrue(allQuestions.isNotEmpty())

        ExamDomain.entries.forEach { domain ->
            val domainQuestions = allQuestions.filter { it.domain == domain }
            assertTrue("Domain $domain should have questions", domainQuestions.isNotEmpty())
        }
    }

    @Test
    fun `test all questions have valid options and correct answer index`() {
        QuestionBank.allQuestions.forEach { question ->
            assertTrue("Question ${question.id} has at least 4 options", question.options.size >= 4)
            assertTrue(
                "Question ${question.id} correctAnswerIndex is within options range",
                question.correctAnswerIndex in question.options.indices
            )
            assertTrue("Question ${question.id} has explanation", question.explanation.isNotBlank())
        }
    }

    @Test
    fun `test curve radius calculation formula`() {
        val degreeOfCurve = 4.0
        val radius = 5729.58 / degreeOfCurve
        assertEquals(1432.395, radius, 0.001)
    }

    @Test
    fun `test curvature and refraction formula`() {
        val distanceMiles = 2.0
        val cr = 0.574 * distanceMiles * distanceMiles
        assertEquals(2.296, cr, 0.001)
    }
}
