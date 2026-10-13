package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetTarget
import com.example.data.model.ColorChoice
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetBottomSheet(
    target: BetTarget,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirmBet: (chipAmount: Double, multiplier: Int) -> Unit
) {
    var selectedChip by remember { mutableStateOf(100.0) }
    var multiplier by remember { mutableStateOf(1) }
    var agreeToRules by remember { mutableStateOf(true) }

    val totalCost = selectedChip * multiplier
    val isInsufficientBalance = totalCost > currentBalance

    // Potential return calculation (2% platform service charge deducted)
    val potentialWin = (totalCost * 0.98) * target.defaultMultiplier

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            // Header with target preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "BET PLACEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = target.displayLabel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = when (target) {
                            is BetTarget.ColorBet -> when (target.color) {
                                ColorChoice.GREEN -> ColorGreen
                                ColorChoice.VIOLET -> ColorViolet
                                ColorChoice.RED -> ColorRed
                                ColorChoice.GREEN_VIOLET -> ColorGreen
                                ColorChoice.RED_VIOLET -> ColorRed
                            }
                            is BetTarget.NumberBet -> ColorGold
                            is BetTarget.SizeBet -> ColorBlue
                        }
                    )
                }

                Surface(
                    color = SurfaceHighlight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${target.defaultMultiplier}X Odds",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chip Amount Selector
            Text(
                text = "Contract Money (Per Unit)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val chips = listOf(10.0, 50.0, 100.0, 500.0, 1000.0, 5000.0)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chips.forEach { chip ->
                    val isSelected = selectedChip == chip
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) ColorGold else SurfaceElevated
                            )
                            .clickable { selectedChip = chip }
                            .testTag("chip_${chip.toInt()}")
                    ) {
                        Text(
                            text = "₹${chip.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Amount Input
            OutlinedTextField(
                value = if (selectedChip !in chips) selectedChip.toInt().toString() else "",
                onValueChange = { input ->
                    val parsed = input.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        selectedChip = parsed
                    }
                },
                label = { Text("Or Enter Custom Amount (₹)") },
                placeholder = { Text("e.g. 250") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorGold,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_bet_amount_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Multiplier Selector
            Text(
                text = "Quantity / Multiplier",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Stepper Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { if (multiplier > 1) multiplier-- },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "X$multiplier",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = ColorGoldLight
                    )

                    IconButton(
                        onClick = { multiplier++ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Quick Multiplier Pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 5, 10, 20, 50).forEach { mul ->
                        val isMulSelected = multiplier == mul
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isMulSelected) ColorGold.copy(alpha = 0.2f) else SurfaceElevated
                                )
                                .border(
                                    1.dp,
                                    if (isMulSelected) ColorGold else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { multiplier = mul }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "X$mul",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMulSelected) ColorGold else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total & Payout Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Total Contract Amount",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${"%,.2f".format(totalCost)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInsufficientBalance) ColorRed else TextPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Estimated Return (Net)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${"%,.2f".format(potentialWin)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorGreen
                        )
                    }

                    if (isInsufficientBalance) {
                        Text(
                            text = "Insufficient balance! Please deposit or lower amount.",
                            fontSize = 11.sp,
                            color = ColorRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Agreement checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { agreeToRules = !agreeToRules }
            ) {
                Checkbox(
                    checked = agreeToRules,
                    onCheckedChange = { agreeToRules = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ColorGold,
                        checkmarkColor = Color.Black
                    )
                )
                Text(
                    text = "I agree to Pre-sale Game Rules & 2% Service Fee",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (!isInsufficientBalance && agreeToRules) {
                            onConfirmBet(selectedChip, multiplier)
                        }
                    },
                    enabled = !isInsufficientBalance && agreeToRules,
                    modifier = Modifier
                        .weight(2f)
                        .testTag("confirm_bet_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Confirm Bet (₹${totalCost.toInt()})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
