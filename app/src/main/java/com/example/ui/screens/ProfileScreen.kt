package com.example.ui.screens

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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    onOpenAuth: () -> Unit,
    onSubmitKyc: (pan: String, aadhaar: String) -> Unit,
    onLinkBank: (account: String, ifsc: String, holder: String, upi: String) -> Unit,
    onClaimVipBonus: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showKycDialog by remember { mutableStateOf(false) }
    var showBankDialog by remember { mutableStateOf(false) }
    var copiedNotice by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. User Header & Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DividerColor, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ColorGold.copy(alpha = 0.2f))
                                    .border(2.dp, ColorGold, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ColorGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (profile?.isLoggedIn == true) profile.phoneOrEmail else "Guest Player",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        color = ColorGold,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "VIP ${profile?.vipLevel ?: 1}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "UID: ${profile?.userId ?: "CW_GUEST"}",
                                    fontSize = 11.sp,
                                    color = TextTertiary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAuth,
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (profile?.isLoggedIn == true) "Switch" else "Login",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // VIP XP Progression Bar
                    val currentXp = profile?.vipXp ?: 450
                    val nextTierXp = (profile?.vipLevel ?: 1) * 500
                    val progress = (currentXp.toFloat() / nextTierXp.toFloat()).coerceIn(0f, 1f)

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "VIP Level Progression",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$currentXp / $nextTierXp XP",
                                fontSize = 11.sp,
                                color = ColorGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ColorGold,
                            trackColor = DividerColor
                        )
                    }
                }
            }
        }

        // Operator & Admin Mode Banner
        item {
            Button(
                onClick = onOpenAdmin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceElevated,
                    contentColor = ColorRed
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, ColorRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .testTag("open_admin_console_button")
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ColorRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Operator & Admin Control Panel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. KYC Verification Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ColorGreen)
                            Text(
                                text = "KYC Verification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            color = if (profile?.kycVerified == true) ColorGreen.copy(alpha = 0.2f) else ColorGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (profile?.kycVerified == true) "VERIFIED" else "PENDING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile?.kycVerified == true) ColorGreen else ColorGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PAN: ${profile?.kycPan ?: "Not submitted"} | Aadhaar: ${profile?.kycAadhaar ?: "Not submitted"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showKycDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (profile?.kycVerified == true) "Update KYC Documents" else "Complete KYC Verification",
                            fontSize = 12.sp,
                            color = ColorGold
                        )
                    }
                }
            }
        }

        // 3. Bank Account & UPI Linking Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ColorBlue)
                            Text(
                                text = "Bank Account & UPI",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            color = ColorBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "LINKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "A/C: ${profile?.bankAccountNumber ?: "None"} (${profile?.bankIfsc ?: "IFSC"})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "UPI ID: ${profile?.upiId ?: "None"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showBankDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Edit Bank & UPI Details", fontSize = 12.sp, color = ColorBlue)
                    }
                }
            }
        }

        // 4. Referral Program & Earnings Hub
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = ColorViolet)
                        Text(
                            text = "Referral Program & Earnings",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Total Referrals", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = "${profile?.referralsCount ?: 0} Players",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorViolet
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Commission Earned", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = "₹${"%,.0f".format(profile?.referralEarnings ?: 0.0)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Personal Invite Code Copy Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceHighlight)
                            .border(1.dp, ColorViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(text = "Your Referral Code", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = profile?.referralCode ?: "WIN7789",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ColorGoldLight,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(profile?.referralCode ?: "WIN7789"))
                                    copiedNotice = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorViolet)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (copiedNotice) "Copied!" else "Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // KYC Dialog
    if (showKycDialog) {
        KycSubmitDialog(
            currentPan = profile?.kycPan ?: "",
            currentAadhaar = profile?.kycAadhaar ?: "",
            onSubmit = { pan, aadhaar ->
                onSubmitKyc(pan, aadhaar)
                showKycDialog = false
            },
            onDismiss = { showKycDialog = false }
        )
    }

    // Bank Linking Dialog
    if (showBankDialog) {
        BankLinkDialog(
            currentAccount = profile?.bankAccountNumber ?: "",
            currentIfsc = profile?.bankIfsc ?: "",
            currentHolder = profile?.bankHolderName ?: "",
            currentUpi = profile?.upiId ?: "",
            onSave = { acc, ifsc, holder, upi ->
                onLinkBank(acc, ifsc, holder, upi)
                showBankDialog = false
            },
            onDismiss = { showBankDialog = false }
        )
    }
}

@Composable
fun KycSubmitDialog(
    currentPan: String,
    currentAadhaar: String,
    onSubmit: (pan: String, aadhaar: String) -> Unit,
    onDismiss: () -> Unit
) {
    var panText by remember { mutableStateOf(currentPan) }
    var aadhaarText by remember { mutableStateOf(currentAadhaar) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "KYC Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorGold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = panText,
                    onValueChange = { panText = it.uppercase() },
                    label = { Text("PAN Card Number") },
                    placeholder = { Text("ABCDE1234F") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = aadhaarText,
                    onValueChange = { aadhaarText = it },
                    label = { Text("Aadhaar Card Number") },
                    placeholder = { Text("1234-5678-9012") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(panText, aadhaarText) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGold, contentColor = Color.Black)
                    ) {
                        Text("Submit KYC", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BankLinkDialog(
    currentAccount: String,
    currentIfsc: String,
    currentHolder: String,
    currentUpi: String,
    onSave: (acc: String, ifsc: String, holder: String, upi: String) -> Unit,
    onDismiss: () -> Unit
) {
    var accText by remember { mutableStateOf(currentAccount) }
    var ifscText by remember { mutableStateOf(currentIfsc) }
    var holderText by remember { mutableStateOf(currentHolder) }
    var upiText by remember { mutableStateOf(currentUpi) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Link Bank Account & UPI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorBlue)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = holderText, onValueChange = { holderText = it }, label = { Text("Account Holder Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = accText, onValueChange = { accText = it }, label = { Text("Bank Account Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = ifscText, onValueChange = { ifscText = it.uppercase() }, label = { Text("IFSC Code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = upiText, onValueChange = { upiText = it }, label = { Text("UPI ID") }, placeholder = { Text("username@upi") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(accText, ifscText, holderText, upiText) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorBlue, contentColor = Color.White)
                    ) {
                        Text("Save Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
