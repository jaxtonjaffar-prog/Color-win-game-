package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawRound
import com.example.data.model.GameMode
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class LiveAnimationMode(val label: String) {
    BALL_DROP("Ball Drop"),
    COIN_FLIP("Coin Flip")
}

@Composable
fun PeriodTimerCard(
    selectedMode: GameMode,
    onSelectMode: (GameMode) -> Unit,
    periodId: String,
    remainingSeconds: Int,
    isLocked: Boolean,
    recentRounds: List<DrawRound>,
    publishedCommitHash: String = "",
    modifier: Modifier = Modifier
) {
    var animationMode by remember { mutableStateOf(LiveAnimationMode.BALL_DROP) }
    var isSimulatingAnimation by remember { mutableStateOf(false) }
    var showFairnessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Pulsing effect for countdown timer
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isLocked) 0.94f else 0.98f,
        targetValue = if (isLocked) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLocked) 400 else 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLocked) 0.4f else 0.2f,
        targetValue = if (isLocked) 0.95f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLocked) 400 else 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Trigger ball drop / coin flip when locked (final 5s) or manually previewed
    val latestResultNumber = recentRounds.firstOrNull()?.resultNumber ?: 7
    val shouldAnimateLive = isLocked || isSimulatingAnimation

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 1. Mode Selector Tabs
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(ColorGold, ColorGoldDark)
                                ) else Brush.horizontalGradient(
                                    listOf(SurfaceElevated, SurfaceElevated)
                                )
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) ColorGold else DividerColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 8.dp)
                            .testTag("mode_tab_${mode.name}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Win Go",
                                fontSize = 9.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = if (isSelected) Color.Black.copy(alpha = 0.8f) else TextTertiary
                            )
                            Text(
                                text = mode.title.removePrefix("Win Go "),
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Period and Countdown Banner (Center Piece with Pulsing Glow)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SurfaceDark, SurfaceElevated)
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isLocked) ColorRed.copy(alpha = glowAlpha) else ColorGold.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Column: Period info & quick recent outcomes
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = ColorGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Period Number",
                                fontSize = 10.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = periodId.ifEmpty { "Loading..." },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = ColorGoldLight,
                            modifier = Modifier.testTag("current_period_id")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Badge
                        Surface(
                            color = if (isLocked) ColorRed.copy(alpha = 0.2f) else ColorGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isLocked) ColorRed else ColorGreen
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isLocked) ColorRed else ColorGreen)
                                )
                                Text(
                                    text = if (isLocked) "LOCKED" else "BETTING OPEN",
                                    fontSize = 9.sp,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) ColorRed else ColorGreen
                                )
                            }
                        }
                    }

                    // Right Column: Pulsing Countdown Timer with smooth number transitions
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.scale(pulseScale)
                    ) {
                        Text(
                            text = if (isLocked) "TIME REMAINING" else "COUNTDOWN",
                            fontSize = 10.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) ColorRed else TextSecondary,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Betting Locked",
                                    tint = ColorRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Digit Box for Minutes
                            DigitContainer(
                                digit = "%02d".format(minutes),
                                isLocked = isLocked,
                                glowAlpha = glowAlpha
                            )

                            Text(
                                text = ":",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = PoppinsFontFamily,
                                color = if (isLocked) ColorRed else ColorGold
                            )

                            // Digit Box for Seconds with Smooth Transitions
                            DigitContainer(
                                digit = "%02d".format(seconds),
                                isLocked = isLocked,
                                glowAlpha = glowAlpha
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Linear progress indicator with glowing head
                        val progress = remainingSeconds.toFloat() / selectedMode.seconds.toFloat()
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(110.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isLocked) ColorRed else ColorGold,
                            trackColor = SurfaceHighlight
                        )
                    }
                }

                if (publishedCommitHash.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = SurfaceCard.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isLocked) ColorRed.copy(alpha = 0.5f) else ColorGold.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFairnessDialog = true }
                            .testTag("fairness_commitment_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isLocked) ColorRed else ColorGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Pre-Round Hash: ${publishedCommitHash.take(10)}...${publishedCommitHash.takeLast(6)}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                color = ColorGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PROVABLY FAIR ▾",
                                    fontSize = 8.5.sp,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = ColorGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showFairnessDialog) {
                ProvablyFairDialog(
                    currentPeriodId = periodId,
                    currentCommitHash = publishedCommitHash,
                    isLocked = isLocked,
                    recentRounds = recentRounds,
                    onDismiss = { showFairnessDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Center Live Result Animation Showcase (Ball Drop / Coin Flip)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BackgroundDark)
                    .border(1.dp, BorderGlow, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Animation Header with Mode Selector & Replay button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = ColorGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LIVE RESULT ARENA",
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ColorGold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Toggle between Ball Drop and Coin Flip
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiveAnimationMode.values().forEach { mode ->
                                val isCurrent = animationMode == mode
                                Surface(
                                    color = if (isCurrent) ColorGold else SurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { animationMode = mode }
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 10.sp,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) Color.Black else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Trigger / Replay animation button
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isSimulatingAnimation = true
                                        delay(2400)
                                        isSimulatingAnimation = false
                                    }
                                },
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("preview_live_animation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Replay Animation",
                                    tint = ColorGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // The Live Animation Stage
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(SurfaceDark, SurfaceCard)
                                )
                            )
                            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                    ) {
                        if (animationMode == LiveAnimationMode.BALL_DROP) {
                            BallDropArenaView(
                                isDropping = shouldAnimateLive,
                                resultNumber = latestResultNumber
                            )
                        } else {
                            CoinFlipArenaView(
                                isFlipping = shouldAnimateLive,
                                resultNumber = latestResultNumber
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Digit Container with Smooth Number Transitions
 */
@Composable
fun DigitContainer(
    digit: String,
    isLocked: Boolean,
    glowAlpha: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 40.dp, height = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceElevated, SurfaceDark)
                )
            )
            .border(
                1.dp,
                if (isLocked) ColorRed.copy(alpha = glowAlpha) else ColorGold.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically { height -> -height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> height } + fadeOut())
            },
            label = "digit_transition"
        ) { text ->
            Text(
                text = text,
                fontSize = 22.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Black,
                color = if (isLocked) ColorRed else ColorGoldLight,
                modifier = Modifier.testTag("timer_digit_$text")
            )
        }
    }
}

