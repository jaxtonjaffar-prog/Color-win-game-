package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
    // Subtle breathing glow animation for vibrant buttons
    val infiniteTransition = rememberInfiniteTransition(label = "button_glow")
    val buttonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.65f else 1.0f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SELECT PREDICTION",
                    fontSize = 12.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    color = ColorGold,
                    letterSpacing = 1.sp
                )
                if (isLocked) {
                    Text(
                        text = "🔒 DRAW IN PROGRESS",
                        fontSize = 11.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = ColorRed
                    )
                } else {
                    Text(
                        text = "WIN UP TO 9.5X",
                        fontSize = 10.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Primary Vibrant Glowing Colour Buttons: Green, Violet, Red
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Vibrant Glowing Green Button (#00C853)
                GlowingColorButton(
                    title = "Green",
                    multiplier = "2X",
                    mainColor = ColorGreen,
                    darkColor = ColorGreenDark,
                    glowColor = ColorGreenGlow,
                    glowAlpha = buttonGlowAlpha,
                    testTag = "bet_green",
                    enabled = !isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.GREEN)) }
                )

                // Vibrant Glowing Violet Button (#AA00FF)
                GlowingColorButton(
                    title = "Violet",
                    multiplier = "4.5X",
                    mainColor = ColorViolet,
                    darkColor = ColorVioletDark,
                    glowColor = ColorVioletGlow,
                    glowAlpha = buttonGlowAlpha,
                    testTag = "bet_violet",
                    enabled = !isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.VIOLET)) }
                )

                // Vibrant Glowing Red Button (#D50000)
                GlowingColorButton(
                    title = "Red",
                    multiplier = "2X",
                    mainColor = ColorRed,
                    darkColor = ColorRedDark,
                    glowColor = ColorRedGlow,
                    glowAlpha = buttonGlowAlpha,
                    testTag = "bet_red",
                    enabled = !isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.RED)) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Combo Colour Options: Green+Violet and Red+Violet (1.5x)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ComboBetButton(
                    title = "Green + Violet",
                    multiplier = "1.5X Combo",
                    brush = Brush.horizontalGradient(listOf(ColorGreen, ColorViolet)),
                    testTag = "bet_green_violet",
                    enabled = !isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.GREEN_VIOLET)) }
                )

                ComboBetButton(
                    title = "Red + Violet",
                    multiplier = "1.5X Combo",
                    brush = Brush.horizontalGradient(listOf(ColorRed, ColorViolet)),
                    testTag = "bet_red_violet",
                    enabled = !isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTarget(BetTarget.ColorBet(ColorChoice.RED_VIOLET)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Numbers 0 to 9 Betting Grid
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "NUMBER BETTING",
                    fontSize = 11.sp,
                    fontFamily = PoppinsFontFamily,
                    color = ColorGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "0 & 5: Combo 9.5X | 1-4, 6-9: 9X",
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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

            // 4. Big / Small Betting Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SizeBetButton(
                    title = "BIG",
                    subtitle = "Numbers 5 - 9",
                    multiplier = "2X",
                    color = ColorGold,
                    testTag = "bet_big",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.SizeBet(SizeChoice.BIG)) }
                )

                SizeBetButton(
                    title = "SMALL",
                    subtitle = "Numbers 0 - 4",
                    multiplier = "2X",
                    color = ColorBlue,
                    testTag = "bet_small",
                    modifier = Modifier.weight(1f),
                    enabled = !isLocked,
                    onClick = { onSelectTarget(BetTarget.SizeBet(SizeChoice.SMALL)) }
                )
            }
        }
    }
}

/**
 * Glowing Color Button with neon border and gradient
 */
@Composable
fun GlowingColorButton(
    title: String,
    multiplier: String,
    mainColor: Color,
    darkColor: Color,
    glowColor: Color,
    glowAlpha: Float,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(mainColor, darkColor))
            )
            .border(
                width = 1.5.dp,
                color = mainColor.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Surface(
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = multiplier,
                    fontSize = 11.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorGoldLight,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Combo Color Button
 */
@Composable
fun ComboBetButton(
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
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = multiplier,
                    fontSize = 9.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    color = ColorGoldLight,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Number Ball Item (0 to 9) with Bold Poppins Numbers and Casino Token Rim
 */
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
            .size(56.dp)
            .clip(CircleShape)
            .background(baseBrush)
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("bet_num_$number")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number.toString(),
                fontSize = 20.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (number == 0 || number == 5) "9.5X" else "9X",
                fontSize = 8.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = ColorGoldLight
            )
        }
    }
}

/**
 * Big / Small Button
 */
@Composable
fun SizeBetButton(
    title: String,
    subtitle: String,
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
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.5.dp, color, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Surface(
                color = color,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = multiplier,
                    fontSize = 10.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
