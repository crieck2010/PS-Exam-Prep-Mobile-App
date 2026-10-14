package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExamDomain
import com.example.model.QuestionDifficulty
import com.example.ui.theme.*
import com.example.viewmodel.QuizConfig

@Composable
fun QuizConfigDialog(
    initialConfig: QuizConfig,
    onDismiss: () -> Unit,
    onConfirm: (QuizConfig) -> Unit
) {
    var selectedDomain by remember { mutableStateOf(initialConfig.selectedDomain) }
    var focusGrowthAreas by remember { mutableStateOf(initialConfig.focusGrowthAreas) }
    var questionCount by remember { mutableIntStateOf(initialConfig.questionCount) }
    var difficulty by remember { mutableStateOf(initialConfig.difficulty) }
    var isCustomCountDialogOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1F21),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Text(
                    text = "Add more questions",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Focus section
                Text(
                    text = "Focus",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip(
                        text = "All topics",
                        isSelected = !focusGrowthAreas && selectedDomain == null,
                        onClick = {
                            focusGrowthAreas = false
                            selectedDomain = null
                        },
                        modifier = Modifier.testTag("chip_all_topics")
                    )
                    SelectableChip(
                        text = "Growth areas",
                        isSelected = focusGrowthAreas,
                        onClick = {
                            focusGrowthAreas = true
                            selectedDomain = null
                        },
                        modifier = Modifier.testTag("chip_growth_areas")
                    )
                }

                // Domain specific selection chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Or Specific Domain:",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ExamDomain.entries) { domain ->
                        SelectableChip(
                            text = domain.shortName,
                            isSelected = selectedDomain == domain && !focusGrowthAreas,
                            onClick = {
                                selectedDomain = domain
                                focusGrowthAreas = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Number of questions section
                Text(
                    text = "Number of questions",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 20).forEach { count ->
                        SelectableChip(
                            text = count.toString(),
                            isSelected = questionCount == count,
                            onClick = { questionCount = count },
                            modifier = Modifier.testTag("chip_count_$count")
                        )
                    }
                    SelectableChip(
                        text = if (questionCount !in listOf(5, 10, 20)) "$questionCount ✎" else "Custom ✎",
                        isSelected = questionCount !in listOf(5, 10, 20),
                        onClick = { isCustomCountDialogOpen = true },
                        modifier = Modifier.testTag("chip_count_custom")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Difficulty section
                Text(
                    text = "Difficulty",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip(
                        text = "Easier",
                        isSelected = difficulty == QuestionDifficulty.EASIER,
                        onClick = { difficulty = QuestionDifficulty.EASIER },
                        modifier = Modifier.testTag("chip_diff_easier")
                    )
                    SelectableChip(
                        text = "About the same",
                        isSelected = difficulty == QuestionDifficulty.ABOUT_THE_SAME,
                        onClick = { difficulty = QuestionDifficulty.ABOUT_THE_SAME },
                        modifier = Modifier.testTag("chip_diff_same")
                    )
                    SelectableChip(
                        text = "Harder",
                        isSelected = difficulty == QuestionDifficulty.HARDER,
                        onClick = { difficulty = QuestionDifficulty.HARDER },
                        modifier = Modifier.testTag("chip_diff_harder")
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_cancel_quiz_config")
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            onConfirm(
                                QuizConfig(
                                    selectedDomain = selectedDomain,
                                    focusGrowthAreas = focusGrowthAreas,
                                    questionCount = questionCount,
                                    difficulty = difficulty
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueAccentDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("button_start_quiz_config")
                    ) {
                        Text(
                            text = "Add questions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (isCustomCountDialogOpen) {
        var inputCount by remember { mutableStateOf(questionCount.toString()) }
        AlertDialog(
            onDismissRequest = { isCustomCountDialogOpen = false },
            title = { Text("Custom Question Count") },
            text = {
                OutlinedTextField(
                    value = inputCount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) inputCount = it.take(2) },
                    label = { Text("Enter 1 - 50 questions") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = inputCount.toIntOrNull() ?: 10
                        questionCount = parsed.coerceIn(1, 50)
                        isCustomCountDialogOpen = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isCustomCountDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF1E3A5F) else Color(0xFF28292C)
    val textColor = if (isSelected) Color(0xFFA8C7FA) else TextPrimary
    val borderColor = if (isSelected) Color(0xFF38BDF8) else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(if (isSelected) 1.dp else 0.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Text(
                    text = "✓ ",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
