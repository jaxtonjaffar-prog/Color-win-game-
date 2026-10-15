package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ColorWin - Prediction Game", appName)
    }

    @Test
    fun `test all game modes exist with correct durations`() {
        assertEquals(30, GameMode.WINGO_30S.seconds)
        assertEquals(60, GameMode.WINGO_1MIN.seconds)
        assertEquals(180, GameMode.WINGO_3MIN.seconds)
        assertEquals(300, GameMode.WINGO_5MIN.seconds)
    }

    @Test
    fun `test 3-part balance summation`() {
        val wallet = WalletAccount(
            id = 1,
            mainBalance = 1200.0,
            bonusBalance = 500.0,
            winningsBalance = 300.0
        )
        assertEquals(2000.0, wallet.totalBalance, 0.01)
    }

    @Test
    fun `test user profile and referral code`() {
        val profile = UserProfile(
            userId = "CW_782910",
            phoneOrEmail = "+91 98765 43210",
            referralCode = "WIN7789",
            vipLevel = 2,
            kycVerified = true
        )
        assertEquals("WIN7789", profile.referralCode)
        assertTrue(profile.kycVerified)
        assertEquals(2, profile.vipLevel)
    }

    @Test
    fun `test color calculations for green red and violet combos`() {
        val greenColors = GameUtils.calculateColors(1)
        assertEquals(listOf(ColorChoice.GREEN), greenColors)

        val redColors = GameUtils.calculateColors(2)
        assertEquals(listOf(ColorChoice.RED), redColors)

        val violetRed = GameUtils.calculateColors(0)
        assertTrue(violetRed.contains(ColorChoice.VIOLET))
        assertTrue(violetRed.contains(ColorChoice.RED))
        assertTrue(violetRed.contains(ColorChoice.RED_VIOLET))

        val violetGreen = GameUtils.calculateColors(5)
        assertTrue(violetGreen.contains(ColorChoice.VIOLET))
        assertTrue(violetGreen.contains(ColorChoice.GREEN))
        assertTrue(violetGreen.contains(ColorChoice.GREEN_VIOLET))
    }

    @Test
    fun `test payout calculations for green red violet and combos`() {
        val betGreen = UserBet(
            periodId = "202610131M0001",
            gameMode = "WINGO_1MIN",
            betTargetKey = BetTarget.ColorBet(ColorChoice.GREEN).toKey(),
            chipAmount = 100.0,
            multiplier = 1,
            totalAmount = 100.0
        )
        // Draw 3 -> Pure Green -> 100 * 0.98 * 2.0 = 196.0
        val payoutGreenWin = GameUtils.calculatePayout(betGreen, 3)
        assertEquals(196.0, payoutGreenWin, 0.01)

        // Draw 5 -> Split Green -> 100 * 0.98 * 1.5 = 147.0
        val payoutSplitWin = GameUtils.calculatePayout(betGreen, 5)
        assertEquals(147.0, payoutSplitWin, 0.01)

        // Violet Bet
        val betViolet = UserBet(
            periodId = "202610131M0001",
            gameMode = "WINGO_1MIN",
            betTargetKey = BetTarget.ColorBet(ColorChoice.VIOLET).toKey(),
            chipAmount = 100.0,
            multiplier = 1,
            totalAmount = 100.0
        )
        // Draw 0 or 5 -> 100 * 0.98 * 4.5 = 441.0
        val payoutVioletWin = GameUtils.calculatePayout(betViolet, 0)
        assertEquals(441.0, payoutVioletWin, 0.01)

        // Green+Violet Direct Combo Bet
        val betGreenVioletCombo = UserBet(
            periodId = "202610131M0001",
            gameMode = "WINGO_1MIN",
            betTargetKey = BetTarget.ColorBet(ColorChoice.GREEN_VIOLET).toKey(),
            chipAmount = 100.0,
            multiplier = 1,
            totalAmount = 100.0
        )
        val payoutComboWin = GameUtils.calculatePayout(betGreenVioletCombo, 5)
        assertEquals(147.0, payoutComboWin, 0.01)

        // Exact number 0 combo bet -> higher 9.5x payout: 100 * 0.98 * 9.5 = 931.0
        val betNumber0 = UserBet(
            periodId = "202610131M0001",
            gameMode = "WINGO_1MIN",
            betTargetKey = BetTarget.NumberBet(0).toKey(),
            chipAmount = 100.0,
            multiplier = 1,
            totalAmount = 100.0
        )
        val payoutNumber0 = GameUtils.calculatePayout(betNumber0, 0)
        assertEquals(931.0, payoutNumber0, 0.01)

        // Regular exact number 7 -> 9.0x payout: 100 * 0.98 * 9.0 = 882.0
        val betNumber7 = UserBet(
            periodId = "202610131M0001",
            gameMode = "WINGO_1MIN",
            betTargetKey = BetTarget.NumberBet(7).toKey(),
            chipAmount = 100.0,
            multiplier = 1,
            totalAmount = 100.0
        )
        val payoutNumber7 = GameUtils.calculatePayout(betNumber7, 7)
        assertEquals(882.0, payoutNumber7, 0.01)
    }

    @Test
    fun `test provably fair hash generation is deterministic`() {
        val hash1 = GameUtils.generateHashProof("202610131M0001", 5)
        val hash2 = GameUtils.generateHashProof("202610131M0001", 5)
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length) // SHA-256 hex string length
    }

    @Test
    fun `test server side RNG generates valid commitment and number in 0 to 9`() {
        val commitment = com.example.data.security.ServerRngEngine.generateCommitment("202610141M0042")
        assertEquals(64, commitment.serverSeed.length)
        assertEquals(64, commitment.commitHash.length)
        assertTrue(commitment.resultNumber in 0..9)

        // Deriving with same seed and period must be 100% deterministic
        val derivedNumber = com.example.data.security.ServerRngEngine.deriveResultNumber(commitment.serverSeed, "202610141M0042")
        assertEquals(commitment.resultNumber, derivedNumber)
    }

    @Test
    fun `test provably fair verification succeeds for genuine seed and fails on tampered seed`() {
        val periodId = "202610141M0099"
        val commitment = com.example.data.security.ServerRngEngine.generateCommitment(periodId)

        // Genuine proof check
        val verified = com.example.data.security.ServerRngEngine.verifyProof(
            periodId = periodId,
            revealedServerSeed = commitment.serverSeed,
            expectedCommitHash = commitment.commitHash,
            expectedNumber = commitment.resultNumber
        )
        assertTrue(verified.isValid)

        // Tampered seed check -> must fail
        val fakeSeed = "a".repeat(64)
        val tamperedCheck = com.example.data.security.ServerRngEngine.verifyProof(
            periodId = periodId,
            revealedServerSeed = fakeSeed,
            expectedCommitHash = commitment.commitHash,
            expectedNumber = commitment.resultNumber
        )
        org.junit.Assert.assertFalse(tamperedCheck.isValid)
    }

    @Test
    fun `test anti-cheat rate limiter throttles rapid wagers`() {
        val rateLimiter = com.example.data.security.UserRateLimiter(minIntervalMs = 200L, maxRequestsPerWindow = 3)
        val userId = "USER_TEST_123"

        // 1st request -> allowed
        val r1 = rateLimiter.checkBettingAllowed(userId)
        assertTrue(r1 is com.example.data.security.RateLimitResult.Allowed)

        // Immediate 2nd request (<200ms) -> throttled
        val r2 = rateLimiter.checkBettingAllowed(userId)
        assertTrue(r2 is com.example.data.security.RateLimitResult.Throttled)
    }
}
