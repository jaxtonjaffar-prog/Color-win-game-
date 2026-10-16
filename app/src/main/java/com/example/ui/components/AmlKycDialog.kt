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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun AmlKycDialog(
    currentPan: String,
    currentAadhaar: String,
    onSubmit: (pan: String, aadhaar: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var panText by remember { mutableStateOf(currentPan) }
    var aadhaarText by remember { mutableStateOf(currentAadhaar) }
    var isAgreed by remember { mutableStateOf(true) }

    val panPattern = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    val isPanValid = panPattern.matches(panText.trim().uppercase())
    val isAadhaarValid = aadhaarText.trim().length >= 12

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
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ColorGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AML / Tier-2 KYC",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "High-Value Payouts (> ₹10,000)",
                                fontSize = 11.sp,
                                color = ColorGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_aml_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Statutory Regulatory Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ColorGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = "PMLA 2002 STATUTORY COMPLIANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Under the Prevention of Money Laundering Act (PMLA) 2002 & FIU-IND regulations, all transactions and single/cumulative withdrawals exceeding ₹10,000 require Enhanced Due Diligence (EDD) Tier-2 verification with valid Permanent Account Number (PAN).",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PAN Field
                Text(
                    text = "PERMANENT ACCOUNT NUMBER (PAN)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = panText,
                    onValueChange = { if (it.length <= 10) panText = it.uppercase() },
                    label = { Text("10-Digit PAN Number") },
                    placeholder = { Text("e.g. ABCDE1234F") },
                    singleLine = true,
                    isError = panText.isNotEmpty() && !isPanValid,
                    supportingText = {
                        if (panText.isNotEmpty() && !isPanValid) {
                            Text("Standard PAN format: 5 letters, 4 digits, 1 letter", color = ColorRed)
                        } else if (isPanValid) {
                            Text("✓ Valid PAN format detected", color = ColorGreen)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGreen,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("aml_pan_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Aadhaar Field
                Text(
                    text = "AADHAAR NUMBER / VID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = aadhaarText,
                    onValueChange = { aadhaarText = it },
                    label = { Text("12-Digit Aadhaar or Masked VID") },
                    placeholder = { Text("e.g. 5678-1234-9012") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorGreen,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("aml_aadhaar_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Consent Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isAgreed,
                        onCheckedChange = { isAgreed = it },
                        colors = CheckboxDefaults.colors(checkedColor = ColorGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I declare that the funds are legitimate and authorize statutory AML verification against NSDL/UIDAI databases.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (isPanValid && isAadhaarValid && isAgreed) {
                                onSubmit(panText.trim().uppercase(), aadhaarText.trim())
                            }
                        },
                        enabled = isPanValid && isAadhaarValid && isAgreed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("submit_aml_kyc_button")
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear AML Tier-2", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
