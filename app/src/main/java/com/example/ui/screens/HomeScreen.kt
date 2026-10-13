package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

enum class MainNavTab(val title: String) {
    WINGO("Win Go"),
    TRENDS("Trends"),
    WALLET("Wallet"),
    PROFILE("Profile")
}

enum class GameSubTab(val title: String) {
    GAME_RECORD("Game Record"),
    TREND_CHART("Chart"),
    MY_BETS("My Bets")
}

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val periodId by viewModel.currentPeriodId.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val isLocked by viewModel.isBettingLocked.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val recentRounds by viewModel.recentRounds.collectAsStateWithLifecycle()
    val allBets by viewModel.allBets.collectAsStateWithLifecycle()
    val activeRoundBets by viewModel.activeRoundUserBets.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val selectedBetTarget by viewModel.selectedBetTarget.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val forcedNumber by viewModel.forcedResult.collectAsStateWithLifecycle()
    val pendingWithdrawals by viewModel.pendingWithdrawals.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()

    var currentNavTab by remember { mutableStateOf(MainNavTab.WINGO) }
    var currentGameSubTab by remember { mutableStateOf(GameSubTab.GAME_RECORD) }

    var showWalletDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messageSnackbar.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HeaderBar(
                balance = wallet?.totalBalance ?: 0.0,
                soundEnabled = soundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onOpenDeposit = { currentNavTab = MainNavTab.WALLET },
                onOpenDaily = { viewModel.claimDailyBonus() },
                onOpenRules = { showRulesDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                listOf(
                    MainNavTab.WINGO to Icons.Default.Casino,
                    MainNavTab.TRENDS to Icons.Default.TrendingUp,
                    MainNavTab.WALLET to Icons.Default.AccountBalanceWallet,
                    MainNavTab.PROFILE to Icons.Default.Person
                ).forEach { (tab, icon) ->
                    val isSelected = currentNavTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentNavTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) ColorGold else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ColorGold else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ColorGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name}")
                    )
                }
            }
        },
        containerColor = BackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNavTab) {
                MainNavTab.WINGO -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 10.dp,
                            bottom = 20.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 640.dp)
                    ) {
                        // 1. Period and Timer Card
                        item {
                            PeriodTimerCard(
                                selectedMode = selectedMode,
                                onSelectMode = { viewModel.selectMode(it) },
                                periodId = periodId,
                                remainingSeconds = remainingSeconds,
                                isLocked = isLocked,
                                recentRounds = recentRounds
                            )
                        }

                        // 2. Active Wagers + Quick Lucky / Repeat Bet Bar
                        item {
                            ActiveWagersBar(
                                activeBets = activeRoundBets,
                                isLocked = isLocked,
                                onRandomBet = { viewModel.placeRandomBet(100.0) },
                                onRepeatBet = { viewModel.repeatLastBet() }
                            )
                        }

                        // 3. Bet Action Selection Grid (Green/Red/Violet, Combos, 0-9, Big/Small)
                        item {
                            BetActionGrid(
                                isLocked = isLocked,
                                onSelectTarget = { viewModel.openBetSheet(it) }
                            )
                        }

                        // 4. Game Sub-Tab Selector
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                GameSubTab.values().forEach { subTab ->
                                    val isSelected = currentGameSubTab == subTab
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) ColorGold else SurfaceDark)
                                            .clickable { currentGameSubTab = subTab }
                                            .padding(vertical = 10.dp)
                                            .testTag("subtab_${subTab.name}")
                                    ) {
                                        Text(
                                            text = subTab.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // 5. Game Sub-Tab Content
                        item {
                            when (currentGameSubTab) {
                                GameSubTab.GAME_RECORD -> {
                                    GameRecordTable(rounds = recentRounds)
                                }
                                GameSubTab.TREND_CHART -> {
                                    TrendChartScreen(rounds = recentRounds)
                                }
                                GameSubTab.MY_BETS -> {
                                    MyBetsScreen(
                                        bets = allBets,
                                        onRebet = { bet -> viewModel.openBetSheet(bet.target) }
                                    )
                                }
                            }
                        }
                    }
                }

                MainNavTab.TRENDS -> {
                    TrendsScreen(rounds = recentRounds)
                }

                MainNavTab.WALLET -> {
                    WalletScreen(
                        wallet = wallet,
                        profile = profile,
                        transactions = allTransactions,
                        onDeposit = { method, amt -> viewModel.deposit(method, amt) },
                        onWithdraw = { amt, method, dest -> viewModel.withdraw(amt, method, dest) }
                    )
                }

                MainNavTab.PROFILE -> {
                    ProfileScreen(
                        profile = profile,
                        onOpenAuth = { showAuthDialog = true },
                        onSubmitKyc = { pan, aadhaar -> viewModel.submitKyc(pan, aadhaar) },
                        onLinkBank = { acc, ifsc, holder, upi -> viewModel.linkBank(acc, ifsc, holder, upi) },
                        onClaimVipBonus = { viewModel.claimDailyBonus() },
                        onOpenAdmin = { showAdminDialog = true }
                    )
                }
            }
        }
    }

    // Bet Sheet
    selectedBetTarget?.let { target ->
        BetBottomSheet(
            target = target,
            currentBalance = wallet?.totalBalance ?: 0.0,
            onDismiss = { viewModel.closeBetSheet() },
            onConfirmBet = { chip, mul -> viewModel.placeBet(chip, mul) }
        )
    }

    // Win / Draw Celebration Dialog
    celebration?.let { cel ->
        DrawCelebrationDialog(
            celebration = cel,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }

    // Auth Dialog (Phone/Email + OTP + Referral Code)
    if (showAuthDialog) {
        AuthDialog(
            onLogin = { phoneOrEmail, referralCode ->
                viewModel.login(phoneOrEmail, referralCode)
            },
            onGuestBrowse = {
                viewModel.setGuestMode()
            },
            onDismiss = { showAuthDialog = false }
        )
    }

    // Wallet Quick Deposit Dialog
    if (showWalletDialog) {
        WalletDialog(
            wallet = wallet,
            onTopUp = { viewModel.deposit("Quick TopUp", it) },
            onClaimDaily = { viewModel.claimDailyBonus() },
            onDismiss = { showWalletDialog = false }
        )
    }

    // Rules Guide Dialog
    if (showRulesDialog) {
        RulesGuideDialog(onDismiss = { showRulesDialog = false })
    }

    // Admin & Operator Console Dialog
    if (showAdminDialog) {
        AdminConsoleDialog(
            forcedNumber = forcedNumber,
            pendingWithdrawals = pendingWithdrawals,
            wallet = wallet,
            profile = profile,
            onSetForcedNumber = { viewModel.overrideNextResult(it) },
            onApproveWithdrawal = { viewModel.approveWithdrawal(it) },
            onRejectWithdrawal = { id, amt -> viewModel.rejectWithdrawal(id, amt) },
            onAdjustBalance = { m, b, w -> viewModel.adminSetBalance(m, b, w) },
            onToggleKyc = { viewModel.adminToggleKyc(it) },
            onDismiss = { showAdminDialog = false }
        )
    }
}
