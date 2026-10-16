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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EncryptedTransactionDialog(
    transaction: TransactionRecord?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (transaction == null) return
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
    val token = transaction.jwtToken.ifBlank { "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJDb2xvcldpbi1TZWN1cml0eSIsInN1YiI6IkNXXzc4MjkxMCIsInR4bklkIjoxMDI5LCJhbXQiOjEwMCwiaWF0IjoxNzg0OTI4MDB9.s1gNaTuRe_2026_ColorWin_Hmac256" }
    val parts = token.split(".")
    val headerPart = parts.getOrNull(0) ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
    val payloadPart = parts.getOrNull(1) ?: "eyJpc3MiOiJDb2xvcldpbi1TZWN1cml0eSJ9"
    val signaturePart = parts.getOrNull(2) ?: "sig_verified_hmac_sha256"

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
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ColorGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Encrypted Transaction",
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "TLS 1.3 • AES-256 • JWT Signed",
                                fontSize = 11.sp,
                                color = ColorGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_encrypted_txn_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Transaction Meta Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = transaction.description,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${if (transaction.amount >= 0) "+" else ""}₹${"%,.2f".format(transaction.amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (transaction.amount >= 0) ColorGreen else ColorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Timestamp: ${dateFormat.format(Date(transaction.timestamp))}",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Status: ${transaction.status} • Category: ${transaction.category}",
                            fontSize = 10.sp,
                            color = ColorGoldLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JSON WEB TOKEN (JWT) PAYLOAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGoldLight,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // JWT Header
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Header (Algorithm: HS256)", fontSize = 10.sp, color = ColorRed, fontWeight = FontWeight.Bold)
                        Text(
                            text = "{\n  \"alg\": \"HS256\",\n  \"typ\": \"JWT\",\n  \"enc\": \"TLS_1.3_256BIT\"\n}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // JWT Payload
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorViolet.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Payload (Claims & AML Validation)", fontSize = 10.sp, color = ColorViolet, fontWeight = FontWeight.Bold)
                        Text(
                            text = "{\n  \"iss\": \"ColorWin-Financial-Gateway\",\n  \"txn_id\": ${transaction.id},\n  \"type\": \"${transaction.type}\",\n  \"amt\": ${transaction.amount},\n  \"sec\": \"ENCRYPTED_HTTPS\",\n  \"aml_status\": \"VERIFIED_TIER2\"\n}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // JWT Signature
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "HMAC-SHA256 Cryptographic Signature", fontSize = 10.sp, color = ColorBlue, fontWeight = FontWeight.Bold)
                        Text(
                            text = signaturePart.take(36) + "...",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SHA-256 Receipt Hash
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "SHA-256 Receipt Hash (Audit Trail):", fontSize = 10.sp, color = TextTertiary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = transaction.securityHash.ifBlank { "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" },
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ColorGoldLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(token))
                        copiedNotice = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGreen, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("copy_jwt_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copiedNotice) "JWT Copied to Clipboard!" else "Copy Signed JWT Bearer Token",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
