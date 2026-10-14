package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExamDomain
import com.example.ui.components.QuizConfigDialog
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .safeDrawingPadding()
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val pastQuizzes by viewModel.pastQuizzes.collectAsStateWithLifecycle()
    val activeQuizState by viewModel.activeQuizState.collectAsStateWithLifecycle()
    val latestResult by viewModel.latestQuizResult.collectAsStateWithLifecycle()
    val showConfigDialog by viewModel.showConfigDialog.collectAsStateWithLifecycle()
    val showThemeDialog by viewModel.showThemeDialog.collectAsStateWithLifecycle()
    val currentConfig by viewModel.quizConfig.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val syncInfo by viewModel.syncInfo.collectAsStateWithLifecycle()
    val sessionDraft by viewModel.sessionDraft.collectAsStateWithLifecycle()

    when (currentScreen) {
        Screen.DASHBOARD -> {
            DashboardScreen(
                userProgress = userProgress,
                pastQuizzes = pastQuizzes,
                currentThemeMode = themeMode,
                syncInfo = syncInfo,
                sessionDraft = sessionDraft,
                onOpenThemeDialog = { viewModel.openThemeDialog() },
                onTriggerSync = { viewModel.triggerSync() },
                onResumeDraftSession = { viewModel.resumeDraftSession() },
                onDiscardDraftSession = { viewModel.discardDraftSession() },
                onStartDomainQuiz = { domain: ExamDomain ->
                    viewModel.openQuizConfig(domain = domain, count = 10)
                },
                onStartQuickQuiz = {
                    viewModel.openQuizConfig(domain = null, count = 10)
                },
                onStartGrowthAreasQuiz = {
                    viewModel.openQuizConfig(domain = null, focusGrowthAreas = true, count = 10)
                },
                onStartMockExam = {
                    viewModel.startMockExam(questionCount = 80, durationMinutes = 240)
                },
                onStartSpacedReview = {
                    viewModel.openQuizConfig(isSpacedDue = true, count = userProgress.dueCount.coerceAtLeast(5))
                },
                onOpenAnalytics = {
                    viewModel.navigateTo(Screen.ANALYTICS)
                },
                onOpenFlashcards = {
                    viewModel.navigateTo(Screen.FLASHCARDS)
                },
                onOpenStudyGuide = {
                    viewModel.navigateTo(Screen.STUDY_GUIDE)
                },
                onReviewPastQuiz = { quiz ->
                    viewModel.reviewQuizQuestions(quiz)
                }
            )
        }

        Screen.ACTIVE_QUIZ -> {
            ActiveQuizScreen(
                state = activeQuizState,
                onSelectOption = { optionIdx ->
                    viewModel.selectAnswer(optionIdx)
                },
                onNext = {
                    viewModel.nextQuestion()
                },
                onPrevious = {
                    viewModel.previousQuestion()
                },
                onExit = {
                    viewModel.navigateTo(Screen.DASHBOARD)
                },
                onScoreSummaryClick = {
                    if (latestResult != null && !activeQuizState.isMockExam) {
                        viewModel.navigateTo(Screen.SCORE_SUMMARY)
                    } else {
                        viewModel.finishQuiz()
                    }
                },
                onToggleFlag = {
                    viewModel.toggleFlagCurrentQuestion()
                },
                onJumpToQuestion = { targetIndex ->
                    viewModel.jumpToQuestion(targetIndex)
                },
                onSubmitMockExam = {
                    viewModel.finishQuiz()
                }
            )
        }

        Screen.SCORE_SUMMARY -> {
            ScoreSummaryScreen(
                quizResult = latestResult,
                onTakeAnotherQuiz = {
                    viewModel.openQuizConfig()
                },
                onReviewQuestions = {
                    viewModel.reviewQuizQuestions(latestResult)
                },
                onOpenFlashcards = {
                    viewModel.navigateTo(Screen.FLASHCARDS)
                },
                onOpenStudyGuide = {
                    viewModel.navigateTo(Screen.STUDY_GUIDE)
                },
                onExitToDashboard = {
                    viewModel.navigateTo(Screen.DASHBOARD)
                }
            )
        }

        Screen.ANALYTICS -> {
            AnalyticsScreen(
                userProgress = userProgress,
                trendPoints = viewModel.getPerformanceTrendPoints(),
                mistakePatterns = viewModel.getMistakePatterns(),
                onBack = {
                    viewModel.navigateTo(Screen.DASHBOARD)
                },
                onStartSpacedReview = {
                    viewModel.openQuizConfig(isSpacedDue = true, count = userProgress.dueCount.coerceAtLeast(5))
                }
            )
        }

        Screen.FLASHCARDS -> {
            FlashcardsScreen(
                onBack = {
                    viewModel.navigateTo(Screen.DASHBOARD)
                }
            )
        }

        Screen.STUDY_GUIDE -> {
            StudyGuideScreen(
                onBack = {
                    viewModel.navigateTo(Screen.DASHBOARD)
                }
            )
        }
    }

    if (showConfigDialog) {
        QuizConfigDialog(
            initialConfig = currentConfig,
            onDismiss = { viewModel.dismissQuizConfig() },
            onConfirm = { config ->
                viewModel.updateQuizConfig(
                    domain = config.selectedDomain,
                    focusGrowthAreas = config.focusGrowthAreas,
                    count = config.questionCount,
                    difficulty = config.difficulty,
                    isSpacedDue = config.isSpacedRepetitionDueOnly
                )
                viewModel.startQuizFromConfig()
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onSelectTheme = { selected ->
                viewModel.setThemeMode(selected)
            },
            onDismiss = {
                viewModel.dismissThemeDialog()
            }
        )
    }
}
