package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun ResponsibleGamblingDialog(
    profile: UserProfile?,
    onSaveLimits: (depositLimit: Double, singleBetLimit: Double, dailyLossLimit: Double, sessionMins: Int) -> Unit,
    onApplySelfExclusion: (durationHours: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var depositLimitText by remember { mutableStateOf((profile?.dailyDepositLimit ?: 25000.0).toInt().toString()) }
    var singleBetLimitText by remember { mutableStateOf((profile?.singleBetLimit ?: 5000.0).toInt().toString()) }
    var dailyLossLimitText by remember { mutableStateOf((profile?.dailyLossLimit ?: 10000.0).toInt().toString()) }
    var selectedSessionMins by remember { mutableIntStateOf(profile?.sessionTimeLimitMinutes ?: 45) }

    var selectedCoolOffHours by remember { mutableIntStateOf(24) }
    var showSelfExclusionConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ColorGreen.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = ColorGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Responsible Gaming",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Limits & Player Protection",
                                fontSize = 11.sp,
                                color = ColorGreen
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_responsible_gambling_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Current Safety Summary
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "18+ RESPONSIBLE PLAYER CHARTER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set voluntary limits to maintain safe and healthy gameplay. Once reached, transactions or wagers exceeding these thresholds are automatically paused.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Daily Deposit Limit
                Text(
                    text = "Daily Deposit Cap (₹)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = depositLimitText,
                    onValueChange = { depositLimitText = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text("e.g. 10000") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("daily_deposit_limit_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Single Bet Cap
                Text(
                    text = "Maximum Single Wager Cap (₹)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = singleBetLimitText,
                    onValueChange = { singleBetLimitText = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text("e.g. 5000") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("single_bet_limit_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Daily Loss Limit
                Text(
                    text = "Daily Net Loss Limit (₹)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dailyLossLimitText,
                    onValueChange = { dailyLossLimitText = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text("e.g. 10000") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("daily_loss_limit_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Reality Check Interval
                Text(
                    text = "Session Reality Check Alert",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(15 to "15 Mins", 30 to "30 Mins", 45 to "45 Mins", 60 to "60 Mins").forEach { (mins, label) ->
                        val isChosen = selectedSessionMins == mins
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) ColorGreen else SurfaceElevated)
                                .clickable { selectedSessionMins = mins }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val dep = depositLimitText.toDoubleOrNull() ?: 25000.0
                        val bet = singleBetLimitText.toDoubleOrNull() ?: 5000.0
                        val loss = dailyLossLimitText.toDoubleOrNull() ?: 10000.0
                        onSaveLimits(dep, bet, loss, selectedSessionMins)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGreen, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("save_limits_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Save Responsible Gaming Limits", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                // Self-Exclusion Section
                Text(
                    text = "Self-Exclusion (Take a Break)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Need a complete break? Lock your account from wagering for a mandatory cool-off period. This cannot be undone until the timer expires.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(24 to "24 Hours", 168 to "7 Days", 720 to "30 Days").forEach { (hrs, label) ->
                        val isChosen = selectedCoolOffHours == hrs
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) ColorRed else SurfaceElevated)
                                .clickable { selectedCoolOffHours = hrs }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showSelfExclusionConfirm = true },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorRed),
                    modifier = Modifier.fillMaxWidth().testTag("apply_self_exclusion_button")
                ) {
                    Icon(Icons.Default.LockClock, contentDescription = null, tint = ColorRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Activate Self-Exclusion (${if (selectedCoolOffHours >= 24) "${selectedCoolOffHours / 24} Days" else "$selectedCoolOffHours Hours"})",
                        color = ColorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Helpline Notice
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = ColorGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Helpline: 1800-266-0058 (National Problem Gambling Support 24x7)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    if (showSelfExclusionConfirm) {
        AlertDialog(
            onDismissRequest = { showSelfExclusionConfirm = false },
            title = { Text("Confirm Self-Exclusion", fontWeight = FontWeight.Bold, color = ColorRed) },
            text = {
                Text(
                    "Are you sure you want to pause all wagering activities for ${selectedCoolOffHours / 24} days? Once activated, this lock is strictly enforced by our compliance engine and cannot be cancelled early.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApplySelfExclusion(selectedCoolOffHours)
                        showSelfExclusionConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorRed, contentColor = Color.White)
                ) {
                    Text("Confirm & Lock", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSelfExclusionConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }
}
