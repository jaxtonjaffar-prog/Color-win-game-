package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.*
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiView(
    modifier: Modifier = Modifier
) {
    val confettiColors = listOf(
        ColorGold,
        ColorGoldLight,
        ColorGreen,
        ColorViolet,
        ColorRed,
        ColorCyan,
        Color(0xFFFF4081)
    )

    val particles = remember {
        List(85) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.6f + 0.4f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                size = Random.nextFloat() * 12f + 8f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti_physics")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val y = ((progress * p.speedY) % 1f) * height
            val x = (p.initialX * width + (p.speedX * progress * width)) % width
            val rotation = progress * p.rotationSpeed

            rotate(degrees = rotation, pivot = Offset(x, y)) {
                if (p.isCircle) {
                    drawCircle(
                        color = p.color,
                        radius = p.size / 2f,
                        center = Offset(x, y)
                    )
                } else {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(x - p.size / 2f, y - p.size / 3f),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
