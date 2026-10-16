package com.example.data.security

import com.example.data.model.UserProfile
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

sealed class GeoCheckResult {
    data class Allowed(val state: String) : GeoCheckResult()
    data class Blocked(val state: String, val legalNotice: String) : GeoCheckResult()
}

sealed class AgeCheckResult {
    data class Verified(val age: Int, val dob: String) : AgeCheckResult()
    data class Underage(val age: Int, val reason: String) : AgeCheckResult()
    data class InvalidFormat(val message: String) : AgeCheckResult()
}

sealed class AmlCheckResult {
    object Approved : AmlCheckResult()
    data class KycRequired(val message: String) : AmlCheckResult()
    data class AmlTier2Required(val message: String) : AmlCheckResult()
}

sealed class ResponsibleLimitResult {
    object Allowed : ResponsibleLimitResult()
    data class Blocked(val title: String, val reason: String) : ResponsibleLimitResult()
}

object GeoComplianceEngine {
    val RESTRICTED_STATES = listOf(
        "Telangana",
        "Andhra Pradesh",
        "Assam",
        "Odisha",
        "Nagaland",
        "Sikkim",
        "Tamil Nadu"
    )

    val ALLOWED_STATES = listOf(
        "Maharashtra",
        "Karnataka",
        "Delhi",
        "Goa",
        "Gujarat",
        "Haryana",
        "Punjab",
        "West Bengal",
        "Rajasthan",
        "Uttar Pradesh",
        "Kerala",
        "Madhya Pradesh"
    )

    fun checkCompliance(state: String): GeoCheckResult {
        val normalized = state.trim()
        val isRestricted = RESTRICTED_STATES.any { it.equals(normalized, ignoreCase = true) }
        return if (isRestricted) {
            val notice = when (normalized.lowercase()) {
                "telangana" -> "The Telangana Gaming Act, 1974 prohibits real-money wagering games in Telangana jurisdiction."
                "andhra pradesh" -> "The Andhra Pradesh Gaming Amendment Act, 2020 prohibits all real-money online prediction and betting platforms."
                "assam" -> "The Assam Game and Betting Act, 1970 strictly restricts games of chance and staking platforms in Assam."
                "odisha" -> "The Odisha Prevention of Gambling Act, 1955 restricts real-money wagering within state boundaries."
                else -> "State legislation in $normalized prohibits real-money online prediction games. Access and financial operations are restricted."
            }
            GeoCheckResult.Blocked(normalized, notice)
        } else {
            GeoCheckResult.Allowed(normalized)
        }
    }
}

object AgeVerificationEngine {
    fun calculateAge(dobString: String): AgeCheckResult {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            format.isLenient = false
            val birthDate = format.parse(dobString) ?: return AgeCheckResult.InvalidFormat("Invalid date format (YYYY-MM-DD required)")

            val dobCalendar = Calendar.getInstance().apply { time = birthDate }
            val today = Calendar.getInstance()

            var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            if (age >= 18) {
                AgeCheckResult.Verified(age, dobString)
            } else {
                AgeCheckResult.Underage(
                    age = age,
                    reason = "You must be at least 18 years of age to access ColorWin. (Detected age: $age years)"
                )
            }
        } catch (e: Exception) {
            AgeCheckResult.InvalidFormat("Error parsing date: ${e.message}")
        }
    }
}

object AmlComplianceEngine {
    const val AML_THRESHOLD_INR = 10000.0

    fun validateWithdrawal(amount: Double, profile: UserProfile): AmlCheckResult {
        if (!profile.kycVerified) {
            return AmlCheckResult.KycRequired("Basic KYC Verification (PAN & Aadhaar) is required for all cash withdrawals.")
        }

        if (amount > AML_THRESHOLD_INR) {
            if (!profile.isAmlVerified) {
                return AmlCheckResult.AmlTier2Required(
                    "AML / PMLA Compliance Notice: Withdrawals exceeding ₹10,000 require Enhanced Due Diligence (EDD) Tier-2 KYC verification."
                )
            }

            // Validate PAN format
            val panPattern = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
            if (!panPattern.matches(profile.kycPan.trim())) {
                return AmlCheckResult.AmlTier2Required("Invalid PAN Card format for AML clearance. Standard 10-character PAN required.")
            }

            // Validate Bank Account details exist
            if (profile.bankAccountNumber.isBlank() && profile.upiId.isBlank()) {
                return AmlCheckResult.AmlTier2Required("Linked Bank Account or Verified UPI is required for high-value AML payouts.")
            }
        }

        return AmlCheckResult.Approved
    }
}

