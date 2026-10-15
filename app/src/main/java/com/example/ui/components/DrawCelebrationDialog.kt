package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.DrawCelebration

@Composable
fun DrawCelebrationDialog(
    celebration: DrawCelebration,
    onDismiss: () -> Unit
) {
    val round = celebration.round
    val isWin = celebration.winAmount > 0.0

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ball_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Confetti overlay on win!
            if (isWin) {
                ConfettiView(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(bottom = 20.dp)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, if (isWin) ColorGold else DividerColor, RoundedCornerShape(22.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header badge
                    if (isWin) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = "Win Celebration",
                            tint = ColorGold,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CONGRATULATIONS!",
                            fontSize = 20.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Black,
                            color = ColorGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "YOU WON ₹${"%,.2f".format(celebration.winAmount)}",
                            fontSize = 24.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Black,
                            color = ColorGreen,
                            modifier = Modifier.testTag("celebration_win_amount")
                        )
                    } else if (celebration.hadBets) {
                        Icon(
                            imageVector = Icons.Default.SentimentDissatisfied,
                            contentDescription = "Lost",
                            tint = TextSecondary,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BETTER LUCK NEXT ROUND!",
                            fontSize = 17.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    } else {
                        Text(
                            text = "ROUND DRAW RESULT",
                            fontSize = 18.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = ColorGold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Period ${round.periodId}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // The Glowing Big Result Ball
                    val number = round.resultNumber
                    val baseBrush = when (number) {
                        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
                        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
                        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
                        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(scale)
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(baseBrush)
                            .border(3.5.dp, Color.White, CircleShape)
                            .testTag("celebration_ball_${number}")
                    ) {
                        Text(
                            text = number.toString(),
                            fontSize = 40.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result tags: Colors & Size
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                        ) {
                            Text(
                                text = when (round.colorsString) {
                                    "VIOLET_RED" -> "Red + Violet"
                                    "VIOLET_GREEN" -> "Green + Violet"
                                    "GREEN" -> "Green"
                                    else -> "Red"
                                },
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    round.hasGreen && round.hasViolet -> ColorGreen
                                    round.hasRed && round.hasViolet -> ColorRed
                                    round.hasGreen -> ColorGreen
                                    else -> ColorRed
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            color = SurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                        ) {
                            Text(
                                text = if (round.isBig) "BIG (5-9)" else "SMALL (0-4)",
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (round.isBig) ColorGold else ColorBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Provably Fair Proof Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(0.5.dp, DividerColor, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ColorGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Provably Fair SHA-256 Verified",
                                    fontSize = 10.sp,
                                    fontFamily = PoppinsFontFamily,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = round.hashProof.take(28) + "...",
                                fontSize = 10.sp,
                                color = TextTertiary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Continue Playing Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("close_celebration_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWin) ColorGold else SurfaceHighlight,
                            contentColor = if (isWin) Color.Black else Color.White
                        )
                    ) {
                        Text(
                            text = "Continue Playing",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
