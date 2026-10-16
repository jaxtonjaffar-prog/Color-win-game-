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
import com.example.data.security.AgeCheckResult
import com.example.data.security.AgeVerificationEngine
import com.example.ui.theme.*

@Composable
fun AgeVerificationDialog(
    currentDob: String,
    onVerifyAge: (dob: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Current dob e.g. "1998-05-15"
    val parts = currentDob.split("-")
    var yearText by remember { mutableStateOf(parts.getOrNull(0) ?: "1998") }
    var monthText by remember { mutableStateOf(parts.getOrNull(1) ?: "05") }
    var dayText by remember { mutableStateOf(parts.getOrNull(2) ?: "15") }
    var isConfirmedAdult by remember { mutableStateOf(true) }

    val formattedDob = "${yearText.padStart(4, '0')}-${monthText.padStart(2, '0')}-${dayText.padStart(2, '0')}"
    val ageCheck = remember(formattedDob) {
        AgeVerificationEngine.calculateAge(formattedDob)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.5f)),
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
                                .background(ColorGold.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ColorGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Age Verification",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Strict 18+ Statutory Compliance",
                                fontSize = 11.sp,
                                color = ColorGoldLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_age_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Legal Disclaimer Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorGold.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ColorGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = "MANDATORY 18+ REQUIREMENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Underage gambling is an offense. You must be at least 18 years old to wager, deposit, or claim bonuses. Date of birth is verified against your government ID during AML/KYC review.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ENTER DATE OF BIRTH (YYYY-MM-DD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    // Day
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { if (it.length <= 2) dayText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Day") },
                        placeholder = { Text("15") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGold,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("dob_day_input")
                    )

                    // Month
                    OutlinedTextField(
                        value = monthText,
                        onValueChange = { if (it.length <= 2) monthText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Month") },
                        placeholder = { Text("05") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGold,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("dob_month_input")
                    )

                    // Year
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { if (it.length <= 4) yearText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Year") },
                        placeholder = { Text("1998") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGold,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1.4f).testTag("dob_year_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Age Status Pill
                when (ageCheck) {
                    is AgeCheckResult.Verified -> {
                        Surface(
                            color = ColorGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Age: ${ageCheck.age} years old • Legally Eligible (18+)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorGreen
                                )
                            }
                        }
                    }
                    is AgeCheckResult.Underage -> {
                        Surface(
                            color = ColorRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = ColorRed, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Age: ${ageCheck.age} years old • Underage (Must be 18+)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorRed
                                )
                            }
                        }
                    }
                    is AgeCheckResult.InvalidFormat -> {
                        Text(text = "Please enter a valid date of birth", fontSize = 11.sp, color = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isConfirmedAdult = !isConfirmedAdult }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isConfirmedAdult,
                        onCheckedChange = { isConfirmedAdult = it },
                        colors = CheckboxDefaults.colors(checkedColor = ColorGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I solemnly declare that I am 18 years or older and accept the responsible gaming charter.",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isEligible = ageCheck is AgeCheckResult.Verified && isConfirmedAdult

                Button(
                    onClick = {
                        onVerifyAge(formattedDob)
                        onDismiss()
                    },
                    enabled = isEligible,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGold, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("confirm_age_button")
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Verify & Confirm 18+ Age", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
