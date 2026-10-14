package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun QuizProgressBar(
    currentIndex: Int,
    totalQuestions: Int,
    correctCount: Int,
    incorrectCount: Int,
    modifier: Modifier = Modifier
) {
    val currentDisplay = (currentIndex + 1).coerceAtMost(totalQuestions)
    val fraction = if (totalQuestions > 0) currentDisplay.toFloat() / totalQuestions else 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Slider-like progress indicator with circle indicator thumb
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track background line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2C2D31))
            )

            // Active track line
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF5F6368))
            )

            // Circle thumb at current position
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .wrapContentWidth(Alignment.End)
                    .size(16.dp)
                    .border(2.dp, Color(0xFF8E9196), CircleShape)
                    .background(DarkBackground, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Fraction e.g. "3 / 50"
        Text(
            text = "$currentDisplay / $totalQuestions",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Incorrect Pill Badge (Red with ✕ count)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFCA5A5).copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✕ $incorrectCount",
                color = Color(0xFF7F1D1D),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Correct Pill Badge (Green with ✓ count)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF86EFAC).copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✓ $correctCount",
                color = Color(0xFF064E3B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
