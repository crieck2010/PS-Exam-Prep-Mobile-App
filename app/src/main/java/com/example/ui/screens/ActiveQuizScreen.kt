package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.QuestionOptionCard
import com.example.ui.components.QuizProgressBar
import com.example.ui.theme.*
import com.example.viewmodel.ActiveQuizUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    state: ActiveQuizUiState,
    onSelectOption: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExit: () -> Unit,
    onScoreSummaryClick: () -> Unit,
    onToggleFlag: () -> Unit = {},
    onJumpToQuestion: (Int) -> Unit = {},
    onSubmitMockExam: () -> Unit = onScoreSummaryClick,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentQuestion = state.currentQuestion
    val questionNumber = state.currentIndex + 1
    val letters = listOf("A", "B", "C", "D")

    var showNavigatorDialog by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }

    BackHandler {
        if (state.isMockExam) {
            showSubmitConfirmation = true
        } else {
            onExit()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                // Top Bar with Close, Title, Timer (if mock), Flag, Navigator, Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (state.isMockExam) showSubmitConfirmation = true else onExit()
                        },
                        modifier = Modifier.testTag("button_close_quiz")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close quiz",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                state.isMockExam -> "NCEES PS Mock Exam"
                                state.domainFilter != null -> "PS Prep - ${state.domainFilter.shortName}"
                                else -> "PS Prep - NCEES Practice"
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = when {
                                state.isMockExam -> "${state.questions.size} Questions • Timed Simulation"
                                state.isReviewModeOnly -> "Review Mode"
                                else -> "Practice Questions"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Mock Exam Countdown Timer
                    if (state.isMockExam) {
                        val isWarning = state.remainingSeconds < 900 // under 15 mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isWarning) Color(0xFF451A1A) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isWarning) Color(0xFFEF4444) else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("mock_exam_timer")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isWarning) Color(0xFFF87171) else BlueAccentLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.formattedRemainingTime,
                                    color = if (isWarning) Color(0xFFF87171) else MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Flag button (for mock exam or general review)
                    IconButton(
                        onClick = onToggleFlag,
                        modifier = Modifier.testTag("button_flag_question")
                    ) {
                        Icon(
                            imageVector = if (state.isCurrentFlagged) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                            contentDescription = "Flag for review",
                            tint = if (state.isCurrentFlagged) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Question Navigator Grid icon
                    IconButton(
                        onClick = { showNavigatorDialog = true },
                        modifier = Modifier.testTag("button_question_navigator")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Question grid",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "NCEES PS Exam Problem: ${currentQuestion?.questionText ?: "Practice"}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Question"))
                        },
                        modifier = Modifier.testTag("button_share_question")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Progress Bar with dynamic pill counters (or Mock Exam question status)
                if (!state.isMockExam) {
                    QuizProgressBar(
                        currentIndex = state.currentIndex,
                        totalQuestions = state.questions.size,
                        correctCount = state.correctCount,
                        incorrectCount = state.incorrectCount
                    )
                } else {
                    // Mock exam progress tracker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question $questionNumber of ${state.questions.size}",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${state.answeredCount} Answered",
                                color = Color(0xFF4ADE80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.flaggedIndices.size} Flagged",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back and Next pill buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back pill button
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = state.currentIndex > 0) { onPrevious() }
                            .testTag("button_quiz_back"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back",
                            color = if (state.currentIndex > 0) MaterialTheme.colorScheme.onBackground else TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Next / Finish / Submit pill button
                    val isLastQuestion = state.currentIndex >= state.questions.size - 1
                    Box(
                        modifier = Modifier
                            .width(if (state.isMockExam && isLastQuestion) 130.dp else 110.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(BlueAccentDark)
                            .clickable {
                                if (state.isMockExam && isLastQuestion) {
                                    showSubmitConfirmation = true
                                } else {
                                    onNext()
                                }
                            }
                            .testTag("button_quiz_next"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                state.isMockExam && isLastQuestion -> "Submit Exam"
                                isLastQuestion && !state.isReviewModeOnly -> "Finish"
                                else -> "Next"
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Link "Back to score summary" or "Submit Mock Exam"
                Text(
                    text = if (state.isMockExam) "Submit Exam Early" else "Back to score summary",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .clickable {
                            if (state.isMockExam) showSubmitConfirmation = true else onScoreSummaryClick()
                        }
                        .padding(8.dp)
                        .testTag("link_back_to_summary")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (currentQuestion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No questions loaded", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Question Domain badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question $questionNumber",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentQuestion.domain.shortName,
                            color = BlueAccentLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question Text
                Text(
                    text = currentQuestion.questionText,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Options list
                val userChoice = state.userAnswers[state.currentIndex]
                currentQuestion.options.forEachIndexed { optIdx, optText ->
                    val letter = letters.getOrElse(optIdx) { "${optIdx + 1}" }
                    val isSelected = userChoice == optIdx
                    val isCorrect = optIdx == currentQuestion.correctAnswerIndex

                    QuestionOptionCard(
                        optionLetter = letter,
                        optionText = optText,
                        isSelected = isSelected,
                        isCorrectOption = isCorrect,
                        isAnswerRevealed = state.isAnswerRevealedForCurrent,
                        explanationText = currentQuestion.explanation,
                        onSelect = { onSelectOption(optIdx) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Question Navigator Dialog (allows jumping to any question)
    if (showNavigatorDialog) {
        Dialog(onDismissRequest = { showNavigatorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Question Navigator",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendItem(label = "Current", color = BlueAccent)
                        LegendItem(label = "Answered", color = Color(0xFF4ADE80))
                        LegendItem(label = "Flagged", color = Color(0xFFFBBF24))
                        LegendItem(label = "Unanswered", color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.questions.size) { idx ->
                            val isCurrent = idx == state.currentIndex
                            val isAnswered = state.userAnswers.containsKey(idx)
                            val isFlagged = state.flaggedIndices.contains(idx)

                            val bg = when {
                                isCurrent -> BlueAccent
                                isAnswered -> Color(0xFF166534)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .border(
                                        width = if (isFlagged) 2.dp else 1.dp,
                                        color = if (isFlagged) Color(0xFFFBBF24) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onJumpToQuestion(idx)
                                        showNavigatorDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showNavigatorDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
    }

    // Submit Mock Exam Confirmation Dialog
    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = {
                Text(
                    text = "Submit Mock Exam?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to finish and submit your exam?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Answered: ${state.answeredCount} / ${state.questions.size}",
                        color = Color(0xFF4ADE80),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• Unanswered: ${state.unansweredCount}",
                        color = if (state.unansweredCount > 0) Color(0xFFF87171) else Color(0xFF4ADE80),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• Flagged for review: ${state.flaggedIndices.size}",
                        color = Color(0xFFFBBF24),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        onSubmitMockExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccentDark)
                ) {
                    Text("Yes, Submit Exam", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Return to Exam", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}
