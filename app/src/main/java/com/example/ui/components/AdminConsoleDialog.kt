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
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.example.data.model.WalletAccount
import com.example.ui.theme.*

@Composable
fun AdminConsoleDialog(
    forcedNumber: Int?,
    pendingWithdrawals: List<TransactionRecord>,
    wallet: WalletAccount?,
    profile: UserProfile?,
    onSetForcedNumber: (Int?) -> Unit,
    onApproveWithdrawal: (Long) -> Unit,
    onRejectWithdrawal: (Long, Double) -> Unit,
    onAdjustBalance: (main: Double, bonus: Double, win: Double) -> Unit,
    onToggleKyc: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var adminMainBalText by remember { mutableStateOf(wallet?.mainBalance?.toInt()?.toString() ?: "1200") }
    var adminBonusBalText by remember { mutableStateOf(wallet?.bonusBalance?.toInt()?.toString() ?: "500") }
    var adminWinBalText by remember { mutableStateOf(wallet?.winningsBalance?.toInt()?.toString() ?: "300") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, ColorRed, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColorRed.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ColorRed, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = "ADMIN OPERATOR CONSOLE", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ColorRed)
                            Text(text = "Game Master & Financial Controls", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: Manual Draw Override
                Text(
                    text = "1. Game Outcome Override (Live Draw Control)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (forcedNumber != null) "Active Override: Next round result will be Number $forcedNumber" else "Mode: Automatic Random RNG",
                    fontSize = 11.sp,
                    color = if (forcedNumber != null) ColorGreen else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Number buttons 0-9 for manual preset
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (0..4).forEach { num ->
                        val isPicked = forcedNumber == num
                        Button(
                            onClick = { onSetForcedNumber(if (isPicked) null else num) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPicked) ColorRed else SurfaceElevated,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("admin_force_$num")
                        ) {
                            Text(text = "$num", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (5..9).forEach { num ->
                        val isPicked = forcedNumber == num
                        Button(
                            onClick = { onSetForcedNumber(if (isPicked) null else num) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPicked) ColorRed else SurfaceElevated,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("admin_force_$num")
                        ) {
                            Text(text = "$num", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                if (forcedNumber != null) {
                    OutlinedButton(
                        onClick = { onSetForcedNumber(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Reset to Fair Random RNG", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 2: Pending Withdrawal Approvals Queue
                Text(
                    text = "2. Pending Withdrawals Queue (${pendingWithdrawals.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (pendingWithdrawals.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Text(text = "No pending withdrawal requests", fontSize = 11.sp, color = TextTertiary)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pendingWithdrawals.forEach { tx ->
                            val absAmount = kotlin.math.abs(tx.amount)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Request #$tx.id: ₹${absAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorGreen)
                                        Text(text = tx.description.take(35) + "...", fontSize = 9.sp, color = TextTertiary)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { onApproveWithdrawal(tx.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(text = "Approve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { onRejectWithdrawal(tx.id, absAmount) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorRed),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(text = "Reject", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 3: User Balance Adjustment Station
                Text(
                    text = "3. Quick Wallet Balance Override",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = adminMainBalText,
                        onValueChange = { adminMainBalText = it },
                        label = { Text("Main (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = adminBonusBalText,
                        onValueChange = { adminBonusBalText = it },
                        label = { Text("Bonus (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = adminWinBalText,
                        onValueChange = { adminWinBalText = it },
                        label = { Text("Winnings (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val m = adminMainBalText.toDoubleOrNull() ?: 1200.0
                        val b = adminBonusBalText.toDoubleOrNull() ?: 500.0
                        val w = adminWinBalText.toDoubleOrNull() ?: 300.0
                        onAdjustBalance(m, b, w)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Apply Balance Override", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorGold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 4: KYC Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "KYC Verification Status", fontSize = 12.sp, color = TextPrimary)
                    Switch(
                        checked = profile?.kycVerified == true,
                        onCheckedChange = { onToggleKyc(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColorGreen)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Close Admin Console", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
