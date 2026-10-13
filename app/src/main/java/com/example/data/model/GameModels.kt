package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest

enum class GameMode(val title: String, val seconds: Int, val code: String) {
    WINGO_30S("Win Go 30s", 30, "30S"),
    WINGO_1MIN("Win Go 1Min", 60, "1M"),
    WINGO_3MIN("Win Go 3Min", 180, "3M"),
    WINGO_5MIN("Win Go 5Min", 300, "5M")
}

enum class ColorChoice(val label: String, val multiplier: Double) {
    GREEN("Green", 2.0),
    VIOLET("Violet", 4.5),
    RED("Red", 2.0),
    GREEN_VIOLET("Green+Violet", 1.5),
    RED_VIOLET("Red+Violet", 1.5)
}

enum class SizeChoice(val label: String, val multiplier: Double) {
    BIG("Big (5-9)", 2.0),
    SMALL("Small (0-4)", 2.0)
}

sealed class BetTarget(val displayLabel: String, val defaultMultiplier: Double) {
    data class ColorBet(val color: ColorChoice) : BetTarget(color.label, color.multiplier)
    data class NumberBet(val number: Int) : BetTarget("Number $number", if (number == 0 || number == 5) 9.5 else 9.0)
    data class SizeBet(val size: SizeChoice) : BetTarget(size.label, size.multiplier)

    fun toKey(): String = when (this) {
        is ColorBet -> "COLOR_${color.name}"
        is NumberBet -> "NUM_$number"
        is SizeBet -> "SIZE_${size.name}"
    }

    companion object {
        fun fromKey(key: String): BetTarget {
            return when {
                key.startsWith("COLOR_") -> ColorBet(ColorChoice.valueOf(key.removePrefix("COLOR_")))
                key.startsWith("NUM_") -> NumberBet(key.removePrefix("NUM_").toInt())
                key.startsWith("SIZE_") -> SizeBet(SizeChoice.valueOf(key.removePrefix("SIZE_")))
                else -> ColorBet(ColorChoice.GREEN)
            }
        }
    }
}

enum class BetStatus {
    PENDING,
    WON,
    LOST
}

@Entity(tableName = "wallet_account")
data class WalletAccount(
    @PrimaryKey val id: Int = 1,
    val mainBalance: Double = 1200.0,
    val bonusBalance: Double = 500.0,
    val winningsBalance: Double = 300.0,
    val totalWon: Double = 0.0,
    val totalLost: Double = 0.0,
    val totalBetsCount: Int = 0,
    val lastDailyClaimTimestamp: Long = 0L
) {
    // Total combined usable balance
    val totalBalance: Double get() = mainBalance + bonusBalance + winningsBalance
    // For backwards compatibility
    val balance: Double get() = totalBalance
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val userId: String = "CW_782910",
    val phoneOrEmail: String = "+91 98765 43210",
    val isLoggedIn: Boolean = true,
    val referralCode: String = "WIN7789",
    val referredBy: String? = null,
    val vipLevel: Int = 2,
    val vipXp: Int = 450,
    val referralEarnings: Double = 1250.0,
    val referralsCount: Int = 8,
    val kycVerified: Boolean = true,
    val kycPan: String = "ABCDE1234F",
    val kycAadhaar: String = "XXXX-XXXX-9012",
    val bankAccountNumber: String = "987654321001",
    val bankIfsc: String = "HDFC0001234",
    val bankHolderName: String = "VIP Player",
    val upiId: String = "player@oksbi"
)

@Entity(tableName = "draw_rounds")
data class DrawRound(
    @PrimaryKey val periodId: String,
    val gameMode: String,
    val resultNumber: Int,
    val colorsString: String, // "GREEN", "RED", "VIOLET_RED", "VIOLET_GREEN"
    val sizeString: String,   // "BIG", "SMALL"
    val timestamp: Long,
    val hashProof: String
) {
    val isBig: Boolean get() = resultNumber >= 5
    val hasViolet: Boolean get() = resultNumber == 0 || resultNumber == 5
    val hasGreen: Boolean get() = resultNumber in listOf(1, 3, 5, 7, 9)
    val hasRed: Boolean get() = resultNumber in listOf(0, 2, 4, 6, 8)
}

@Entity(tableName = "user_bets")
data class UserBet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val periodId: String,
    val gameMode: String,
    val betTargetKey: String,
    val chipAmount: Double,
    val multiplier: Int,
    val totalAmount: Double, // chipAmount * multiplier
    val status: BetStatus = BetStatus.PENDING,
    val winAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val target: BetTarget get() = BetTarget.fromKey(betTargetKey)
}

