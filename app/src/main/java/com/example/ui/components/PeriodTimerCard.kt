package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.GameMode
import com.example.ui.theme.*

@Composable
fun PeriodTimerCard(
    selectedMode: GameMode,
    onSelectMode: (GameMode) -> Unit,
    periodId: String,
    remainingSeconds: Int,
    isLocked: Boolean,
    recentRounds: List<DrawRound>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Mode Selector Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GameMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(ColorGold, Color(0xFFD97706))
                                ) else Brush.horizontalGradient(
                                    listOf(SurfaceElevated, SurfaceElevated)
                                )
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 8.dp)
                            .testTag("mode_tab_${mode.name}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Win Go",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextTertiary
                            )
                            Text(
                                text = mode.title.removePrefix("Win Go "),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Period and Timer Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SurfaceElevated, SurfaceHighlight.copy(alpha = 0.6f))
                        )
                    )
                    .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Column: Period Info
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Period Number",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = periodId.ifEmpty { "Loading..." },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorGoldLight,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("current_period_id")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Recent Outcome Balls preview
                        Text(
                            text = "Recent Draws",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            recentRounds.take(5).reversed().forEach { round ->
                                DrawBallMini(number = round.resultNumber)
                            }
                        }
                    }

                    // Right Column: Countdown Box
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isLocked) "BETTING CLOSED" else "Time Remaining",
                            fontSize = 11.sp,
                            fontWeight = if (isLocked) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLocked) ColorRed else TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        val timeStr = "%02d:%02d".format(minutes, seconds)

                        val timerColor by animateColorAsState(
                            targetValue = if (isLocked) ColorRed else ColorGold,
                            label = "timer_color"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = ColorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = timeStr,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = timerColor,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("countdown_timer_text")
                            )
                        }

                        // Progress indicator bar
                        val progress = remainingSeconds.toFloat() / selectedMode.seconds.toFloat()
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(90.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isLocked) ColorRed else ColorGold,
                            trackColor = DividerColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawBallMini(number: Int, modifier: Modifier = Modifier) {
    val isDualSplit = number == 0 || number == 5
    val baseBrush = when (number) {
        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(baseBrush)
    ) {
        Text(
            text = number.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
