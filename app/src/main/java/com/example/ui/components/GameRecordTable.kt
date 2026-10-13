package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
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
import com.example.data.model.DrawRound
import com.example.ui.theme.*

@Composable
fun GameRecordTable(
    rounds: List<DrawRound>,
    modifier: Modifier = Modifier
) {
    var selectedRoundForProof by remember { mutableStateOf<DrawRound?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Period",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Number",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Big/Small",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Color",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(1.8f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (rounds.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Waiting for draw rounds...",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rounds.take(15).forEach { round ->
                        RecordRow(
                            round = round,
                            onClick = { selectedRoundForProof = round }
                        )
                    }
                }
            }
        }
    }

    selectedRoundForProof?.let { round ->
        ProofDialog(round = round, onDismiss = { selectedRoundForProof = null })
    }
}

@Composable
fun RecordRow(
    round: DrawRound,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Period
        Text(
            text = round.periodId.takeLast(8),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary,
            modifier = Modifier.weight(2f)
        )

        // Number Ball
        Box(modifier = Modifier.weight(1.2f)) {
            val num = round.resultNumber
            val baseBrush = when (num) {
                0 -> Brush.horizontalGradient(listOf(ColorViolet, ColorRed))
                5 -> Brush.horizontalGradient(listOf(ColorViolet, ColorGreen))
                1, 3, 7, 9 -> Brush.linearGradient(listOf(ColorGreen, ColorGreenDark))
                else -> Brush.linearGradient(listOf(ColorRed, ColorRedDark))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(baseBrush)
            ) {
                Text(
                    text = num.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Big/Small
        Text(
            text = if (round.isBig) "Big" else "Small",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (round.isBig) ColorGold else ColorBlue,
            modifier = Modifier.weight(1.5f)
        )

        // Color Dots
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1.8f)
        ) {
            when (round.resultNumber) {
                0 -> {
                    ColorDot(ColorRed)
                    ColorDot(ColorViolet)
                }
                5 -> {
                    ColorDot(ColorGreen)
                    ColorDot(ColorViolet)
                }
                1, 3, 7, 9 -> ColorDot(ColorGreen)
                else -> ColorDot(ColorRed)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Provably Fair",
                tint = TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun ProofDialog(round: DrawRound, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Provably Fair Verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Period: ${round.periodId}",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Outcome: ${round.resultNumber} (${round.colorsString})",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "SHA-256 Hash Digest:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(8.dp)
                ) {
                    Text(
                        text = round.hashProof,
                        fontSize = 10.sp,
                        color = ColorGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight)
                ) {
                    Text("Close", color = TextPrimary)
                }
            }
        }
    }
}