enum class TransactionCategory {
    DEPOSIT,
    WITHDRAWAL,
    BET,
    WIN,
    BONUS,
    REFERRAL
}

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // "BET_PLACED", "BET_WON", "TOP_UP", "DAILY_BONUS", "WITHDRAWAL", "REFERRAL_COMMISSION"
    val category: String = "ALL", // DEPOSIT, WITHDRAWAL, BET, WIN, BONUS, REFERRAL
    val amount: Double,
    val description: String,
    val status: String = "COMPLETED", // COMPLETED, PENDING, PROCESSING
    val timestamp: Long = System.currentTimeMillis()
)

object GameUtils {
    fun calculateColors(number: Int): List<ColorChoice> {
        return when (number) {
            0 -> listOf(ColorChoice.RED, ColorChoice.VIOLET, ColorChoice.RED_VIOLET)
            5 -> listOf(ColorChoice.GREEN, ColorChoice.VIOLET, ColorChoice.GREEN_VIOLET)
            1, 3, 7, 9 -> listOf(ColorChoice.GREEN)
            2, 4, 6, 8 -> listOf(ColorChoice.RED)
            else -> listOf(ColorChoice.GREEN)
        }
    }

    fun calculateColorsString(number: Int): String {
        return when (number) {
            0 -> "VIOLET_RED"
            5 -> "VIOLET_GREEN"
            1, 3, 7, 9 -> "GREEN"
            else -> "RED"
        }
    }

    fun calculateSize(number: Int): SizeChoice {
        return if (number >= 5) SizeChoice.BIG else SizeChoice.SMALL
    }

    fun generatePeriodId(gameMode: GameMode, roundIndex: Long): String {
        val datePart = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
        val serial = (roundIndex % 10000).toString().padStart(4, '0')
        return "$datePart${gameMode.code}$serial"
    }

    fun generateHashProof(periodId: String, number: Int): String {
        val salt = "COLOR_WIN_PROVABLY_FAIR_2026_SECRET"
        val input = "$periodId-$number-$salt"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Standard Win Go Rule:
     * Service charge: 2% (tax deduction).
     * If user bets Green and result is 1, 3, 7, 9 -> 2.0x (minus 2% fee -> 1.96x).
     * If user bets Green and result is 5 (Green+Violet) -> 1.5x (minus 2% fee -> 1.47x).
     * If user bets Red and result is 2, 4, 6, 8 -> 2.0x (1.96x).
     * If user bets Red and result is 0 (Red+Violet) -> 1.5x (1.47x).
     * If user bets Violet and result is 0 or 5 -> 4.5x (4.41x).
     * If user bets Green+Violet combo and result is 5 -> 1.5x (1.47x).
     * If user bets Red+Violet combo and result is 0 -> 1.5x (1.47x).
     * If user bets Number (1-4, 6-9) -> 9.0x (8.82x).
     * If user bets Number 0 or 5 (Violet combo) -> 9.5x (9.31x higher payout).
     * If user bets Big and result is 5-9 -> 2.0x (1.96x).
     * If user bets Small and result is 0-4 -> 2.0x (1.96x).
     */
    fun calculatePayout(bet: UserBet, drawNumber: Int): Double {
        val contract = bet.totalAmount
        val netContract = contract * 0.98 // 2% service fee
        val target = bet.target

        return when (target) {
            is BetTarget.ColorBet -> {
                when (target.color) {
                    ColorChoice.GREEN -> {
                        when (drawNumber) {
                            1, 3, 7, 9 -> netContract * 2.0
                            5 -> netContract * 1.5
                            else -> 0.0
                        }
                    }
                    ColorChoice.RED -> {
                        when (drawNumber) {
                            2, 4, 6, 8 -> netContract * 2.0
                            0 -> netContract * 1.5
                            else -> 0.0
                        }
                    }
                    ColorChoice.VIOLET -> {
                        if (drawNumber == 0 || drawNumber == 5) {
                            netContract * 4.5
                        } else {
                            0.0
                        }
                    }
                    ColorChoice.GREEN_VIOLET -> {
                        if (drawNumber == 5) {
                            netContract * 1.5
                        } else {
                            0.0
                        }
                    }
                    ColorChoice.RED_VIOLET -> {
                        if (drawNumber == 0) {
                            netContract * 1.5
                        } else {
                            0.0
                        }
                    }
                }
            }
            is BetTarget.NumberBet -> {
                if (target.number == drawNumber) {
                    val multiplier = if (drawNumber == 0 || drawNumber == 5) 9.5 else 9.0
                    netContract * multiplier
                } else {
                    0.0
                }
            }
            is BetTarget.SizeBet -> {
                val isBigResult = drawNumber >= 5
                val matches = (target.size == SizeChoice.BIG && isBigResult) ||
                        (target.size == SizeChoice.SMALL && !isBigResult)
                if (matches) {
                    netContract * 2.0
                } else {
                    0.0
                }
            }
        }
    }
}
