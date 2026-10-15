package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class RoundCommitment(
    val periodId: String,
    val serverSeed: String,
    val commitHash: String,
    val resultNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class VerificationResult(
    val isValid: Boolean,
    val calculatedHash: String,
    val expectedHash: String,
    val calculatedNumber: Int,
    val expectedNumber: Int,
    val message: String
)

/**
 * Server-Side Cryptographically Secure RNG & Provably Fair Engine.
 * 
 * Guarantees:
 * 1. Strict Server-Side RNG using java.security.SecureRandom (client never controls outcome).
 * 2. Provably Fair Commitment: Published before round start:
 *    commitHash = SHA-256(serverSeed + ":" + periodId)
 * 3. Deterministic outcome derived from HMAC-SHA256(serverSeed, periodId).
 * 4. Transparent cryptographic verification for players.
 */
object ServerRngEngine {

    private val secureRandom = SecureRandom()

    /**
     * Generates a pre-round commitment for a new period.
     * The commitHash is published to all clients BEFORE betting begins.
     * The serverSeed remains secret until the round ends.
     */
    fun generateCommitment(periodId: String): RoundCommitment {
        val seedBytes = ByteArray(32)
        secureRandom.nextBytes(seedBytes)
        val serverSeed = seedBytes.joinToString("") { "%02x".format(it) }

        val commitHash = calculateCommitHash(serverSeed, periodId)
        val resultNumber = deriveResultNumber(serverSeed, periodId)

        return RoundCommitment(
            periodId = periodId,
            serverSeed = serverSeed,
            commitHash = commitHash,
            resultNumber = resultNumber
        )
    }

    /**
     * SHA-256(serverSeed + ":" + periodId)
     */
    fun calculateCommitHash(serverSeed: String, periodId: String): String {
        val input = "$serverSeed:$periodId"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives a deterministic number 0-9 using HMAC-SHA256 with the server seed.
     */
    fun deriveResultNumber(serverSeed: String, periodId: String): Int {
        val keySpec = SecretKeySpec(serverSeed.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(keySpec)
        val hmacBytes = mac.doFinal(periodId.toByteArray(Charsets.UTF_8))

        // Take the first 4 bytes as unsigned integer, modulo 10
        val rawValue = ((hmacBytes[0].toLong() and 0xFFL) shl 24) or
                ((hmacBytes[1].toLong() and 0xFFL) shl 16) or
                ((hmacBytes[2].toLong() and 0xFFL) shl 8) or
                (hmacBytes[3].toLong() and 0xFFL)

        return (rawValue % 10L).toInt()
    }

    /**
     * Verifies that:
     * 1. SHA-256(revealedSeed + ":" + periodId) == expectedCommitHash
     * 2. deriveResultNumber(revealedSeed, periodId) == expectedNumber
     */
    fun verifyProof(
        periodId: String,
        revealedServerSeed: String,
        expectedCommitHash: String,
        expectedNumber: Int
    ): VerificationResult {
        val calculatedHash = calculateCommitHash(revealedServerSeed, periodId)
        val calculatedNumber = deriveResultNumber(revealedServerSeed, periodId)

        val hashMatches = calculatedHash.equals(expectedCommitHash, ignoreCase = true)
        val numberMatches = calculatedNumber == expectedNumber

        val isValid = hashMatches && numberMatches
        val message = when {
            isValid -> "Provably Fair Verified! The published commit hash and outcome match the revealed server seed."
            !hashMatches -> "Verification Failed: Commit hash mismatch! The seed does not reproduce the published pre-round hash."
            else -> "Verification Failed: Result number mismatch!"
        }

        return VerificationResult(
            isValid = isValid,
            calculatedHash = calculatedHash,
            expectedHash = expectedCommitHash,
            calculatedNumber = calculatedNumber,
            expectedNumber = expectedNumber,
            message = message
        )
    }
}
