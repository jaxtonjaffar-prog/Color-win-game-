package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawRound
import com.example.ui.theme.*

@Composable
fun TrendChartScreen(
    rounds: List<DrawRound>,
    modifier: Modifier = Modifier
) {
    val sampleRounds = rounds.take(30)
    val total = sampleRounds.size.coerceAtLeast(1)

    val greenCount = sampleRounds.count { it.hasGreen }
    val redCount = sampleRounds.count { it.hasRed }
    val violetCount = sampleRounds.count { it.hasViolet }
    val bigCount = sampleRounds.count { it.isBig }
    val smallCount = total - bigCount

    val greenPct = (greenCount.toFloat() / total * 100).toInt()
    val redPct = (redCount.toFloat() / total * 100).toInt()
    val violetPct = (violetCount.toFloat() / total * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "STATISTICAL TREND & PROBABILITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Color Ratio Percentages
            Text(
                text = "Color Frequency (Last $total Rounds)",
                fontSize = 11.sp,
                color = ColorGoldLight,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatBadge(
                    title = "Green",
                    count = "$greenCount ($greenPct%)",
                    color = ColorGreen,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    title = "Violet",
                    count = "$violetCount ($violetPct%)",
                    color = ColorViolet,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    title = "Red",
                    count = "$redCount ($redPct%)",
                    color = ColorRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Big vs Small Distribution
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Big (5-9): $bigCount (${(bigCount * 100 / total)}%)",
                    fontSize = 11.sp,
                    color = ColorGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Small (0-4): $smallCount (${(smallCount * 100 / total)}%)",
                    fontSize = 11.sp,
                    color = ColorBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Dual Progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(bigCount.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(ColorGold)
                )
                Box(
                    modifier = Modifier
                        .weight(smallCount.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(ColorBlue)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Hot & Cold Numbers Frequency Heatmap
            Text(
                text = "Number Frequency Heatmap (0 - 9)",
                fontSize = 11.sp,
                color = ColorGoldLight,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val numberFrequencies = (0..9).map { n ->
                n to sampleRounds.count { it.resultNumber == n }
            }
            val maxFreq = numberFrequencies.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                numberFrequencies.forEach { (num, freq) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${freq}x",
                            fontSize = 9.sp,
                            color = if (freq >= maxFreq) ColorGold else TextTertiary,
                            fontWeight = if (freq >= maxFreq) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when (num) {
                                        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
                                        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
                                        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
                                        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
                                    }
                                )
                        ) {
                            Text(
                                text = num.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Bead Plate Roadmap (Sequence of recent 24 rounds)
            Text(
                text = "Bead Plate Trend Roadmap",
                fontSize = 11.sp,
                color = ColorGoldLight,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleRounds.take(12).reversed().forEach { r ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DrawBallMini(number = r.resultNumber)
                            Text(
                                text = if (r.isBig) "B" else "S",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (r.isBig) ColorGold else ColorBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = count,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }
    }
}