object TransactionCryptoEngine {
    private const val HMAC_SECRET = "COLORWIN_HTTPS_JWT_SECRET_KEY_2026_COMPLIANCE"

    fun generateJwtToken(
        userId: String,
        transactionId: Long,
        type: String,
        amount: Double
    ): String {
        val header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}"
        val issuedAt = System.currentTimeMillis()
        val expiresAt = issuedAt + (24 * 60 * 60 * 1000L)
        val payload = "{\"iss\":\"ColorWin-Security\",\"sub\":\"$userId\",\"txnId\":$transactionId,\"type\":\"$type\",\"amt\":$amount,\"iat\":$issuedAt,\"exp\":$expiresAt,\"sec\":\"TLS_1.3_AES_256\",\"aml_status\":\"CLEARED\"}"

        val base64Header = base64UrlEncode(header.toByteArray(StandardCharsets.UTF_8))
        val base64Payload = base64UrlEncode(payload.toByteArray(StandardCharsets.UTF_8))
        val signatureInput = "$base64Header.$base64Payload"

        val signature = computeHmacSha256(signatureInput, HMAC_SECRET)
        val base64Signature = base64UrlEncode(signature)

        return "$base64Header.$base64Payload.$base64Signature"
    }

    fun generateSha256Receipt(
        periodOrTxnId: String,
        userId: String,
        amount: Double,
        timestamp: Long
    ): String {
        val input = "CW_TXN-$periodOrTxnId-$userId-$amount-$timestamp-SALT_2026"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun computeHmacSha256(data: String, key: String): ByteArray {
        val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
        )
    }
}

object ResponsibleGamblingEngine {
    fun checkBet(betAmount: Double, profile: UserProfile): ResponsibleLimitResult {
        val now = System.currentTimeMillis()

        // 1. Self-exclusion check
        if (profile.selfExclusionUntil > now) {
            val remainingHours = ((profile.selfExclusionUntil - now) / (1000 * 60 * 60)).coerceAtLeast(1)
            return ResponsibleLimitResult.Blocked(
                title = "Self-Exclusion Active",
                reason = "Your self-exclusion cool-off period is active for another $remainingHours hour(s). Wagering is paused for your safety."
            )
        }

        // 2. Single bet limit check
        if (profile.singleBetLimit > 0 && betAmount > profile.singleBetLimit) {
            return ResponsibleLimitResult.Blocked(
                title = "Single Bet Limit Exceeded",
                reason = "Your wager of ₹${betAmount.toInt()} exceeds your configured Single Bet Limit of ₹${profile.singleBetLimit.toInt()}."
            )
        }

        // 3. Daily loss limit check
        if (profile.dailyLossLimit > 0 && profile.currentDailyLoss + betAmount > profile.dailyLossLimit) {
            return ResponsibleLimitResult.Blocked(
                title = "Daily Loss Limit Reached",
                reason = "Placing this wager may exceed your Daily Loss Limit of ₹${profile.dailyLossLimit.toInt()} (Current losses today: ₹${profile.currentDailyLoss.toInt()})."
            )
        }

        return ResponsibleLimitResult.Allowed
    }

    fun checkDeposit(depositAmount: Double, profile: UserProfile): ResponsibleLimitResult {
        if (profile.dailyDepositLimit > 0 && profile.currentDailyDeposit + depositAmount > profile.dailyDepositLimit) {
            return ResponsibleLimitResult.Blocked(
                title = "Daily Deposit Limit Exceeded",
                reason = "Deposit of ₹${depositAmount.toInt()} exceeds your remaining Daily Deposit Limit (Limit: ₹${profile.dailyDepositLimit.toInt()}, Deposited today: ₹${profile.currentDailyDeposit.toInt()})."
            )
        }
        return ResponsibleLimitResult.Allowed
    }
}
