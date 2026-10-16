package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.model.VipSystem
import com.example.data.model.WalletAccount
import com.example.ui.theme.*

@Composable
fun VipClubDialog(
    profile: UserProfile?,
    wallet: WalletAccount?,
    onClaimCashback: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevel = profile?.vipLevel ?: 1
    val currentXp = profile?.vipXp ?: 450
    val currentTier = VipSystem.getTier(currentLevel)
    val nextTier = VipSystem.getNextTier(currentLevel)
    val unclaimedCashback = wallet?.unclaimedCashback ?: 0.0
    var showHouseEdgeDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(ColorGold, ColorGoldDark)))
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "VIP Club & Cashback",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Earn up to 12% Loss Cashback Daily",
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily,
                                color = ColorGoldLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_vip_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Current Tier & Cashback Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = Color(currentTier.colorHex),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "VIP ${currentTier.level} ${currentTier.name.uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${currentTier.cashbackPercent}% Cashback Rate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // XP Progress
                        val reqXp = nextTier?.requiredXp ?: (currentTier.requiredXp + 1000)
                        val progress = (currentXp.toFloat() / reqXp.toFloat()).coerceIn(0f, 1f)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Tier Progress", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "$currentXp / $reqXp XP", fontSize = 11.sp, color = ColorGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = ColorGold,
                            trackColor = DividerColor,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wager ₹10 in any game = 1 VIP XP gained",
                            fontSize = 9.sp,
                            color = TextTertiary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Unclaimed Cashback Claim Box
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Unclaimed Loss Cashback",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "₹${"%,.2f".format(unclaimedCashback)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ColorGreen
                                )
                            }

                            Button(
                                onClick = onClaimCashback,
                                enabled = unclaimedCashback >= 1.0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("claim_cashback_button")
                            ) {
                                Text(
                                    text = if (unclaimedCashback >= 1.0) "Claim Cashback" else "Min ₹1 to Claim",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // House edge quick info trigger
                OutlinedButton(
                    onClick = { showHouseEdgeDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View 10% House Edge & Payout Math",
                        fontSize = 11.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // All 10 Tiers List
                Text(
                    text = "ALL VIP TIERS & BENEFITS",
                    fontSize = 11.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = ColorGold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VipSystem.TIERS.forEach { tier ->
                        val isUserTier = tier.level == currentLevel
                        Surface(
                            color = if (isUserTier) ColorGold.copy(alpha = 0.15f) else SurfaceCard,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isUserTier) ColorGold else DividerColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = Color(tier.colorHex),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "V${tier.level}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = tier.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUserTier) ColorGold else TextPrimary
                                        )
                                        Text(
                                            text = "${tier.requiredXp} XP",
                                            fontSize = 9.sp,
                                            color = TextTertiary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${tier.cashbackPercent}% Cashback",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorGreen
                                    )
                                    Text(
                                        text = "${tier.referralCommissionRate}% Commission",
                                        fontSize = 9.sp,
                                        color = ColorViolet
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHouseEdgeDialog) {
        HouseEdgeInfoDialog(onDismiss = { showHouseEdgeDialog = false })
    }
}
