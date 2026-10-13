package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawRound
import com.example.ui.components.DrawBallMini
import com.example.ui.theme.*

@Composable
fun TrendsScreen(
    rounds: List<DrawRound>,
    modifier: Modifier = Modifier
) {
    val sampleRounds = rounds.take(50)

    // Calculate Missed Patterns (rounds since last appearance)
    val roundsSinceViolet = sampleRounds.indexOfFirst { it.hasViolet }.let { if (it == -1) sampleRounds.size else it }
    val roundsSinceZero = sampleRounds.indexOfFirst { it.resultNumber == 0 }.let { if (it == -1) sampleRounds.size else it }
    val roundsSinceFive = sampleRounds.indexOfFirst { it.resultNumber == 5 }.let { if (it == -1) sampleRounds.size else it }
    val roundsSinceBig = sampleRounds.indexOfFirst { it.isBig }.let { if (it == -1) sampleRounds.size else it }
    val roundsSinceSmall = sampleRounds.indexOfFirst { !it.isBig }.let { if (it == -1) sampleRounds.size else it }

    // Streak Analysis (Current Dragon / Tiger streak)
    var currentStreakColor = "NONE"
    var currentStreakCount = 0
    if (sampleRounds.isNotEmpty()) {
        val firstIsGreen = sampleRounds.first().hasGreen && !sampleRounds.first().hasRed
        val firstIsRed = sampleRounds.first().hasRed && !sampleRounds.first().hasGreen
        currentStreakColor = if (firstIsGreen) "Green" else if (firstIsRed) "Red" else "Violet"

        for (r in sampleRounds) {
            val isMatch = when (currentStreakColor) {
                "Green" -> r.hasGreen && !r.hasRed
                "Red" -> r.hasRed && !r.hasGreen
                else -> r.hasViolet
            }
            if (isMatch) currentStreakCount++ else break
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Dragon vs Tiger Streak Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DividerColor, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DRAGON / TIGER STREAK TRACKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Current Streak Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (currentStreakColor == "Green") ColorGreen else ColorRed,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Active Streak", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$currentStreakCount × $currentStreakColor",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (currentStreakColor == "Green") ColorGreen else ColorRed
                                )
                                Text(text = "Consecutive Runs", fontSize = 9.sp, color = TextTertiary)
                            }
                        }

                        // Dominance Ratio Card
                        val greenWins = sampleRounds.count { it.hasGreen }
                        val redWins = sampleRounds.count { it.hasRed }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, ColorGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "50-Round Ratio", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🟢 $greenWins : 🔴 $redWins",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ColorGoldLight
                                )
                                Text(text = "Green vs Red Dominance", fontSize = 9.sp, color = TextTertiary)
                            }
                        }
                    }
                }
            }
        }

        // 2. Missed / Follow Pattern Matrix
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MISSED & FOLLOW PATTERNS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rounds since target last appeared (Hot/Cold timing)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MissedPill(label = "🟣 Violet", roundsMissed = roundsSinceViolet, color = ColorViolet, modifier = Modifier.weight(1f))
                        MissedPill(label = "Combo 0", roundsMissed = roundsSinceZero, color = ColorRed, modifier = Modifier.weight(1f))
                        MissedPill(label = "Combo 5", roundsMissed = roundsSinceFive, color = ColorGreen, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MissedPill(label = "Big (5-9)", roundsMissed = roundsSinceBig, color = ColorGold, modifier = Modifier.weight(1f))
                        MissedPill(label = "Small (0-4)", roundsMissed = roundsSinceSmall, color = ColorBlue, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 3. Last 50 Results History Table
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LAST 50 ROUNDS RESULTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sampleRounds.forEach { round ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = round.periodId.takeLast(8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DrawBallMini(number = round.resultNumber)
                                    Text(
                                        text = round.resultNumber.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = if (round.isBig) "Big" else "Small",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (round.isBig) ColorGold else ColorBlue
                                )

                                Surface(
                                    color = SurfaceHighlight,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = when (round.colorsString) {
                                            "VIOLET_RED" -> "Red+Violet"
                                            "VIOLET_GREEN" -> "Green+Violet"
                                            "GREEN" -> "Green"
                                            else -> "Red"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            round.hasGreen && round.hasViolet -> ColorGreen
                                            round.hasRed && round.hasViolet -> ColorRed
                                            round.hasGreen -> ColorGreen
                                            else -> ColorRed
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MissedPill(
    label: String,
    roundsMissed: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(
                text = "${roundsMissed}x Missed",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (roundsMissed >= 6) ColorRed else TextSecondary
            )
        }
    }
}
