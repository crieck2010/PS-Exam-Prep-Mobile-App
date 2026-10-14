package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StudySessionDraftEntity
import com.example.data.sync.SyncInfo
import com.example.data.sync.SyncState
import com.example.model.ExamDomain
import com.example.model.QuizResult
import com.example.model.UserProgress
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    userProgress: UserProgress,
    pastQuizzes: List<QuizResult>,
    currentThemeMode: ThemeMode,
    syncInfo: SyncInfo = SyncInfo(),
    sessionDraft: StudySessionDraftEntity? = null,
    onOpenThemeDialog: () -> Unit,
    onTriggerSync: () -> Unit = {},
    onResumeDraftSession: () -> Unit = {},
    onDiscardDraftSession: () -> Unit = {},
    onStartDomainQuiz: (ExamDomain) -> Unit,
    onStartQuickQuiz: () -> Unit,
    onStartGrowthAreasQuiz: () -> Unit,
    onStartMockExam: () -> Unit,
    onStartSpacedReview: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenStudyGuide: () -> Unit,
    onReviewPastQuiz: (QuizResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDomainFilter by remember { mutableStateOf<ExamDomain?>(null) }

    val filteredDomains = remember(selectedDomainFilter) {
        if (selectedDomainFilter == null) ExamDomain.entries else listOf(selectedDomainFilter!!)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PS Exam Prep",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NCEES Principles & Practice of Surveying",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    // Theme selector button
                    IconButton(
                        onClick = onOpenThemeDialog,
                        modifier = Modifier.testTag("button_theme_mode")
                    ) {
                        Icon(
                            imageVector = when (currentThemeMode) {
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            },
                            contentDescription = "Switch theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Streak Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2E2415))
                            .border(1.dp, StreakGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("streak_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProgress.streakDays}-Day Streak",
                                color = StreakGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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

            // Sync Status & Local Room Cache Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (!syncInfo.isOnline) Color(0xFF27272A)
                        else if (syncInfo.state == SyncState.SYNCING) Color(0xFF1E3A5F)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (!syncInfo.isOnline) Color(0xFFFBBF24).copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp)
                    .testTag("bar_sync_status")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when {
                                !syncInfo.isOnline -> Icons.Default.CloudOff
                                syncInfo.state == SyncState.SYNCING -> Icons.Default.Sync
                                else -> Icons.Default.CloudDone
                            },
                            contentDescription = null,
                            tint = when {
                                !syncInfo.isOnline -> Color(0xFFFBBF24)
                                syncInfo.state == SyncState.SYNCING -> Color(0xFF38BDF8)
                                else -> Color(0xFF4ADE80)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when {
                                    !syncInfo.isOnline -> "Offline Mode • Room Cache Active"
                                    syncInfo.state == SyncState.SYNCING -> "Syncing progress with cloud..."
                                    else -> "All Data Synced • Room Offline Ready"
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when {
                                    !syncInfo.isOnline -> "${syncInfo.unsyncedCount} pending updates queued locally"
                                    syncInfo.state == SyncState.SYNCING -> "Uploading study session records"
                                    else -> "Last synced at ${syncInfo.lastSyncFormatted}"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (syncInfo.isOnline) {
                        IconButton(
                            onClick = onTriggerSync,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("button_trigger_sync")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync now",
                                tint = BlueAccentLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resume In-Progress Session Card (Cached in Room)
            if (sessionDraft != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F766E), Color(0xFF134E4A))
                            )
                        )
                        .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                        .testTag("card_resume_session_draft")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2DD4BF).copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (sessionDraft.isMockExam) "SAVED MOCK EXAM" else "CACHED STUDY SESSION",
                                    color = Color(0xFFCCFBF1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Question ${sessionDraft.currentIndex + 1}",
                                color = Color(0xFF99F6E4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (sessionDraft.isMockExam) "Continue 80-Question Mock Exam" else "Resume In-Progress Practice Session",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your answers are securely cached locally in Room and can be resumed anytime.",
                            color = Color(0xFFE6FFFA),
                            fontSize = 12.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDiscardDraftSession,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFCA5A5)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(42.dp)
                                    .testTag("button_discard_draft_session")
                            ) {
                                Text("Discard", fontSize = 13.sp)
                            }

                            Button(
                                onClick = onResumeDraftSession,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2DD4BF),
                                    contentColor = Color(0xFF042F2E)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(42.dp)
                                    .testTag("button_resume_draft_session")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume Now", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Mock Exam Simulation Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .testTag("card_mock_exam_banner")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OFFICIAL FORMAT",
                                color = Color(0xFF7DD3FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4 Hours • 80 Questions",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NCEES PS Full Mock Exam",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Simulate real testing conditions with questions proportionally sampled from all 5 domains. Answer feedback is provided upon completion.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartMockExam,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueAccentLight,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("button_start_mock_exam")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start 80-Question Mock Exam",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spaced Repetition Due Alert (if any questions due)
            if (userProgress.dueCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2E2415))
                        .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable { onStartSpacedReview() }
                        .padding(14.dp)
                        .testTag("banner_spaced_repetition_due")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${userProgress.dueCount} Questions Due for Review",
                                    color = StreakGold,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Spaced repetition recall schedule",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = onStartSpacedReview,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StreakGold,
                                contentColor = Color(0xFF451A03)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Review", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Overall Readiness & Analytics Entry Card
            val totalMasteryAvg = if (userProgress.domainMastery.isNotEmpty()) {
                (userProgress.domainMastery.values.sum() / userProgress.domainMastery.size * 100).toInt()
            } else 70

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exam Readiness",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$totalMasteryAvg%",
                            color = Color(0xFF38BDF8),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(totalMasteryAvg / 100f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row: Modules, Questions, Accuracy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            label = "Completed Modules",
                            value = "${userProgress.completedModules.size}"
                        )
                        StatItem(
                            label = "Questions Answered",
                            value = "${userProgress.totalQuestionsAnswered}"
                        )
                        val accuracy = if (userProgress.totalQuestionsAnswered > 0) {
                            (userProgress.totalCorrect * 100) / userProgress.totalQuestionsAnswered
                        } else 0
                        StatItem(
                            label = "Overall Accuracy",
                            value = "$accuracy%"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button to Open Performance Analytics Screen
                    OutlinedButton(
                        onClick = onOpenAnalytics,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("button_view_full_analytics")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = BlueAccentLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Performance Analytics & Mistake Trends",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions: Start Quick Quiz / Focus Weak Areas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartQuickQuiz,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueAccentDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("button_quick_quiz")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quick Quiz (10)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onStartGrowthAreasQuiz,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("button_growth_areas_quiz")
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Growth Areas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Domain Picker & Overview
            Text(
                text = "Practice by Exam Domain",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select an NCEES domain to practice specific questions and track module completion.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Domain Filter Picker Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    DomainFilterChip(
                        label = "All Domains",
                        isSelected = selectedDomainFilter == null,
                        onClick = { selectedDomainFilter = null },
                        modifier = Modifier.testTag("chip_domain_filter_all")
                    )
                }
                items(ExamDomain.entries) { domain ->
                    DomainFilterChip(
                        label = domain.shortName,
                        isSelected = selectedDomainFilter == domain,
                        onClick = {
                            selectedDomainFilter = if (selectedDomainFilter == domain) null else domain
                        },
                        modifier = Modifier.testTag("chip_domain_filter_${domain.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Domain Modules List
            filteredDomains.forEach { domain ->
                val mastery = userProgress.domainMastery[domain] ?: 0.6f
                val masteryPercent = (mastery * 100).toInt()
                val completedCount = userProgress.completedModules.count { it.contains(domain.id) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("card_domain_${domain.id}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = domain.title,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${domain.examWeightPercentage}% of exam • $completedCount modules completed",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }

                            // Practice button for this specific domain
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable { onStartDomainQuiz(domain) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("button_practice_domain_${domain.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Practice",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = domain.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mastery Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(mastery)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (mastery >= 0.75f) Color(0xFF4ADE80)
                                            else if (mastery >= 0.5f) Color(0xFF38BDF8)
                                            else Color(0xFFFBBF24)
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$masteryPercent% mastery",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keep Learning shortcuts
            Text(
                text = "Study Tools",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Flashcards card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { onOpenFlashcards() }
                        .padding(16.dp)
                        .testTag("button_open_flashcards")
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = Color(0xFF818CF8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Flashcards",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Quick concept review",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Study Guide card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { onOpenStudyGuide() }
                        .padding(16.dp)
                        .testTag("button_open_study_guide")
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Study Guide",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Formulas & rules",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Quizzes & Review Access
            if (pastQuizzes.isNotEmpty()) {
                Text(
                    text = "Recent Practice Sessions",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                pastQuizzes.take(3).forEach { quiz ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            .clickable { onReviewPastQuiz(quiz) }
                            .padding(14.dp)
                            .testTag("item_recent_quiz_${quiz.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (quiz.isMockExam) "NCEES PS Mock Exam" else (quiz.domainFilter?.title ?: "Full Practice Quiz"),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (quiz.isMockExam) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF1E3A8A))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("SIMULATION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = "${quiz.correctCount}/${quiz.totalQuestions} correct • ${quiz.percentage}%",
                                    color = if (quiz.percentage >= 70) Color(0xFF4ADE80) else Color(0xFFF87171),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Review",
                                    color = BlueAccentLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = BlueAccentLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
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
private fun DomainFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFF1E3A5F) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFFA8C7FA) else MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
