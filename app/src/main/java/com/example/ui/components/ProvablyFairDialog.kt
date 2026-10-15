package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DrawRound
import com.example.data.security.ServerRngEngine
import com.example.data.security.VerificationResult
import com.example.ui.theme.*

@Composable
fun ProvablyFairDialog(
    currentPeriodId: String,
    currentCommitHash: String,
    isLocked: Boolean,
    recentRounds: List<DrawRound>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedRound by remember { mutableStateOf<DrawRound?>(recentRounds.firstOrNull()) }
    var testPeriodId by remember(selectedRound) { mutableStateOf(selectedRound?.periodId ?: currentPeriodId) }
    var testServerSeed by remember(selectedRound) { mutableStateOf(selectedRound?.serverSeed ?: "") }
    var testExpectedHash by remember(selectedRound) { mutableStateOf(selectedRound?.hashProof ?: currentCommitHash) }
    var testExpectedNumber by remember(selectedRound) { mutableStateOf((selectedRound?.resultNumber ?: 7).toString()) }

    var verificationResult by remember { mutableStateOf<VerificationResult?>(null) }
    var copiedText by remember { mutableStateOf<String?>(null) }

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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(ColorGold, ColorGoldDark)))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Anti-Cheat & Fairness",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "100% Cryptographic Transparency",
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily,
                                color = ColorGoldLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_fairness_dialog")
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

                // 4 Pillars Summary
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillarItem(
                        icon = Icons.Default.Security,
                        title = "Server-Side RNG Only",
                        desc = "Zero client execution. Numbers generated via SecureRandom 256-bit cryptography on the game server.",
                        accentColor = ColorGreen
                    )
                    PillarItem(
                        icon = Icons.Default.Lock,
                        title = "Pre-Round Published Hash",
                        desc = "SHA-256 commitment published before betting begins. The outcome cannot be altered.",
                        accentColor = ColorGold
                    )
                    PillarItem(
                        icon = Icons.Default.Timer,
                        title = "Strict Bet Lock (5s)",
                        desc = "Wagers are locked 5 seconds prior to result generation. Server strictly rejects late wagers.",
                        accentColor = ColorRed
                    )
                    PillarItem(
                        icon = Icons.Default.Speed,
                        title = "Anti-Bot Rate Limiting",
                        desc = "Per-user request throttling (max 5 wagers / 3s, 300ms spacing) to prevent automated scripts and burst flooding.",
                        accentColor = ColorViolet
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                // Current Active Round Commitment Banner
                Text(
                    text = "ACTIVE ROUND COMMITMENT",
                    fontSize = 11.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = ColorGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Period: $currentPeriodId",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                color = if (isLocked) ColorRed.copy(alpha = 0.2f) else ColorGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isLocked) "BETTING LOCKED" else "ACTIVE & COMMITTED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) ColorRed else ColorGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Published SHA-256 Commit Hash:",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentCommitHash.ifEmpty { "Generating..." },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ColorGoldLight
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentCommitHash))
                                    copiedText = "Hash copied to clipboard!"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorGold),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("copy_commit_hash_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Copy Hash", fontSize = 10.sp, fontFamily = PoppinsFontFamily)
                            }
                        }
                    }
                }

                if (copiedText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = copiedText!!,
                        fontSize = 10.sp,
                        color = ColorGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Verifier
                Text(
                    text = "INDEPENDENT PROVABLY FAIR VERIFIER",
                    fontSize = 11.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = ColorGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verify that revealed server seed matches published commit hash and produces the exact winning number:",
                    fontSize = 10.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select from recent rounds
                if (recentRounds.isNotEmpty()) {
                    Text(
                        text = "Select past round to audit:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recentRounds.take(3).forEach { round ->
                            val isSel = selectedRound?.periodId == round.periodId
                            OutlinedButton(
                                onClick = {
                                    selectedRound = round
                                    verificationResult = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSel) ColorGold.copy(alpha = 0.15f) else Color.Transparent,
                                    contentColor = if (isSel) ColorGold else TextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) ColorGold else DividerColor
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text(
                                    text = "...${round.periodId.takeLast(6)}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Inputs
                OutlinedTextField(
                    value = testPeriodId,
                    onValueChange = { testPeriodId = it },
                    label = { Text("Period ID", fontSize = 10.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = ColorGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = testServerSeed,
                    onValueChange = { testServerSeed = it },
                    label = { Text("Revealed Server Seed", fontSize = 10.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = ColorGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = testExpectedHash,
                    onValueChange = { testExpectedHash = it },
                    label = { Text("Published Commit Hash", fontSize = 10.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = ColorGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = testExpectedNumber,
                    onValueChange = { testExpectedNumber = it },
                    label = { Text("Expected Outcome Number (0-9)", fontSize = 10.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGold,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = ColorGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val num = testExpectedNumber.toIntOrNull() ?: 0
                        verificationResult = ServerRngEngine.verifyProof(
                            periodId = testPeriodId.trim(),
                            revealedServerSeed = testServerSeed.trim(),
                            expectedCommitHash = testExpectedHash.trim(),
                            expectedNumber = num
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("verify_proof_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Verify Cryptographic Proof",
                        fontSize = 12.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Verification Result Badge
                if (verificationResult != null) {
                    val result = verificationResult!!
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = if (result.isValid) ColorGreen.copy(alpha = 0.15f) else ColorRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (result.isValid) ColorGreen else ColorRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (result.isValid) ColorGreen else ColorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (result.isValid) "PROOF MATHEMATICALLY VERIFIED" else "VERIFICATION FAILED",
                                    fontSize = 12.sp,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = if (result.isValid) ColorGreen else ColorRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.message,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                            if (result.isValid) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Derived Number: ${result.calculatedNumber} ✓ | Hash: ${result.calculatedHash.take(16)}... ✓",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ColorGoldLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PillarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    accentColor: Color
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
