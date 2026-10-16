package com.example.data.model

data class VipTier(
    val level: Int,
    val name: String,
    val requiredXp: Int,
    val cashbackPercent: Double,
    val referralCommissionRate: Double,
    val dailyReward: Double,
    val colorHex: Long
)

object VipSystem {
    val TIERS = listOf(
        VipTier(0, "Newbie", 0, 0.0, 5.0, 10.0, 0xFF78909CL),
        VipTier(1, "Bronze", 500, 2.0, 5.5, 25.0, 0xFFCD7F32L),
        VipTier(2, "Silver", 1500, 3.0, 6.0, 50.0, 0xFFC0C0C0L),
        VipTier(3, "Gold", 3500, 4.0, 7.0, 100.0, 0xFFFFD700L),
        VipTier(4, "Platinum", 7000, 5.0, 7.5, 200.0, 0xFFE5E4E2L),
        VipTier(5, "Diamond", 12000, 6.0, 8.5, 350.0, 0xFF00E5FFL),
        VipTier(6, "Crown", 20000, 7.0, 9.0, 500.0, 0xFFFF9100L),
        VipTier(7, "Master", 35000, 8.0, 9.5, 800.0, 0xFFFF1744L),
        VipTier(8, "Grandmaster", 55000, 9.0, 10.0, 1200.0, 0xFFD500F9L),
        VipTier(9, "Champion", 80000, 10.0, 10.0, 2000.0, 0xFF00E676L),
        VipTier(10, "Royal Legend", 120000, 12.0, 10.0, 3500.0, 0xFFFFEA00L)
    )

    fun getTier(level: Int): VipTier {
        return TIERS.find { it.level == level } ?: TIERS.first()
    }

    fun getTierForXp(xp: Int): VipTier {
        return TIERS.lastOrNull { xp >= it.requiredXp } ?: TIERS.first()
    }

    fun getNextTier(currentLevel: Int): VipTier? {
        return TIERS.find { it.level == currentLevel + 1 }
    }
}

data class DepositBonusTier(
    val minAmount: Double,
    val bonusPercent: Double,
    val tag: String,
    val description: String
)

object DepositBonusSystem {
    val TIERS = listOf(
        DepositBonusTier(100.0, 5.0, "STARTER", "+5% Welcome Kickstart"),
        DepositBonusTier(500.0, 10.0, "BOOST", "+10% Value Booster"),
        DepositBonusTier(1000.0, 20.0, "POPULAR", "+20% Most Popular Match"),
        DepositBonusTier(3000.0, 35.0, "HIGH ROLLER", "+35% High Roller Special"),
        DepositBonusTier(10000.0, 50.0, "VIP ROYAL", "+50% Ultimate VIP Bonus")
    )

    fun calculateBonus(amount: Double, isFirstDeposit: Boolean = false): Double {
        if (isFirstDeposit && amount >= 500.0) {
            // First deposit 100% match up to ₹5,000
            return minOf(amount, 5000.0)
        }
        val matchedTier = TIERS.lastOrNull { amount >= it.minAmount }
        val rate = matchedTier?.bonusPercent ?: 0.0
        return (amount * rate) / 100.0
    }

    fun getTierForAmount(amount: Double): DepositBonusTier? {
        return TIERS.lastOrNull { amount >= it.minAmount }
    }
}

data class HouseEdgeInfo(
    val betType: String,
    val winProbability: Double,
    val formula: String,
    val grossPayout: Double,
    val houseEdgePercent: Double,
    val rtpPercent: Double,
    val description: String
)

object HouseEdgeSystem {
    /**
     * Exact House Edge breakdown:
     * - Color Bet (Green/Red): 2x payout on 45% win probability = 0.45 * 2 = 0.90 -> 10% House Edge.
     * - Number Bet (0-9): 9x payout on 10% win probability = 0.10 * 9 = 0.90 -> 10% House Edge.
     * - Violet Bet (0 or 5): 4.5x payout on 20% win probability = 0.20 * 4.5 = 0.90 -> 10% House Edge.
     * - Big / Small: 1.8x on 50% or 2.0x on 45% = 10% House Edge.
     */
    val BREAKDOWN = listOf(
        HouseEdgeInfo(
            betType = "Colour (Green / Red)",
            winProbability = 45.0,
            formula = "2.0x on 45% probability = 90% RTP",
            grossPayout = 2.0,
            houseEdgePercent = 10.0,
            rtpPercent = 90.0,
            description = "10 balls (0-9). 4 pure colours (40%) + 1 split colour (10% at half value). Player return is exactly 90% (10% House Edge)."
        ),
        HouseEdgeInfo(
            betType = "Exact Number (0-9)",
            winProbability = 10.0,
            formula = "9.0x on 10% probability = 90% RTP",
            grossPayout = 9.0,
            houseEdgePercent = 10.0,
            rtpPercent = 90.0,
            description = "1 winning ball out of 10. Payout is 9.0x multiplier, leaving 10% house edge."
        ),
        HouseEdgeInfo(
            betType = "Violet Token (0 & 5)",
            winProbability = 20.0,
            formula = "4.5x on 20% probability = 90% RTP",
            grossPayout = 4.5,
            houseEdgePercent = 10.0,
            rtpPercent = 90.0,
            description = "2 winning balls out of 10 (0 and 5). 4.5x payout yields 90% RTP and exactly 10% house edge."
        ),
        HouseEdgeInfo(
            betType = "Big / Small (0-4 vs 5-9)",
            winProbability = 45.0,
            formula = "2.0x on 45% (or 1.8x on 50%) = 90% RTP",
            grossPayout = 2.0,
            houseEdgePercent = 10.0,
            rtpPercent = 90.0,
            description = "Standard casino 10% house edge with platform tax and zero-split protection."
        )
    )
}

object ReferralSystem {
    fun getCommissionRate(referralsCount: Int, vipLevel: Int): Double {
        return when {
            referralsCount >= 30 || vipLevel >= 8 -> 10.0
            referralsCount >= 15 || vipLevel >= 6 -> 9.0
            referralsCount >= 10 || vipLevel >= 4 -> 8.0
            referralsCount >= 5 || vipLevel >= 2 -> 6.5
            else -> 5.0
        }
    }

    val SUB_AFFILIATE_RATE = 2.5 // Level 2 Indirect Referral Commission
}
