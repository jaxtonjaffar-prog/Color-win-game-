package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun RulesGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.dp, DividerColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Colour Prediction Game Rules",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGold
                )
                Spacer(modifier = Modifier.height(12.dp))

                RuleItem(
                    title = "1. Round System & Configurable Durations",
                    description = "Choose between Win Go 30s, 1Min (standard 60s), 3Min, or 5Min.\n• Betting Phase: First 55 seconds (or total duration minus 5s).\n• Lock Phase: Final 5 seconds (strictly no new bets).\n• Result Reveal: Random colour and number generated at round end.\n• Auto-loop: Next round starts automatically after result!"
                )

                RuleItem(
                    title = "2. Colour Options & Payout Multipliers",
                    description = "• 🟢 Green (2x): Numbers 1, 3, 7, 9 pay 2.0x. Number 5 pays 1.5x.\n• 🔴 Red (2x): Numbers 2, 4, 6, 8 pay 2.0x. Number 0 pays 1.5x.\n• 🟣 Violet (4.5x): Numbers 0 & 5 pay 4.5x.\n• 🟢+🟣 Green+Violet (1.5x): Direct combo bet on number 5 outcome pays 1.5x.\n• 🔴+🟣 Red+Violet (1.5x): Direct combo bet on number 0 outcome pays 1.5x."
                )

                RuleItem(
                    title = "3. Number Betting (0 to 9)",
                    description = "• Numbers 1, 2, 3, 4, 6, 7, 8, 9: Win pays 9.0x!\n• Numbers 0 & 5: Violet + Green/Red combos pay higher 9.5x combo multiplier!"
                )

                RuleItem(
                    title = "4. Big / Small (Odds: 2.0X)",
                    description = "• Big: Numbers 5, 6, 7, 8, 9 (2.0x payout).\n• Small: Numbers 0, 1, 2, 3, 4 (2.0x payout)."
                )

                RuleItem(
                    title = "5. Service Fee & Payout Formula",
                    description = "• Contract = Chip Amount × Multiplier.\n• Net Contract = Contract × 0.98 (2% platform service charge).\n• Net Win = Net Contract × Multiplier."
                )

                RuleItem(
                    title = "6. Provably Fair Guarantee",
                    description = "Every round's draw is cryptographically hashed using SHA-256 with verifiable timestamp and unique secret seed. Tap any history row in 'Game Record' to inspect the SHA-256 proof."
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGold, contentColor = Color.Black)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RuleItem(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGoldLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )
    }
}
