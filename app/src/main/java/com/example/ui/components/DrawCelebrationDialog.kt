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
import androidx.compose.ui.draw.rotate
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
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, if (isWin) ColorGold else DividerColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // Header badge
                if (isWin) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = "Win",
                        tint = ColorGold,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CONGRATULATIONS!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ColorGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "YOU WON ₹${"%,.2f".format(celebration.winAmount)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorGreen,
                        modifier = Modifier.testTag("celebration_win_amount")
                    )
                } else if (celebration.hadBets) {
                    Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = "Lost",
                        tint = TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "BETTER LUCK NEXT ROUND!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                } else {
                    Text(
                        text = "DRAW RESULT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Period ${round.periodId}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                // The Big Result Ball
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
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(baseBrush)
                        .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .testTag("celebration_ball_${number}")
                ) {
                    Text(
                        text = number.toString(),
                        fontSize = 38.sp,
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
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (round.colorsString) {
                                "VIOLET_RED" -> "Red + Violet"
                                "VIOLET_GREEN" -> "Green + Violet"
                                "GREEN" -> "Green"
                                else -> "Red"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                round.hasGreen && round.hasViolet -> ColorGreen
                                round.hasRed && round.hasViolet -> ColorRed
                                round.hasGreen -> ColorGreen
                                else -> ColorRed
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        color = SurfaceHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (round.isBig) "Big" else "Small",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (round.isBig) ColorGold else ColorBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Provably Fair Proof box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
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
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Provably Fair SHA-256 Hash",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = round.hashProof.take(24) + "...",
                            fontSize = 10.sp,
                            color = TextTertiary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("close_celebration_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWin) ColorGold else SurfaceHighlight,
                        contentColor = if (isWin) Color.Black else TextPrimary
                    )
                ) {
                    Text(
                        text = "Continue Playing",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
