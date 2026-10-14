package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun QuestionOptionCard(
    optionLetter: String,
    optionText: String,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    isAnswerRevealed: Boolean,
    explanationText: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showExplanation = isAnswerRevealed && (isCorrectOption || isSelected)

    // Border and background state styling
    val cardBackground = when {
        isAnswerRevealed && isCorrectOption -> Color(0xFF1E2421) // subtle green tint
        isAnswerRevealed && isSelected && !isCorrectOption -> Color(0xFF281E20) // subtle red tint
        isSelected -> Color(0xFF252830)
        else -> DarkSurface
    }

    val cardBorder = when {
        isAnswerRevealed && isCorrectOption -> Color(0xFF15803D)
        isAnswerRevealed && isSelected && !isCorrectOption -> Color(0xFFB91C1C)
        isSelected -> BlueAccent
        else -> Color(0xFF2C2D31)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = !isAnswerRevealed) { onSelect() }
            .padding(16.dp)
            .testTag("option_$optionLetter")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option letter e.g. "A."
            Text(
                text = "$optionLetter.",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Option text
            Text(
                text = optionText,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Badges
            if (isAnswerRevealed) {
                if (isSelected) {
                    Text(
                        text = "(Your answer)",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                if (isCorrectOption) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF86EFAC))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ Correct answer",
                            color = Color(0xFF064E3B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFCA5A5))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✕ Incorrect",
                            color = Color(0xFF7F1D1D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Detailed explanation when answer is revealed for the correct option
        AnimatedVisibility(
            visible = showExplanation && isCorrectOption,
            enter = fadeIn() + expandVertically()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF2E3035))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = explanationText,
                    color = Color(0xFFD1D5DB),
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