/**
 * Interactive Ball Drop Physics Simulation View
 */
@Composable
fun BallDropArenaView(
    isDropping: Boolean,
    resultNumber: Int
) {
    val dropAnim = remember { Animatable(0f) }

    LaunchedEffect(isDropping, resultNumber) {
        if (isDropping) {
            dropAnim.snapTo(0f)
            dropAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(1800, easing = FastOutSlowInEasing)
            )
        } else {
            dropAnim.snapTo(1f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Pegs canvas in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw peg rows
            val rows = 3
            for (row in 0 until rows) {
                val cols = 7 - (row % 2)
                val y = height * 0.22f + (row * 16.dp.toPx())
                val step = width / (cols + 1)
                for (col in 1..cols) {
                    val x = col * step
                    drawCircle(
                        color = ColorGold.copy(alpha = 0.35f),
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Bouncing Ball
        val ballProgress = dropAnim.value
        val ballY = (ballProgress * 65).dp
        // slight zig-zag bounce based on sine wave
        val ballXOffset = (sin(ballProgress * 15f) * 18 * (1f - ballProgress)).dp

        val baseBrush = when (resultNumber) {
            0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
            5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
            1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
            else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (ballProgress < 1f) "BALL DROPPING..." else "RESULT: NUMBER $resultNumber",
                fontSize = 10.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (ballProgress < 1f) ColorGoldLight else Color.White
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = ballXOffset, y = ballY)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(baseBrush)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = resultNumber.toString(),
                    fontSize = 20.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // Bottom slots preview (0 to 9 numbers)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        ) {
            (0..9).forEach { num ->
                val isWinningSlot = num == resultNumber && ballProgress >= 0.95f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isWinningSlot) ColorGold else SurfaceHighlight)
                ) {
                    Text(
                        text = num.toString(),
                        fontSize = 9.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = if (isWinningSlot) FontWeight.Black else FontWeight.Bold,
                        color = if (isWinningSlot) Color.Black else TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * 3D Coin Flip Animation View
 */
@Composable
fun CoinFlipArenaView(
    isFlipping: Boolean,
    resultNumber: Int
) {
    val rotationY = remember { Animatable(0f) }

    LaunchedEffect(isFlipping, resultNumber) {
        if (isFlipping) {
            rotationY.snapTo(0f)
            rotationY.animateTo(
                targetValue = 1800f, // 5 full 360 spins
                animationSpec = tween(2000, easing = FastOutSlowInEasing)
            )
        } else {
            rotationY.snapTo(0f)
        }
    }

    val isFront = (rotationY.value % 360f) in 0f..90f || (rotationY.value % 360f) in 270f..360f

    val baseBrush = when (resultNumber) {
        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = if (rotationY.isRunning) "COIN SPINNING IN AIR..." else "WINNING COIN: #$resultNumber",
            fontSize = 11.sp,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            color = ColorGoldLight
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    this.cameraDistance = 18f * density
                }
                .clip(CircleShape)
                .background(if (isFront) baseBrush else Brush.radialGradient(listOf(ColorGold, ColorGoldDark)))
                .border(2.5.dp, ColorGold, CircleShape)
        ) {
            Text(
                text = if (isFront) resultNumber.toString() else "★",
                fontSize = 26.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Black,
                color = if (isFront) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun DrawBallMini(number: Int, modifier: Modifier = Modifier) {
    val baseBrush = when (number) {
        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(baseBrush)
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    ) {
        Text(
            text = number.toString(),
            fontSize = 11.sp,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
