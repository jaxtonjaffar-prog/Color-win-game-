package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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
import com.example.ui.theme.*

@Composable
fun RecentResultsStrip(
    rounds: List<DrawRound>,
    modifier: Modifier = Modifier
) {
    val totalCount = rounds.size
    val greenCount = rounds.count { it.hasGreen }
    val redCount = rounds.count { it.hasRed }
    val violetCount = rounds.count { it.hasViolet }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Title + Mini stats
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
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ColorGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RECENT RESULTS STRIP",
                        fontSize = 11.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Black,
                        color = ColorGold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Summary distribution badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TrendBadge(count = greenCount, color = ColorGreen, label = "G")
                    TrendBadge(count = redCount, color = ColorRed, label = "R")
                    TrendBadge(count = violetCount, color = ColorViolet, label = "V")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Scroll of Recent Outcome Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_results_strip_row")
            ) {
                items(rounds.take(30), key = { it.periodId }) { round ->
                    RecentResultChip(round = round)
                }
            }
        }
    }
}

@Composable
fun RecentResultChip(
    round: DrawRound,
    modifier: Modifier = Modifier
) {
    val number = round.resultNumber
    val baseBrush = when (number) {
        0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
        5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
        1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
        else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
    }

    Box(
        modifier = modifier
            .width(62.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Period (last 3 digits)
            Text(
                text = round.periodId.takeLast(3),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Glowing Outcome Ball
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(baseBrush)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 13.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Big / Small tag
            Text(
                text = if (round.isBig) "BIG" else "SMALL",
                fontSize = 8.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = if (round.isBig) ColorGold else ColorBlue
            )
        }
    }
}

@Composable
fun TrendBadge(
    count: Int,
    color: Color,
    label: String
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = count.toString(),
                fontSize = 9.sp,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
