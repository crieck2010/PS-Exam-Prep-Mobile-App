package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    userProgress: UserProgress,
    trendPoints: List<PerformanceTrendPoint>,
    mistakePatterns: List<MistakePatternStat>,
    onBack: () -> Unit,
    onStartSpacedReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Performance Analytics",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NCEES PS Exam Readiness & Diagnostic Trends",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("button_back_analytics")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Summary Card
            val overallAccuracy = if (userProgress.totalQuestionsAnswered > 0) {
                (userProgress.totalCorrect * 100) / userProgress.totalQuestionsAnswered
            } else 76

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Exam Accuracy",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$overallAccuracy%",
                            color = if (overallAccuracy >= 70) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (overallAccuracy >= 70) "Above passing standard (70%)" else "Below passing standard (70%)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Mock Exam Best",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${userProgress.mockExamBestScore}%",
                            color = BlueAccentLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${userProgress.mockExamsCompleted} simulated exams",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart 1: Performance Trends Over Time
            Text(
                text = "Performance Trends Over Time",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tracking your score trajectory across practice sessions against the 70% passing threshold.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("chart_performance_trend")
            ) {
                PerformanceTrendChart(trendPoints = trendPoints)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Chart 2: Domain Accuracy Rates
            Text(
                text = "Accuracy Rates by NCEES Domain",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Compare your mastery in each testing area against the 70% passing line.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(18.dp)
                    .testTag("chart_domain_accuracy")
            ) {
                Column {
                    ExamDomain.entries.forEach { domain ->
                        val mastery = userProgress.domainMastery[domain] ?: 0.65f
                        val percent = (mastery * 100).toInt()

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = domain.shortName,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$percent%",
                                    color = if (percent >= 70) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Horizontal progress bar with 70% benchmark tick
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(mastery)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            if (percent >= 75) Color(0xFF4ADE80)
                                            else if (percent >= 60) Color(0xFF38BDF8)
                                            else Color(0xFFF87171)
                                        )
                                )

                                // 70% threshold tick line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.70f)
                                        .wrapContentWidth(Alignment.End)
                                        .width(2.dp)
                                        .height(10.dp)
                                        .background(Color.White.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.dp)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "White mark = 70% Target Passing Level",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Spaced Repetition Mastery Status
            Text(
                text = "Spaced Repetition Recall Status",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Questions scheduled by memory decay. Practicing items when due locks them into long-term retention.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            val dueCount = userProgress.dueCount
            val masteredCount = userProgress.spacedRecords.values.count { it.masteryStage == MasteryStage.MASTERED }
            val learningCount = userProgress.spacedRecords.values.count { it.masteryStage == MasteryStage.LEARNING }
            val needsReviewCount = userProgress.spacedRecords.values.count { it.masteryStage == MasteryStage.REVIEW_NEEDED }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(18.dp)
                    .testTag("card_spaced_repetition_status")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RecallStatusPill(label = "Mastered", count = masteredCount, color = Color(0xFF4ADE80))
                        RecallStatusPill(label = "Learning", count = learningCount, color = Color(0xFF38BDF8))
                        RecallStatusPill(label = "Due Now", count = dueCount, color = Color(0xFFFBBF24))
                        RecallStatusPill(label = "Needs Work", count = needsReviewCount, color = Color(0xFFF87171))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (dueCount > 0) {
                        Button(
                            onClick = onStartSpacedReview,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlueAccentDark,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("button_start_spaced_repetition")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Practice $dueCount Due Spaced Repetition Questions", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Chart 3: Common Mistake Patterns
            Text(
                text = "Common Mistake Patterns",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Patterns identified in missed questions with specific advice to prevent exam traps.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            mistakePatterns.take(5).forEach { pattern ->
                val errorPercent = (pattern.errorRate * 100).toInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("pattern_${pattern.patternName.replace(" ", "_")}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = pattern.patternName,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (errorPercent >= 40) Color(0xFFFCA5A5).copy(alpha = 0.2f)
                                        else Color(0xFFFDE68A).copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$errorPercent% error rate",
                                    color = if (errorPercent >= 40) Color(0xFFF87171) else Color(0xFFFBBF24),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Domain: ${pattern.domain.title}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pattern.advice,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun RecallStatusPill(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.5.sp
        )
    }
}

@Composable
fun PerformanceTrendChart(
    trendPoints: List<PerformanceTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (trendPoints.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Complete quizzes to view trend graph", color = TextSecondary)
        }
        return
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingLeft = 32.dp.toPx()
        val paddingRight = 16.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw 70% passing threshold line
        val passY = paddingTop + chartHeight * (1f - 0.70f)
        drawLine(
            color = Color(0xFFFBBF24).copy(alpha = 0.7f),
            start = Offset(paddingLeft, passY),
            end = Offset(width - paddingRight, passY),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
        )

        // Draw points and connecting trend line
        val stepX = if (trendPoints.size > 1) chartWidth / (trendPoints.size - 1) else chartWidth / 2
        val path = Path()

        val pointOffsets = trendPoints.mapIndexed { index, point ->
            val x = paddingLeft + (index * stepX)
            val normalizedScore = (point.scorePercentage.toFloat() / 100f).coerceIn(0f, 1f)
            val y = paddingTop + chartHeight * (1f - normalizedScore)
            Offset(x, y)
        }

        pointOffsets.forEachIndexed { idx, offset ->
            if (idx == 0) {
                path.moveTo(offset.x, offset.y)
            } else {
                path.lineTo(offset.x, offset.y)
            }
        }

        // Draw stroke line
        drawPath(
            path = path,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw point circles
        pointOffsets.forEachIndexed { idx, offset ->
            val score = trendPoints[idx].scorePercentage
            val pointColor = if (score >= 70) Color(0xFF4ADE80) else Color(0xFFF87171)
            drawCircle(
                color = pointColor,
                radius = 5.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = offset
            )
        }
    }
}
