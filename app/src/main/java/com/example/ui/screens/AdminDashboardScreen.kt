package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.*
import com.example.ui.theme.*

enum class AdminTab(val title: String) {
    FINANCIALS("Financials"),
    RNG_CONTROL("RNG & Draws"),
    USERS("Users"),
    WITHDRAWALS("Withdrawals"),
    SETTINGS("Game Settings")
}

@Composable
fun AdminDashboardScreen(
    wallet: WalletAccount?,
    profile: UserProfile?,
    settings: GameSettings,
    allBets: List<UserBet>,
    transactions: List<TransactionRecord>,
    pendingWithdrawals: List<TransactionRecord>,
    onUpdateSettings: (GameSettings) -> Unit,
    onApproveWithdrawal: (Long) -> Unit,
    onRejectWithdrawal: (Long, Double) -> Unit,
    onAdjustBalance: (main: Double, bonus: Double, win: Double) -> Unit,
    onToggleKyc: (Boolean) -> Unit,
    onToggleFreeze: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    var selectedTab by remember { mutableStateOf(AdminTab.FINANCIALS) }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceDark,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColorRed.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = ColorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ADMIN CONSOLE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = ColorRed,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Platform Governance & House Controls",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceHighlight,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_exit_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Exit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = BackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Admin Sub-Nav Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = SurfaceDark,
                contentColor = ColorGold,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdminTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ColorGold else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_${tab.name}")
                    )
                }
            }

            // Tab Content
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    AdminTab.FINANCIALS -> {
                        item {
                            AdminFinancialsView(
                                allBets = allBets,
                                transactions = transactions
                            )
                        }
                    }
                    AdminTab.RNG_CONTROL -> {
                        item {
                            AdminRngControlView(
                                settings = settings,
                                onUpdateSettings = onUpdateSettings
                            )
                        }
                    }
                    AdminTab.USERS -> {
                        item {
                            AdminUserManagementView(
                                profile = profile,
                                wallet = wallet,
                                onAdjustBalance = onAdjustBalance,
                                onToggleKyc = onToggleKyc,
                                onToggleFreeze = onToggleFreeze
                            )
                        }
                    }
                    AdminTab.WITHDRAWALS -> {
                        item {
                            AdminWithdrawalsQueueView(
                                pendingWithdrawals = pendingWithdrawals,
                                onApprove = onApproveWithdrawal,
                                onReject = onRejectWithdrawal
                            )
                        }
                    }
                    AdminTab.SETTINGS -> {
                        item {
                            AdminGameSettingsView(
                                settings = settings,
                                onSaveSettings = onUpdateSettings
                            )
                        }
                    }
                }
            }
        }
    }
}

