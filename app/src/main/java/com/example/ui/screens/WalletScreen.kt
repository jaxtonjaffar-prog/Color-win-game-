package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.DepositBonusSystem
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.example.data.model.WalletAccount
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class WalletSectionTab {
    DEPOSIT,
    WITHDRAW,
    TRANSACTIONS
}

@Composable
fun WalletScreen(
    wallet: WalletAccount?,
    profile: UserProfile?,
    transactions: List<TransactionRecord>,
    onDeposit: (method: String, amount: Double) -> Unit,
    onWithdraw: (amount: Double, method: String, destination: String) -> Unit,
    onClaimCashback: () -> Unit = {},
    onTransferCommission: (Double) -> Unit = {},
    onSelectTransaction: (TransactionRecord) -> Unit = {},
    onOpenResponsibleLimits: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(WalletSectionTab.DEPOSIT) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Balance Overview Card (Main, Bonus, Winnings)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DividerColor, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TOTAL WALLET ASSETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${"%,.2f".format(wallet?.totalBalance ?: 0.0)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = ColorGoldLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3-Way Split
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Main Balance
                        BalanceSubCard(
                            title = "Main Balance",
                            amount = wallet?.mainBalance ?: 0.0,
                            subtitle = "Deposit Funds",
                            color = ColorGold,
                            modifier = Modifier.weight(1f)
                        )

                        // Bonus Balance
                        BalanceSubCard(
                            title = "Bonus Balance",
                            amount = wallet?.bonusBalance ?: 0.0,
                            subtitle = "Promo / Referrals",
                            color = ColorViolet,
                            modifier = Modifier.weight(1f)
                        )

                        // Winnings Balance
                        BalanceSubCard(
                            title = "Winnings",
                            amount = wallet?.winningsBalance ?: 0.0,
                            subtitle = "Withdrawable",
                            color = ColorGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Monetization Perks: VIP Cashback & Referral Commission Quick Claim Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // VIP Loss Cashback Box
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "VIP Loss Cashback", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "₹${"%,.2f".format(wallet?.unclaimedCashback ?: 0.0)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onClaimCashback,
                                    enabled = (wallet?.unclaimedCashback ?: 0.0) >= 1.0,
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.fillMaxWidth().height(28.dp).testTag("wallet_claim_cashback_btn")
                                ) {
                                    Text(text = "Claim Cashback", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Referral Commission Box
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorViolet.copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Referral Commission", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "₹${"%,.2f".format(wallet?.referralCommissionBalance ?: 0.0)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorViolet
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onTransferCommission(wallet?.referralCommissionBalance ?: 0.0) },
                                    enabled = (wallet?.referralCommissionBalance ?: 0.0) >= 10.0,
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorViolet, contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.fillMaxWidth().height(28.dp).testTag("wallet_transfer_comm_btn")
                                ) {
                                    Text(text = "Transfer to Main", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Compliance & Safety Indicator Bar (18+ • TLS 1.3 • Responsible Limits)
        item {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenResponsibleLimits() }
                    .testTag("responsible_gaming_limits_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ColorGreen.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(
                                text = "Responsible Gaming Limits • 18+",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Deposit Cap: ₹${"%,.0f".format(profile?.dailyDepositLimit ?: 25000.0)} | Single Bet Cap: ₹${"%,.0f".format(profile?.singleBetLimit ?: 5000.0)}",
                                fontSize = 10.sp,
                                color = ColorGreen
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenResponsibleLimits,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(text = "Limits", fontSize = 10.sp, color = ColorGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Section Selector Tabs
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    WalletSectionTab.DEPOSIT to "Deposit",
                    WalletSectionTab.WITHDRAW to "Withdraw (Min ₹100)",
                    WalletSectionTab.TRANSACTIONS to "History"
                ).forEach { (tab, title) ->
                    val isSelected = selectedSection == tab
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ColorGold else SurfaceDark)
                            .clickable { selectedSection = tab }
                            .padding(vertical = 10.dp)
                            .testTag("wallet_tab_${tab.name}")
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                    }
                }
            }
        }

        // 3. Section Content
        when (selectedSection) {
            WalletSectionTab.DEPOSIT -> {
                item {
                    DepositView(profile = profile, onDeposit = onDeposit)
                }
            }
            WalletSectionTab.WITHDRAW -> {
                item {
                    WithdrawView(
                        winningsBalance = wallet?.winningsBalance ?: 0.0,
                        profile = profile,
                        onWithdraw = onWithdraw
                    )
                }
            }
            WalletSectionTab.TRANSACTIONS -> {
                item {
                    TransactionsView(
                        transactions = transactions,
                        onSelectTransaction = onSelectTransaction
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceSubCard(
    title: String,
    amount: Double,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "₹${"%,.0f".format(amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = TextTertiary)
        }
    }
}

@Composable
fun DepositView(
    profile: UserProfile?,
    onDeposit: (method: String, amount: Double) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var selectedAmount by remember { mutableStateOf(1000.0) }
    var customAmountText by remember { mutableStateOf("") }
    val isFirstDeposit = profile?.isFirstDepositMade != true

    val methods = listOf(
        "UPI" to "UPI (GPay / PhonePe / Paytm)",
        "Bank" to "Bank Transfer (IMPS / NEFT)",
        "Crypto" to "Crypto USDT (TRC-20)",
        "Gateway" to "Payment Gateway (Cards / NetBanking)"
    )

    val quickAmounts = listOf(100.0, 500.0, 1000.0, 2000.0, 5000.0, 10000.0)
    val calculatedBonus = DepositBonusSystem.calculateBonus(selectedAmount, isFirstDeposit)

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Deposit Promotion Banner
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ColorGold.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = ColorGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFirstDeposit) "100% FIRST DEPOSIT MATCH!" else "TIERED RELOAD BONUSES ACTIVE",
                            fontSize = 11.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Black,
                            color = ColorGoldLight
                        )
                        Text(
                            text = if (isFirstDeposit) "Get 100% matched bonus up to ₹5,000 on your first deposit!" else "Deposit ₹500-₹999 (+10%) | ₹1,000+ (+20%) | ₹3,000+ (+35%) | ₹10,000+ (+50%)",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "1. Select Payment Method",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGoldLight
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                methods.forEach { (code, label) ->
                    val isChosen = selectedMethod == code
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChosen) SurfaceHighlight else SurfaceElevated)
                            .border(1.dp, if (isChosen) ColorGold else DividerColor, RoundedCornerShape(8.dp))
                            .clickable { selectedMethod = code }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        RadioButton(
                            selected = isChosen,
                            onClick = { selectedMethod = code },
                            colors = RadioButtonDefaults.colors(selectedColor = ColorGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isChosen) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Select Deposit Amount & Bonus",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGoldLight
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickAmounts.take(3).forEach { amt ->
                    val bonusAmt = DepositBonusSystem.calculateBonus(amt, isFirstDeposit)
                    AmountPillWithBonus(
                        amount = amt,
                        bonus = bonusAmt,
                        isSelected = selectedAmount == amt && customAmountText.isEmpty(),
                        onClick = {
                            selectedAmount = amt
                            customAmountText = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickAmounts.drop(3).forEach { amt ->
                    val bonusAmt = DepositBonusSystem.calculateBonus(amt, isFirstDeposit)
                    AmountPillWithBonus(
                        amount = amt,
                        bonus = bonusAmt,
                        isSelected = selectedAmount == amt && customAmountText.isEmpty(),
                        onClick = {
                            selectedAmount = amt
                            customAmountText = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = customAmountText,
                onValueChange = {
                    customAmountText = it
                    it.toDoubleOrNull()?.let { valid -> selectedAmount = valid }
                },
                label = { Text("Or Custom Deposit Amount (₹)") },
                placeholder = { Text("e.g. 750") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorGold,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bonus Breakdown Box
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Deposit: ₹${selectedAmount.toInt()} + Bonus: ₹${calculatedBonus.toInt()}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Playable Balance: ₹${(selectedAmount + calculatedBonus).toInt()}",
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Black,
                            color = ColorGreen
                        )
                    }

                    if (calculatedBonus > 0) {
                        Surface(
                            color = ColorGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+₹${calculatedBonus.toInt()} FREE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onDeposit(selectedMethod, selectedAmount) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorGold,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_deposit_button")
            ) {
                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Deposit ₹${selectedAmount.toInt()} (+₹${calculatedBonus.toInt()} Bonus)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun AmountPillWithBonus(
    amount: Double,
    bonus: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ColorGold else SurfaceElevated)
            .border(1.dp, if (isSelected) ColorGoldLight else DividerColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "₹${amount.toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else TextPrimary
            )
            if (bonus > 0) {
                Text(
                    text = "+₹${bonus.toInt()}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color.Black.copy(alpha = 0.75f) else ColorGreen
                )
            }
        }
    }
}

@Composable
fun AmountPill(
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ColorGold else SurfaceElevated)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "₹${amount.toInt()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else TextPrimary
        )
    }
}

@Composable
fun WithdrawView(
    winningsBalance: Double,
    profile: UserProfile?,
    onWithdraw: (amount: Double, method: String, destination: String) -> Unit
) {
    var amountText by remember { mutableStateOf("100") }
    var payoutChannel by remember { mutableStateOf("UPI") }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Notice Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceHighlight)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ColorGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Withdrawal Guidelines",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Minimum withdrawal: ₹100\n• Only Winnings Balance is withdrawable\n• Payouts processed within 24 hours to verified accounts",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Available Winnings: ₹${"%,.2f".format(winningsBalance)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Selector
            Text(
                text = "Select Withdrawal Destination",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                val upiLabel = profile?.upiId ?: "player@oksbi"
                val bankLabel = "Bank: ...${profile?.bankAccountNumber?.takeLast(4) ?: "1001"}"

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (payoutChannel == "UPI") SurfaceHighlight else SurfaceElevated)
                        .border(1.dp, if (payoutChannel == "UPI") ColorGold else DividerColor, RoundedCornerShape(8.dp))
                        .clickable { payoutChannel = "UPI" }
                        .padding(vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "UPI ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (payoutChannel == "UPI") ColorGold else TextPrimary)
                        Text(text = upiLabel, fontSize = 10.sp, color = TextTertiary)
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (payoutChannel == "BANK") SurfaceHighlight else SurfaceElevated)
                        .border(1.dp, if (payoutChannel == "BANK") ColorGold else DividerColor, RoundedCornerShape(8.dp))
                        .clickable { payoutChannel = "BANK" }
                        .padding(vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Bank Account", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (payoutChannel == "BANK") ColorGold else TextPrimary)
                        Text(text = bankLabel, fontSize = 10.sp, color = TextTertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Withdrawal Amount (Min ₹100)") },
                placeholder = { Text("e.g. 500") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorGold,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_amount_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
            val isHighValueAml = enteredAmount > 10000.0
            val isAmlCleared = profile?.isAmlVerified == true

            if (isHighValueAml) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = if (isAmlCleared) ColorGreen.copy(alpha = 0.15f) else ColorGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isAmlCleared) ColorGreen.copy(alpha = 0.5f) else ColorGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("aml_tier2_notice")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isAmlCleared) Icons.Default.VerifiedUser else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isAmlCleared) ColorGreen else ColorGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isAmlCleared) "AML Tier-2 Verified (> ₹10,000)" else "AML / KYC Compliance (> ₹10,000)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAmlCleared) ColorGreen else ColorGold
                            )
                            Text(
                                text = if (isAmlCleared)
                                    "Verified PAN & Aadhaar details cleared for unlimited high-value payouts."
                                else
                                    "Anti-Money Laundering (PMLA) policy requires Tier-2 KYC for withdrawals over ₹10,000.",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isValidAmount = enteredAmount >= 100.0 && enteredAmount <= winningsBalance

            Button(
                onClick = {
                    val dest = if (payoutChannel == "UPI") profile?.upiId ?: "player@oksbi" else profile?.bankAccountNumber ?: "987654321001"
                    onWithdraw(enteredAmount, payoutChannel, dest)
                },
                enabled = isValidAmount,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_withdraw_button")
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Request Withdrawal (₹${enteredAmount.toInt()})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun TransactionsView(
    transactions: List<TransactionRecord>,
    onSelectTransaction: (TransactionRecord) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredList = when (selectedCategory) {
        "DEPOSIT" -> transactions.filter { it.category == "DEPOSIT" }
        "WITHDRAWAL" -> transactions.filter { it.category == "WITHDRAWAL" }
        "BET" -> transactions.filter { it.category == "BET" }
        "WIN" -> transactions.filter { it.category == "WIN" }
        "BONUS" -> transactions.filter { it.category == "BONUS" }
        else -> transactions
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category filter pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "ALL" to "All",
                    "DEPOSIT" to "Deposit",
                    "WITHDRAWAL" to "Withdraw",
                    "BET" to "Bets",
                    "WIN" to "Wins",
                    "BONUS" to "Bonus"
                ).forEach { (cat, label) ->
                    val isSel = selectedCategory == cat
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) ColorGold else SurfaceElevated)
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(text = "No records in this category", color = TextTertiary, fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredList.take(25).forEach { tx ->
                        TransactionRowItem(tx = tx, onClick = { onSelectTransaction(tx) })
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    tx: TransactionRecord,
    onClick: () -> Unit = {}
) {
    val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val isCredit = tx.amount >= 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("txn_row_${tx.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isCredit) ColorGreen.copy(alpha = 0.2f) else ColorRed.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isCredit) ColorGreen else ColorRed,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column {
                Text(
                    text = tx.description,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = timeFormat.format(Date(tx.timestamp)),
                        fontSize = 9.sp,
                        color = TextTertiary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = "Encrypted", tint = ColorGreen, modifier = Modifier.size(10.dp))
                        Text(text = "JWT Signed", fontSize = 8.sp, color = ColorGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isCredit) "+" else ""}₹${"%,.2f".format(tx.amount)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) ColorGreen else ColorRed
            )
            Surface(
                color = if (tx.status == "COMPLETED") SurfaceHighlight else ColorGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = tx.status,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.status == "COMPLETED") TextTertiary else ColorGold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
