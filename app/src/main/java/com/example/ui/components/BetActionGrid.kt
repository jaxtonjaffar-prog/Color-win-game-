package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetTarget
import com.example.data.model.ColorChoice
import com.example.data.model.SizeChoice
import com.example.ui.theme.*

@Composable
fun BetActionGrid(
    isLocked: Boolean,
    onSelectTarget: (BetTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.6f else 1.0f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Section Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SELECT YOUR PREDICTION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                if (isLocked) {
                    Text(
                        text = "DRAW IN PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Colour Buttons (Green, Violet, Red)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Join Green
                ColorBetButton(
                    title = "Join Green",
                    multiplier = "2X",
                    brush = Brush.verticalGradient(listOf(ColorGreen, ColorGreenDark)),
                    testTag = "bet_green",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.GREEN)) }
                )

                // Join Violet
                ColorBetButton(
                    title = "Join Violet",
                    multiplier = "4.5X",
                    brush = Brush.verticalGradient(listOf(ColorViolet, ColorVioletDark)),
                    testTag = "bet_violet",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.VIOLET)) }
                )

                // Join Red
                ColorBetButton(
                    title = "Join Red",
                    multiplier = "2X",
                    brush = Brush.verticalGradient(listOf(ColorRed, ColorRedDark)),
                    testTag = "bet_red",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.RED)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Combo Colour Options (Green+Violet, Red+Violet - 1.5x)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ColorBetButton(
                    title = "🟢+🟣 Green+Violet",
                    multiplier = "1.5X Combo",
                    brush = Brush.horizontalGradient(listOf(ColorGreenDark, ColorVioletDark)),
                    testTag = "bet_green_violet",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.GREEN_VIOLET)) }
                )

                ColorBetButton(
                    title = "🔴+🟣 Red+Violet",
                    multiplier = "1.5X Combo",
                    brush = Brush.horizontalGradient(listOf(ColorRedDark, ColorVioletDark)),
                    testTag = "bet_red_violet",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.RED_VIOLET)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Numbers 0 to 9 Ball Grid
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Number Betting (0-9)",
                    fontSize = 11.sp,
                    color = ColorGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "0 & 5: Combo 9.5X | Others: 9X",
                    fontSize = 10.sp,
                    color = ColorGoldLight,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: 0, 1, 2, 3, 4
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                (0..4).forEach { num ->
                    NumberBallItem(
                        number = num,
                        enabled = !isLocked,
                        onClick = { onSelectTarget(BetTarget.NumberBet(num)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: 5, 6, 7, 8, 9
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                (5..9).forEach { num ->
                    NumberBallItem(
                        number = num,
                        enabled = !isLocked,
                        onClick = { onSelectTarget(BetTarget.NumberBet(num)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Big / Small Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SizeBetButton(
                    title = "Big (5-9)",
                    multiplier = "2X",
                    color = Color(0xFFEAB308),
                    testTag = "bet_big",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.SizeBet(SizeChoice.BIG)) }
                )

                SizeBetButton(
                    title = "Small (0-4)",
                    multiplier = "2X",
                    color = Color(0xFF38BDF8),
                    testTag = "bet_small",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.SizeBet(SizeChoice.SMALL)) }
                )
            }
        }
    }
}

@Composable
fun ColorBetButton(
    title: String,
    multiplier: String,
    brush: Brush,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = multiplier,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = ColorGoldLight
            )
        }
    }
}

@Composable
fun NumberBallItem(
    number: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseBrush = when (number) {
        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(baseBrush)
            .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("bet_num_$number")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (number == 0 || number == 5) "9.5X" else "9X",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGoldLight
            )
        }
    }
}

@Composable
fun SizeBetButton(
    title: String,
    multiplier: String,
    color: Color,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Surface(
                color = color,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = multiplier,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