// 1. FINANCIALS & REPORTS
@Composable
fun AdminFinancialsView(
    allBets: List<UserBet>,
    transactions: List<TransactionRecord>
) {
    val totalTurnover = allBets.sumOf { it.totalAmount }
    val totalWonPayout = allBets.filter { it.status == BetStatus.WON }.sumOf { it.winAmount }
    val ggr = totalTurnover - totalWonPayout // Gross Gaming Revenue
    val profitMargin = if (totalTurnover > 0) (ggr / totalTurnover * 100) else 0.0

    val depositsTotal = transactions.filter { it.category == "DEPOSIT" }.sumOf { kotlin.math.abs(it.amount) }
    val withdrawalsTotal = transactions.filter { it.category == "WITHDRAWAL" && it.status == "COMPLETED" }.sumOf { kotlin.math.abs(it.amount) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FINANCIAL PERFORMANCE & GGR REPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGoldLight,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(title = "Total Wagered", value = "₹${"%,.0f".format(totalTurnover)}", color = ColorGold, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Total Paid Out", value = "₹${"%,.0f".format(totalWonPayout)}", color = ColorRed, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(title = "Gross Revenue (GGR)", value = "₹${"%,.0f".format(ggr)}", color = ColorGreen, modifier = Modifier.weight(1f))
                AdminStatCard(title = "House Margin", value = "${"%,.1f".format(profitMargin)}%", color = ColorBlue, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cashflow Overview",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(title = "Total Deposits", value = "₹${"%,.0f".format(depositsTotal)}", color = ColorGreen, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Completed Payouts", value = "₹${"%,.0f".format(withdrawalsTotal)}", color = ColorGold, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// 2. RNG & BET MANIPULATION CONTROL
@Composable
fun AdminRngControlView(
    settings: GameSettings,
    onUpdateSettings: (GameSettings) -> Unit
) {
    var selectedRngMode by remember(settings) { mutableStateOf(settings.rngControlMode) }
    var manualNum by remember(settings) { mutableStateOf(settings.manualOverrideNumber) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "BET MANIPULATION & RNG DRAW CONTROLLER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorRed,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Control draw outcomes to manage house liquidity and risk exposure in real time.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector
            val modes = listOf(
                "FAIR_RNG" to "100% Fair Random RNG (Standard)",
                "LEAST_PAYOUT" to "Smart Least-Payout (Maximizes House Profit)",
                "MANUAL" to "Manual Outcome Preset (Forced Ball)"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEach { (key, label) ->
                    val isChosen = selectedRngMode == key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChosen) SurfaceHighlight else SurfaceElevated)
                            .border(1.dp, if (isChosen) ColorRed else DividerColor, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedRngMode = key
                                onUpdateSettings(settings.copy(rngControlMode = key))
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = isChosen,
                            onClick = {
                                selectedRngMode = key
                                onUpdateSettings(settings.copy(rngControlMode = key))
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = ColorRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChosen) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            if (selectedRngMode == "MANUAL") {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Select Next Winning Ball Preset (0 - 9):",
                    fontSize = 11.sp,
                    color = ColorGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    (0..4).forEach { n ->
                        val isPicked = manualNum == n
                        Button(
                            onClick = {
                                manualNum = n
                                onUpdateSettings(settings.copy(manualOverrideNumber = n))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPicked) ColorRed else SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text(text = "$n", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    (5..9).forEach { n ->
                        val isPicked = manualNum == n
                        Button(
                            onClick = {
                                manualNum = n
                                onUpdateSettings(settings.copy(manualOverrideNumber = n))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPicked) ColorRed else SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text(text = "$n", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 3. USER MANAGEMENT
@Composable
fun AdminUserManagementView(
    profile: UserProfile?,
    wallet: WalletAccount?,
    onAdjustBalance: (main: Double, bonus: Double, win: Double) -> Unit,
    onToggleKyc: (Boolean) -> Unit,
    onToggleFreeze: (Boolean) -> Unit
) {
    var mainText by remember { mutableStateOf(wallet?.mainBalance?.toInt()?.toString() ?: "1200") }
    var bonusText by remember { mutableStateOf(wallet?.bonusBalance?.toInt()?.toString() ?: "500") }
    var winText by remember { mutableStateOf(wallet?.winningsBalance?.toInt()?.toString() ?: "300") }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PLAYER IDENTITY & ACCOUNT GOVERNANCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGoldLight,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // User Info Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "UID: ${profile?.userId}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                        Text(text = "VIP Level ${profile?.vipLevel}", fontWeight = FontWeight.Bold, color = ColorGold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Phone/Email: ${profile?.phoneOrEmail}", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "PAN: ${profile?.kycPan} | Aadhaar: ${profile?.kycAadhaar}", fontSize = 11.sp, color = TextTertiary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Toggles: Freeze Account & KYC
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = "Freeze Player Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "Blocks all wagers and withdrawals immediately", fontSize = 10.sp, color = TextTertiary)
                }
                Switch(
                    checked = profile?.isFrozen == true,
                    onCheckedChange = { onToggleFreeze(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorRed)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = "KYC Verification Approval", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "Marks identity documents as verified", fontSize = 10.sp, color = TextTertiary)
                }
                Switch(
                    checked = profile?.kycVerified == true,
                    onCheckedChange = { onToggleKyc(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorGreen)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Adjust User Balances Directly", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorGoldLight)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = mainText, onValueChange = { mainText = it }, label = { Text("Main (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = bonusText, onValueChange = { bonusText = it }, label = { Text("Bonus (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = winText, onValueChange = { winText = it }, label = { Text("Winnings (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val m = mainText.toDoubleOrNull() ?: 1200.0
                    val b = bonusText.toDoubleOrNull() ?: 500.0
                    val w = winText.toDoubleOrNull() ?: 300.0
                    onAdjustBalance(m, b, w)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Commit Balance Adjustment", fontSize = 12.sp, color = ColorGold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. WITHDRAWAL APPROVALS QUEUE
@Composable
fun AdminWithdrawalsQueueView(
    pendingWithdrawals: List<TransactionRecord>,
    onApprove: (Long) -> Unit,
    onReject: (Long, Double) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PENDING WITHDRAWAL AUTHORIZATION QUEUE (${pendingWithdrawals.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Verify player eligibility and authorize or refund pending cashouts.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (pendingWithdrawals.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(text = "All withdrawal requests have been processed", color = TextTertiary, fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingWithdrawals.forEach { tx ->
                        val absAmount = kotlin.math.abs(tx.amount)
                        Box(
                            modifier = Modifier
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
                                    Text(text = "Request #$tx.id", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "₹${absAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = tx.description, fontSize = 10.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onApprove(tx.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "Approve & Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onReject(tx.id, absAmount) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "Reject & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. GAME SETTINGS & PAYOUT CONFIGURATION
@Composable
fun AdminGameSettingsView(
    settings: GameSettings,
    onSaveSettings: (GameSettings) -> Unit
) {
    var feeText by remember(settings) { mutableStateOf(settings.platformFeePercent.toString()) }
    var greenMulText by remember(settings) { mutableStateOf(settings.greenMultiplier.toString()) }
    var redMulText by remember(settings) { mutableStateOf(settings.redMultiplier.toString()) }
    var violetMulText by remember(settings) { mutableStateOf(settings.violetMultiplier.toString()) }
    var comboMulText by remember(settings) { mutableStateOf(settings.comboMultiplier.toString()) }
    var numMulText by remember(settings) { mutableStateOf(settings.numberMultiplier.toString()) }
    var comboNumMulText by remember(settings) { mutableStateOf(settings.comboNumberMultiplier.toString()) }
    var sizeMulText by remember(settings) { mutableStateOf(settings.sizeMultiplier.toString()) }
    var minWithdrawText by remember(settings) { mutableStateOf(settings.minWithdrawal.toString()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "GAME SETTINGS & PAYOUT CONFIGURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGoldLight,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Platform Fee & Min Withdrawal
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Platform Fee (%)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minWithdrawText,
                    onValueChange = { minWithdrawText = it },
                    label = { Text("Min Withdrawal (₹)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Payout Multipliers Matrix", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = greenMulText, onValueChange = { greenMulText = it }, label = { Text("Green (X)") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = redMulText, onValueChange = { redMulText = it }, label = { Text("Red (X)") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = violetMulText, onValueChange = { violetMulText = it }, label = { Text("Violet (X)") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = comboMulText, onValueChange = { comboMulText = it }, label = { Text("Combos (X)") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = numMulText, onValueChange = { numMulText = it }, label = { Text("Number (X)") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = comboNumMulText, onValueChange = { comboNumMulText = it }, label = { Text("0 & 5 (X)") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = sizeMulText,
                onValueChange = { sizeMulText = it },
                label = { Text("Big / Small Multiplier (X)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val updated = settings.copy(
                        platformFeePercent = feeText.toDoubleOrNull() ?: 2.0,
                        greenMultiplier = greenMulText.toDoubleOrNull() ?: 2.0,
                        redMultiplier = redMulText.toDoubleOrNull() ?: 2.0,
                        violetMultiplier = violetMulText.toDoubleOrNull() ?: 4.5,
                        comboMultiplier = comboMulText.toDoubleOrNull() ?: 1.5,
                        numberMultiplier = numMulText.toDoubleOrNull() ?: 9.0,
                        comboNumberMultiplier = comboNumMulText.toDoubleOrNull() ?: 9.5,
                        sizeMultiplier = sizeMulText.toDoubleOrNull() ?: 2.0,
                        minWithdrawal = minWithdrawText.toDoubleOrNull() ?: 100.0
                    )
                    onSaveSettings(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorGold, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Save Game & Payout Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
