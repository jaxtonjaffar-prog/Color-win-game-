package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetStatus
import com.example.data.model.UserBet
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyBetsScreen(
    bets: List<UserBet>,
    onRebet: (UserBet) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredBets = when (selectedFilter) {
        "WON" -> bets.filter { it.status == BetStatus.WON }
        "LOST" -> bets.filter { it.status == BetStatus.LOST }
        "PENDING" -> bets.filter { it.status == BetStatus.PENDING }
        else -> bets
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Filter Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "ALL" to "All (${bets.size})",
                    "PENDING" to "Pending",
                    "WON" to "Won",
                    "LOST" to "Lost"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ColorGold else SurfaceElevated)
                            .clickable { selectedFilter = key }
                            .padding(vertical = 6.dp)
                            .testTag("filter_bets_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredBets.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No betting records in this section",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredBets.take(20).forEach { bet ->
                        BetHistoryItem(bet = bet, onRebet = { onRebet(bet) })
                    }
                }
            }
        }
    }
}

@Composable
fun BetHistoryItem(
    bet: UserBet,
    onRebet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(bet.timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Target & Period
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SurfaceHighlight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = bet.target.displayLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = bet.periodId.takeLast(8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                // Status Badge
                when (bet.status) {
                    BetStatus.PENDING -> {
                        Surface(
                            color = ColorGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Pending",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    BetStatus.WON -> {
                        Surface(
                            color = ColorGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Won +₹${"%,.2f".format(bet.winAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    BetStatus.LOST -> {
                        Surface(
                            color = ColorRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Lost -₹${"%,.2f".format(bet.totalAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Contract: ₹${bet.chipAmount.toInt()} × ${bet.multiplier} = ₹${bet.totalAmount.toInt()}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
