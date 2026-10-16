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
import com.example.data.security.GeoCheckResult
import com.example.data.security.GeoComplianceEngine
import com.example.ui.theme.*

@Composable
fun GeoBlockingDialog(
    currentState: String,
    onSelectState: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedState by remember { mutableStateOf(currentState) }
    val complianceResult = remember(selectedState) {
        GeoComplianceEngine.checkCompliance(selectedState)
    }
    val isBlocked = complianceResult is GeoCheckResult.Blocked

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isBlocked) ColorRed.copy(alpha = 0.6f) else ColorBlue.copy(alpha = 0.5f)
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
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
                                .background(if (isBlocked) ColorRed.copy(alpha = 0.2f) else ColorBlue.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isBlocked) Icons.Default.GpsOff else Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isBlocked) ColorRed else ColorBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Geo-Location Compliance",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isBlocked) "Restricted Jurisdiction" else "Permitted Legal Zone",
                                fontSize = 11.sp,
                                color = if (isBlocked) ColorRed else ColorGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_geo_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Legal Status Alert Card
                if (isBlocked) {
                    val notice = (complianceResult as GeoCheckResult.Blocked).legalNotice
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = ColorRed, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "ACCESS RESTRICTED IN $selectedState",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = notice,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Statutory Note: Real-money wagers, prediction stakes, and deposits are prohibited in this state by law.",
                                fontSize = 10.sp,
                                color = ColorRed.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "$selectedState is a Permitted Jurisdiction",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorGreen
                                )
                                Text(
                                    text = "All prediction wagers, deposits, and payouts are legally compliant under prevailing laws.",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SELECT OR SIMULATE DEVICE JURISDICTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Allowed States Section
                Text(
                    text = "Permitted Jurisdictions (Compliant):",
                    fontSize = 10.sp,
                    color = ColorGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GeoComplianceEngine.ALLOWED_STATES.forEach { state ->
                        val isChosen = selectedState == state
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) ColorGreen else SurfaceElevated)
                                .border(1.dp, if (isChosen) ColorGreen else DividerColor, RoundedCornerShape(8.dp))
                                .clickable { selectedState = state }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("state_pill_$state")
                        ) {
                            Text(
                                text = state,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) Color.Black else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Restricted States Section
                Text(
                    text = "Restricted Jurisdictions (Legally Blocked):",
                    fontSize = 10.sp,
                    color = ColorRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GeoComplianceEngine.RESTRICTED_STATES.forEach { state ->
                        val isChosen = selectedState == state
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) ColorRed else SurfaceElevated)
                                .border(1.dp, if (isChosen) ColorRed else DividerColor, RoundedCornerShape(8.dp))
                                .clickable { selectedState = state }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("state_pill_$state")
                        ) {
                            Text(
                                text = state,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onSelectState(selectedState)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlocked) ColorRed else ColorBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("apply_geo_location_button")
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBlocked) "Set as $selectedState (Enforce Block)" else "Confirm Location: $selectedState",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
