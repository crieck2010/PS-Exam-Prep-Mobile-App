package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuestionBank
import com.example.model.ExamDomain
import com.example.model.Flashcard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDomainFilter by remember { mutableStateOf<ExamDomain?>(null) }
    var cardDeck by remember { mutableStateOf(QuestionBank.sampleFlashcards) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    // Track mastered and review needed card IDs
    var masteredCardIds by remember { mutableStateOf(setOf<String>()) }
    var reviewNeededCardIds by remember { mutableStateOf(setOf<String>()) }

    BackHandler {
        onBack()
    }

    // Filter deck based on selected domain
    val filteredCards = remember(selectedDomainFilter, cardDeck) {
        if (selectedDomainFilter == null) cardDeck else cardDeck.filter { it.domain == selectedDomainFilter }
    }

    // Clamp index if filter changes
    val safeIndex = currentIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0))
    val currentCard = filteredCards.getOrNull(safeIndex)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlipAnimation"
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "PS Exam Flashcards",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (filteredCards.isNotEmpty()) "Card ${safeIndex + 1} of ${filteredCards.size} • ${currentCard?.domain?.shortName ?: ""}" else "No cards",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("button_back_flashcards")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // Shuffle deck button
                        IconButton(
                            onClick = {
                                isFlipped = false
                                cardDeck = cardDeck.shuffled()
                                currentIndex = 0
                            },
                            modifier = Modifier.testTag("button_shuffle_cards")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle deck",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Reset progress
                        IconButton(
                            onClick = {
                                isFlipped = false
                                currentIndex = 0
                                masteredCardIds = emptySet()
                                reviewNeededCardIds = emptySet()
                            },
                            modifier = Modifier.testTag("button_reset_deck")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset deck",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Domain Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FlashcardFilterChip(
                            label = "All Cards (${cardDeck.size})",
                            isSelected = selectedDomainFilter == null,
                            onClick = {
                                selectedDomainFilter = null
                                isFlipped = false
                                currentIndex = 0
                            }
                        )
                    }
                    items(ExamDomain.entries) { domain ->
                        val count = cardDeck.count { it.domain == domain }
                        FlashcardFilterChip(
                            label = "${domain.shortName} ($count)",
                            isSelected = selectedDomainFilter == domain,
                            onClick = {
                                selectedDomainFilter = domain
                                isFlipped = false
                                currentIndex = 0
                            }
                        )
                    }
                }

                // Deck Progress Bar
                if (filteredCards.isNotEmpty()) {
                    val progressFraction = (safeIndex + 1).toFloat() / filteredCards.size
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(3.dp)
                                .background(BlueAccent)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Self-assessment feedback when back of card is revealed
                if (isFlipped && currentCard != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                reviewNeededCardIds = reviewNeededCardIds + currentCard.id
                                masteredCardIds = masteredCardIds - currentCard.id
                                if (safeIndex < filteredCards.size - 1) {
                                    isFlipped = false
                                    currentIndex = safeIndex + 1
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7F1D1D),
                                contentColor = Color(0xFFFCA5A5)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("button_card_need_review")
                        ) {
                            Text("✕ Need Review", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                masteredCardIds = masteredCardIds + currentCard.id
                                reviewNeededCardIds = reviewNeededCardIds - currentCard.id
                                if (safeIndex < filteredCards.size - 1) {
                                    isFlipped = false
                                    currentIndex = safeIndex + 1
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF14532D),
                                contentColor = Color(0xFF86EFAC)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("button_card_mastered")
                        ) {
                            Text("✓ Mastered", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Standard Previous / Flip / Next bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (safeIndex > 0) {
                                isFlipped = false
                                currentIndex = safeIndex - 1
                            }
                        },
                        enabled = safeIndex > 0,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.testTag("button_card_previous")
                    ) {
                        Text("Previous")
                    }

                    // Flip button
                    Button(
                        onClick = { isFlipped = !isFlipped },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("button_flip_card")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFlipped) "Show Front" else "Reveal Answer")
                    }

                    Button(
                        onClick = {
                            if (safeIndex < filteredCards.size - 1) {
                                isFlipped = false
                                currentIndex = safeIndex + 1
                            } else {
                                onBack()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccentDark),
                        modifier = Modifier.testTag("button_card_next")
                    ) {
                        Text(if (safeIndex < filteredCards.size - 1) "Next" else "Done")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (currentCard == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No flashcards found for this domain.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Mastery status indicator pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${masteredCardIds.size} Mastered",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF87171))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reviewNeededCardIds.size} To Review",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The 3D Interactive Flipping Flashcard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 14f * density
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.5.dp,
                            color = when {
                                masteredCardIds.contains(currentCard.id) -> Color(0xFF15803D)
                                reviewNeededCardIds.contains(currentCard.id) -> Color(0xFFB91C1C)
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { isFlipped = !isFlipped }
                        .padding(24.dp)
                        .testTag("card_flashcard_view"),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // FRONT SIDE: Concept / Question
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = currentCard.title,
                                    color = BlueAccentLight,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = currentCard.front,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 21.sp,
                                lineHeight = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(36.dp))

                            // Hint / Tap prompt
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tap card to reveal detailed answer",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // BACK SIDE: Detailed Answer & Explanation (rotated back 180 to read legibly)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF14532D).copy(alpha = 0.3f))
                                    .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Detailed Answer & Explanation",
                                    color = Color(0xFF86EFAC),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = currentCard.back,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.5.sp,
                                lineHeight = 25.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Key Takeaway / Exam Formula Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(text = "💡", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Exam Takeaway: ${currentCard.keyTakeaway}",
                                        color = if (MaterialTheme.colorScheme.background == DarkBackground) Color(0xFFFDE68A) else Color(0xFF854D0E),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun FlashcardFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF1E3A5F) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFFA8C7FA) else MaterialTheme.colorScheme.onBackground,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
