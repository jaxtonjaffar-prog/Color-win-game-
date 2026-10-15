package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HeaderBar(
    balance: Double,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic rotating casino winner & platform notifications
    val notifications = remember {
        listOf(
            "🔥 User***829 just won ₹4,500 on Green (Win Go 1Min)!",
            "⚡ Provably Fair RNG: SHA-256 live verified rounds",
            "🎉 User***417 hit 9X on Lucky Number 7 (₹9,000 payout)!",
            "💎 Daily VIP Bonus: Claim your rewards every 24 hours",
            "🚀 User***105 won ₹2,700 on Violet combo (4.5X multiplier)!"
        )
    }

    var notificationIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            notificationIndex = (notificationIndex + 1) % notifications.size
        }
    }

    Surface(
        color = SurfaceDark,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Row 1: Brand & Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Logo & Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(ColorGreen, ColorViolet, ColorRed, ColorGreen)
                                )
                            )
                            .border(1.5.dp, ColorGold, CircleShape)
                    ) {
                        Text(
                            text = "CW",
                            fontWeight = FontWeight.Black,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "COLOR",
                                fontWeight = FontWeight.Black,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 16.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WIN",
                                fontWeight = FontWeight.Black,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 16.sp,
                                color = ColorGold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "PREMIUM CASINO",
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 9.sp,
                            color = TextTertiary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Quick Action Icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Daily Gift
                    IconButton(
                        onClick = onOpenDaily,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("daily_bonus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Daily Reward",
                            tint = ColorGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = if (soundEnabled) ColorGoldLight else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Rules Guide
                    IconButton(
                        onClick = onOpenRules,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("rules_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Rules Guide",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Balance Showcase with Glowing Gold border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(SurfaceElevated, SurfaceHighlight)
                        )
                    )
                    .border(1.dp, ColorGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ColorGold.copy(alpha = 0.2f))
                                .border(1.dp, ColorGold, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = ColorGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "AVAILABLE BALANCE",
                                fontSize = 10.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            AnimatedContent(
                                targetState = balance,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                                (slideOutVertically { height -> -height } + fadeOut())
                                    } else {
                                        (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                                (slideOutVertically { height -> height } + fadeOut())
                                    }
                                },
                                label = "balance_transition"
                            ) { targetBal ->
                                Text(
                                    text = "₹${"%,.2f".format(targetBal)}",
                                    fontSize = 20.sp,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = ColorGold,
                                    modifier = Modifier.testTag("wallet_balance_text")
                                )
                            }
                        }
                    }

                    // Deposit Button (Gleaming Gold button)
                    Button(
                        onClick = onOpenDeposit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("deposit_faucet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Deposit",
                            fontWeight = FontWeight.Black,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Live Notification Ticker (Marquee style notification)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundDark.copy(alpha = 0.7f))
                    .border(0.5.dp, DividerColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notification",
                        tint = ColorGold,
                        modifier = Modifier.size(16.dp)
                    )

                    AnimatedContent(
                        targetState = notificationIndex,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "notification_ticker",
                        modifier = Modifier.weight(1f)
                    ) { idx ->
                        Text(
                            text = notifications[idx],
                            fontSize = 11.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
