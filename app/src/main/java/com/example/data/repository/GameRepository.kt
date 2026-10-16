package com.example.data.repository

import com.example.data.db.GameDao
import com.example.data.model.*
import com.example.data.security.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class GameRepository(private val dao: GameDao) {

    val rateLimiter = UserRateLimiter()
    private val activeCommitments = ConcurrentHashMap<String, RoundCommitment>()
    private val lockedPeriods = ConcurrentHashMap.newKeySet<String>()

    fun registerPeriod(periodId: String): RoundCommitment {
        val commitment = ServerRngEngine.generateCommitment(periodId)
        activeCommitments[periodId] = commitment
        return commitment
    }

    fun getCommitment(periodId: String): RoundCommitment? = activeCommitments[periodId]

    fun lockPeriod(periodId: String) {
        lockedPeriods.add(periodId)
    }

    fun isPeriodLocked(periodId: String): Boolean = lockedPeriods.contains(periodId)

    fun getWalletFlow(): Flow<WalletAccount?> = dao.getWalletFlow()

    fun getUserProfileFlow(): Flow<UserProfile?> = dao.getUserProfileFlow()

    fun getGameSettingsFlow(): Flow<GameSettings?> = dao.getGameSettingsFlow()

    suspend fun getGameSettingsSync(): GameSettings = dao.getGameSettingsSync() ?: GameSettings()

    suspend fun updateGameSettings(settings: GameSettings) {
        dao.updateGameSettings(settings)
    }

    fun getRecentRoundsFlow(mode: GameMode): Flow<List<DrawRound>> =
        dao.getRecentRoundsFlow(mode.name)

    fun getAllBetsFlow(): Flow<List<UserBet>> = dao.getAllBetsFlow()

    fun getTransactionsFlow(): Flow<List<TransactionRecord>> = dao.getTransactionsFlow()

    suspend fun getLastBet(): UserBet? = dao.getLastBet()

    suspend fun initializeIfEmpty() {
        // Init profile if empty
        val currentProfile = dao.getUserProfileSync()
        if (currentProfile == null) {
            val initialProfile = UserProfile(
                id = 1,
                userId = "CW_782910",
                phoneOrEmail = "+91 98765 43210",
                isLoggedIn = true,
                referralCode = "WIN7789",
                vipLevel = 2,
                vipXp = 450,
                referralEarnings = 1250.0,
                referralsCount = 8,
                kycVerified = true,
                kycPan = "ABCDE1234F",
                kycAadhaar = "XXXX-XXXX-9012",
                bankAccountNumber = "987654321001",
                bankIfsc = "HDFC0001234",
                bankHolderName = "VIP Player",
                upiId = "player@oksbi"
            )
            dao.updateUserProfile(initialProfile)
        }

        // Init wallet if empty
        val currentWallet = dao.getWalletSync()
        if (currentWallet == null) {
            val initialWallet = WalletAccount(
                id = 1,
                mainBalance = 1200.0,
                bonusBalance = 500.0,
                winningsBalance = 300.0,
                totalWon = 0.0,
                totalLost = 0.0,
                totalBetsCount = 0,
                lastDailyClaimTimestamp = 0L
            )
            dao.updateWallet(initialWallet)
            dao.insertTransaction(
                TransactionRecord(
                    type = "WELCOME_BONUS",
                    category = "BONUS",
                    amount = 500.0,
                    description = "Welcome Signup Bonus"
                )
            )
            dao.insertTransaction(
                TransactionRecord(
                    type = "INITIAL_DEPOSIT",
                    category = "DEPOSIT",
                    amount = 1200.0,
                    description = "Starting Main Balance"
                )
            )
        }

        // Seed initial history rounds for all modes if empty
        GameMode.values().forEach { mode ->
            val count = dao.getRoundCount(mode.name)
            if (count < 25) {
                seedInitialHistory(mode)
            }
        }
    }

    private suspend fun seedInitialHistory(mode: GameMode) {
        val now = System.currentTimeMillis()
        val dummyRounds = mutableListOf<DrawRound>()
        for (i in 50 downTo 1) {
            val pastTime = now - (i * mode.seconds * 1000L)
            val periodId = GameUtils.generatePeriodId(mode, 100L + (50 - i))
            val commitment = ServerRngEngine.generateCommitment(periodId)
            val number = commitment.resultNumber
            dummyRounds.add(
                DrawRound(
                    periodId = periodId,
                    gameMode = mode.name,
                    resultNumber = number,
                    colorsString = GameUtils.calculateColorsString(number),
                    sizeString = GameUtils.calculateSize(number).name,
                    timestamp = pastTime,
                    hashProof = commitment.commitHash,
                    serverSeed = commitment.serverSeed
                )
            )
        }
        dao.insertRounds(dummyRounds)
    }

    suspend fun getNextRoundIndex(mode: GameMode): Long {
        val count = dao.getRoundCount(mode.name)
        return count + 1
    }

    suspend fun loginWithOtp(phoneOrEmail: String, referralCode: String?): Result<UserProfile> {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val isNewReferral = !referralCode.isNullOrBlank()
        val updatedProfile = profile.copy(
            phoneOrEmail = phoneOrEmail,
            isLoggedIn = true,
            referredBy = if (isNewReferral) referralCode else profile.referredBy
        )
        dao.updateUserProfile(updatedProfile)

        // Award referral signup bonus if code entered
        if (isNewReferral) {
            val wallet = dao.getWalletSync()
            if (wallet != null) {
                dao.updateWallet(wallet.copy(bonusBalance = wallet.bonusBalance + 200.0))
                dao.insertTransaction(
                    TransactionRecord(
                        type = "REFERRAL_SIGNUP_BONUS",
                        category = "BONUS",
                        amount = 200.0,
                        description = "Referral Bonus for code $referralCode"
                    )
                )
            }
        }
        return Result.success(updatedProfile)
    }

    suspend fun setGuestMode(): UserProfile {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val guest = profile.copy(isLoggedIn = false)
        dao.updateUserProfile(guest)
        return guest
    }

    suspend fun submitKyc(pan: String, aadhaar: String): Result<UserProfile> {
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val updated = profile.copy(
            kycPan = pan,
            kycAadhaar = aadhaar,
            kycVerified = true
        )
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun linkBankDetails(account: String, ifsc: String, holder: String, upi: String): Result<UserProfile> {
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val updated = profile.copy(
            bankAccountNumber = account,
            bankIfsc = ifsc,
            bankHolderName = holder,
            upiId = upi
        )
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun placeBet(
        periodId: String,
        gameMode: GameMode,
        target: BetTarget,
        chipAmount: Double,
        multiplier: Int
    ): Result<UserBet> {
        val profile = dao.getUserProfileSync()
        if (profile?.isLoggedIn != true) {
            return Result.failure(Exception("Guest mode: Please Login to place real bets!"))
        }
        if (profile.isFrozen) {
            return Result.failure(Exception("Account is frozen by Admin. Contact customer support."))
        }

        // 1. Age Verification (18+)
        if (!profile.isAgeVerified) {
            return Result.failure(Exception("Age Verification (18+) required before placing wagers. Please verify your age in Profile."))
        }

        // 2. Geo-Blocking Compliance
        if (profile.isGeoBlocked) {
            return Result.failure(Exception("Geo-Blocking Compliance: Wagering is restricted in ${profile.stateLocation} under state gambling regulations."))
        }

        // 3. Responsible Gambling Limits
        val limitCheck = ResponsibleGamblingEngine.checkBet(chipAmount * multiplier, profile)
        if (limitCheck is ResponsibleLimitResult.Blocked) {
            return Result.failure(Exception("${limitCheck.title}: ${limitCheck.reason}"))
        }

        // Anti-Cheat: Rate limiting per user (throttling rapid-fire bets/bots)
        val rateLimitResult = rateLimiter.checkBettingAllowed(profile.userId)
        if (rateLimitResult is RateLimitResult.Throttled) {
            return Result.failure(Exception(rateLimitResult.reason))
        }

        // Anti-Cheat: Bet lock check (reject any bet if period is locked)
        if (isPeriodLocked(periodId)) {
            return Result.failure(Exception("Anti-Cheat: Betting is strictly locked for period $periodId (5s lock before result draw)."))
        }

        val totalCost = chipAmount * multiplier
        val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not initialized"))

        if (wallet.totalBalance < totalCost) {
            return Result.failure(Exception("Insufficient total balance (₹${wallet.totalBalance.toInt()}) for bet ₹${totalCost.toInt()}"))
        }

        // Deduct priority: Bonus balance first -> Main balance -> Winnings balance
        var remainingCost = totalCost
        var newBonus = wallet.bonusBalance
        var newMain = wallet.mainBalance
        var newWinnings = wallet.winningsBalance

        if (newBonus > 0) {
            val deductBonus = minOf(newBonus, remainingCost)
            newBonus -= deductBonus
            remainingCost -= deductBonus
        }
        if (remainingCost > 0 && newMain > 0) {
            val deductMain = minOf(newMain, remainingCost)
            newMain -= deductMain
            remainingCost -= deductMain
        }
        if (remainingCost > 0) {
            newWinnings -= remainingCost
        }

        val updatedWallet = wallet.copy(
            bonusBalance = maxOf(0.0, newBonus),
            mainBalance = maxOf(0.0, newMain),
            winningsBalance = maxOf(0.0, newWinnings),
            totalBetsCount = wallet.totalBetsCount + 1
        )
        dao.updateWallet(updatedWallet)

        val bet = UserBet(
            periodId = periodId,
            gameMode = gameMode.name,
            betTargetKey = target.toKey(),
            chipAmount = chipAmount,
            multiplier = multiplier,
            totalAmount = totalCost,
            status = BetStatus.PENDING,
            winAmount = 0.0
        )
        val betId = dao.insertBet(bet)

        val now = System.currentTimeMillis()
        val jwt = TransactionCryptoEngine.generateJwtToken(profile.userId, betId, "BET_PLACED", totalCost)
        val hash = TransactionCryptoEngine.generateSha256Receipt(periodId, profile.userId, totalCost, now)

        dao.insertTransaction(
            TransactionRecord(
                type = "BET_PLACED",
                category = "BET",
                amount = -totalCost,
                description = "Bet on ${target.displayLabel} (Period $periodId)",
                jwtToken = jwt,
                securityHash = hash,
                isEncrypted = true
            )
        )

        // Award VIP XP (1 XP per ₹10 wagered)
        val addedXp = (totalCost / 10).toInt().coerceAtLeast(1)
        val updatedProfile = profile.copy(
            vipXp = profile.vipXp + addedXp,
            vipLevel = minOf(10, 1 + (profile.vipXp + addedXp) / 500)
        )
        dao.updateUserProfile(updatedProfile)

        return Result.success(bet.copy(id = betId))
    }

    private var forcedNextResult: Int? = null

    fun setForcedNextResult(number: Int?) {
        forcedNextResult = number
    }

    fun getForcedNextResult(): Int? = forcedNextResult

    fun getPendingWithdrawalsFlow(): Flow<List<TransactionRecord>> = dao.getPendingWithdrawalsFlow()

    suspend fun approveWithdrawal(id: Long) {
        dao.updateTransactionStatus(id, "COMPLETED")
    }

    suspend fun rejectWithdrawal(id: Long, amount: Double) {
        dao.updateTransactionStatus(id, "REJECTED")
        // Refund withdrawal amount back to user's winnings
        val wallet = dao.getWalletSync()
        if (wallet != null) {
            dao.updateWallet(wallet.copy(winningsBalance = wallet.winningsBalance + amount))
        }
    }

    suspend fun adminSetWalletBalance(main: Double, bonus: Double, winnings: Double) {
        val wallet = dao.getWalletSync()
        if (wallet != null) {
            dao.updateWallet(wallet.copy(mainBalance = main, bonusBalance = bonus, winningsBalance = winnings))
        }
    }

    suspend fun adminToggleKyc(status: Boolean) {
        val profile = dao.getUserProfileSync()
        if (profile != null) {
            dao.updateUserProfile(profile.copy(kycVerified = status))
        }
    }

    suspend fun adminToggleFreeze(status: Boolean) {
        val profile = dao.getUserProfileSync()
        if (profile != null) {
            dao.updateUserProfile(profile.copy(isFrozen = status))
        }
    }

    suspend fun resolveDraw(
        periodId: String,
        mode: GameMode,
        forcedNumber: Int? = null
    ): Pair<DrawRound, Double> {
        // Enforce bet lock before result generation
        lockPeriod(periodId)

        val settings = dao.getGameSettingsSync() ?: GameSettings()
        val bets = dao.getBetsForPeriod(periodId)

        // Retrieve pre-round commitment generated on server before round started
        val commitment = activeCommitments.remove(periodId) ?: ServerRngEngine.generateCommitment(periodId)
        lockedPeriods.remove(periodId)

        val resultNumber = when {
            forcedNumber != null -> forcedNumber
            forcedNextResult != null -> forcedNextResult!!
            settings.rngControlMode == "MANUAL" && settings.manualOverrideNumber != null -> settings.manualOverrideNumber
            settings.rngControlMode == "LEAST_PAYOUT" && bets.isNotEmpty() -> {
                (0..9).minByOrNull { testNum ->
                    bets.sumOf { bet -> GameUtils.calculatePayout(bet, testNum, settings) }
                } ?: commitment.resultNumber
            }
            else -> commitment.resultNumber // Server-side SecureRandom RNG
        }
        forcedNextResult = null // Clear override after round executes

        val round = DrawRound(
            periodId = periodId,
            gameMode = mode.name,
            resultNumber = resultNumber,
            colorsString = GameUtils.calculateColorsString(resultNumber),
            sizeString = GameUtils.calculateSize(resultNumber).name,
            timestamp = System.currentTimeMillis(),
            hashProof = commitment.commitHash,
            serverSeed = commitment.serverSeed
        )
        dao.insertRound(round)

        var totalRoundWin = 0.0
        var totalRoundLoss = 0.0

        val resolvedBets = bets.map { bet ->
            val winAmount = GameUtils.calculatePayout(bet, resultNumber, settings)
            if (winAmount > 0.0) {
                totalRoundWin += winAmount
                bet.copy(status = BetStatus.WON, winAmount = winAmount)
            } else {
                totalRoundLoss += bet.totalAmount
                bet.copy(status = BetStatus.LOST, winAmount = 0.0)
            }
        }

        if (resolvedBets.isNotEmpty()) {
            dao.updateBets(resolvedBets)
        }

        // Credit winnings to Winnings Balance & accumulate VIP cashback on losses
        val profile = dao.getUserProfileSync()
        val vipTier = if (profile != null) VipSystem.getTier(profile.vipLevel) else VipSystem.TIERS.first()
        val cashbackAccrued = if (totalRoundLoss > 0.0) totalRoundLoss * (vipTier.cashbackPercent / 100.0) else 0.0
        val refRate = if (profile != null) ReferralSystem.getCommissionRate(profile.referralsCount, profile.vipLevel) else 5.0
        val commissionAccrued = if (totalRoundLoss > 0.0) (totalRoundLoss * 0.10) * (refRate / 100.0) else 0.0

        if (totalRoundWin > 0.0) {
            val wallet = dao.getWalletSync()
            if (wallet != null) {
                val updatedWallet = wallet.copy(
                    winningsBalance = wallet.winningsBalance + totalRoundWin,
                    totalWon = wallet.totalWon + totalRoundWin,
                    totalLost = wallet.totalLost + totalRoundLoss,
                    unclaimedCashback = wallet.unclaimedCashback + cashbackAccrued,
                    referralCommissionBalance = wallet.referralCommissionBalance + commissionAccrued
                )
                dao.updateWallet(updatedWallet)
            }
            dao.insertTransaction(
                TransactionRecord(
                    type = "BET_WON",
                    category = "WIN",
                    amount = totalRoundWin,
                    description = "Won period $periodId ($resultNumber ${round.colorsString})"
                )
            )
        } else if (totalRoundLoss > 0.0) {
            val wallet = dao.getWalletSync()
            if (wallet != null) {
                val updatedWallet = wallet.copy(
                    totalLost = wallet.totalLost + totalRoundLoss,
                    unclaimedCashback = wallet.unclaimedCashback + cashbackAccrued,
                    referralCommissionBalance = wallet.referralCommissionBalance + commissionAccrued
                )
                dao.updateWallet(updatedWallet)
            }
        }

        return Pair(round, totalRoundWin)
    }

    suspend fun deposit(method: String, amount: Double): Result<Double> {
        val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not found"))
        val profile = dao.getUserProfileSync()

        if (profile != null) {
            if (!profile.isAgeVerified) {
                return Result.failure(Exception("Age Verification (18+) required before depositing funds."))
            }
            if (profile.isGeoBlocked) {
                return Result.failure(Exception("Geo-Blocking Compliance: Financial operations are restricted in ${profile.stateLocation} under state regulations."))
            }
            val limitCheck = ResponsibleGamblingEngine.checkDeposit(amount, profile)
            if (limitCheck is ResponsibleLimitResult.Blocked) {
                return Result.failure(Exception("${limitCheck.title}: ${limitCheck.reason}"))
            }
        }

        val isFirst = profile?.isFirstDepositMade != true
        val bonus = DepositBonusSystem.calculateBonus(amount, isFirst)

        val updated = wallet.copy(
            mainBalance = wallet.mainBalance + amount,
            bonusBalance = wallet.bonusBalance + bonus
        )
        dao.updateWallet(updated)

        if (profile != null) {
            dao.updateUserProfile(
                profile.copy(
                    isFirstDepositMade = true,
                    currentDailyDeposit = profile.currentDailyDeposit + amount
                )
            )
        }

        val now = System.currentTimeMillis()
        val jwt = TransactionCryptoEngine.generateJwtToken(profile?.userId ?: "CW_782910", now, "DEPOSIT", amount)
        val hash = TransactionCryptoEngine.generateSha256Receipt("DEP_$now", profile?.userId ?: "CW_782910", amount, now)

        dao.insertTransaction(
            TransactionRecord(
                type = "DEPOSIT",
                category = "DEPOSIT",
                amount = amount,
                description = "Deposit via $method (Ref #${Random.nextInt(100000, 999999)})",
                status = "COMPLETED",
                jwtToken = jwt,
                securityHash = hash,
                isEncrypted = true
            )
        )

        if (bonus > 0.0) {
            val bonusJwt = TransactionCryptoEngine.generateJwtToken(profile?.userId ?: "CW_782910", now + 1, "DEPOSIT_BONUS", bonus)
            dao.insertTransaction(
                TransactionRecord(
                    type = "DEPOSIT_BONUS",
                    category = "BONUS",
                    amount = bonus,
                    description = if (isFirst) "First Deposit 100% Match Bonus" else "Tiered Deposit Bonus (+${((bonus / amount) * 100).toInt()}%)",
                    status = "COMPLETED",
                    jwtToken = bonusJwt,
                    securityHash = hash,
                    isEncrypted = true
                )
            )
        }

        return Result.success(updated.totalBalance)
    }

    suspend fun claimVipCashback(): Result<Double> {
        val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not found"))
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))

        val amountToClaim = wallet.unclaimedCashback
        if (amountToClaim < 1.0) {
            return Result.failure(Exception("No cashback available to claim yet (Min ₹1). Place wagers to earn up to 12% loss cashback!"))
        }

        val updatedWallet = wallet.copy(
            winningsBalance = wallet.winningsBalance + amountToClaim,
            unclaimedCashback = 0.0,
            lastCashbackClaimTimestamp = System.currentTimeMillis()
        )
        dao.updateWallet(updatedWallet)

        val updatedProfile = profile.copy(
            totalCashbackClaimed = profile.totalCashbackClaimed + amountToClaim
        )
        dao.updateUserProfile(updatedProfile)

        val now = System.currentTimeMillis()
        val jwt = TransactionCryptoEngine.generateJwtToken(profile.userId, now, "VIP_CASHBACK", amountToClaim)
        val hash = TransactionCryptoEngine.generateSha256Receipt("CB_$now", profile.userId, amountToClaim, now)

        dao.insertTransaction(
            TransactionRecord(
                type = "VIP_CASHBACK",
                category = "WIN",
                amount = amountToClaim,
                description = "VIP ${profile.vipLevel} Daily Loss Cashback Claimed",
                jwtToken = jwt,
                securityHash = hash,
                isEncrypted = true
            )
        )
        return Result.success(amountToClaim)
    }

    suspend fun transferReferralCommission(amount: Double): Result<Double> {
        val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not found"))
        if (wallet.referralCommissionBalance < amount) {
            return Result.failure(Exception("Insufficient commission balance (Available: ₹${wallet.referralCommissionBalance.toInt()})"))
        }

        val updatedWallet = wallet.copy(
            mainBalance = wallet.mainBalance + amount,
            referralCommissionBalance = wallet.referralCommissionBalance - amount
        )
        dao.updateWallet(updatedWallet)

        val now = System.currentTimeMillis()
        val profile = dao.getUserProfileSync()
        val jwt = TransactionCryptoEngine.generateJwtToken(profile?.userId ?: "CW_782910", now, "COMMISSION_TRANSFER", amount)
        val hash = TransactionCryptoEngine.generateSha256Receipt("COMM_$now", profile?.userId ?: "CW_782910", amount, now)

        dao.insertTransaction(
            TransactionRecord(
                type = "COMMISSION_TRANSFER",
                category = "BONUS",
                amount = amount,
                description = "Referral Commission Transferred to Main Balance",
                jwtToken = jwt,
                securityHash = hash,
                isEncrypted = true
            )
        )
        return Result.success(amount)
    }

    suspend fun withdraw(amount: Double, method: String, destination: String): Result<Double> {
        if (amount < 100.0) {
            return Result.failure(Exception("Minimum withdrawal amount is ₹100"))
        }

        val profile = dao.getUserProfileSync()
        if (profile?.isLoggedIn != true) {
            return Result.failure(Exception("Please login to withdraw funds"))
        }

        // 1. AML / KYC Compliance check (Mandatory Tier-2 AML for withdrawals > ₹10,000)
        val amlCheck = AmlComplianceEngine.validateWithdrawal(amount, profile)
        when (amlCheck) {
            is AmlCheckResult.KycRequired -> return Result.failure(Exception(amlCheck.message))
            is AmlCheckResult.AmlTier2Required -> return Result.failure(Exception(amlCheck.message))
            is AmlCheckResult.Approved -> { /* Approved */ }
        }

        val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not found"))
        if (wallet.winningsBalance < amount) {
            return Result.failure(Exception("Insufficient Winnings balance (Available: ₹${wallet.winningsBalance.toInt()}). Winnings can be withdrawn!"))
        }

        val updated = wallet.copy(winningsBalance = wallet.winningsBalance - amount)
        dao.updateWallet(updated)

        val now = System.currentTimeMillis()
        val jwt = TransactionCryptoEngine.generateJwtToken(profile.userId, now, "WITHDRAWAL", amount)
        val hash = TransactionCryptoEngine.generateSha256Receipt("WD_$now", profile.userId, amount, now)

        val amlNote = if (amount > AmlComplianceEngine.AML_THRESHOLD_INR) " [AML Tier-2 Verified]" else ""

        dao.insertTransaction(
            TransactionRecord(
                type = "WITHDRAWAL",
                category = "WITHDRAWAL",
                amount = -amount,
                description = "Withdrawal to $destination ($method)$amlNote - Processing within 24h",
                status = "PROCESSING",
                jwtToken = jwt,
                securityHash = hash,
                isEncrypted = true
            )
        )
        return Result.success(updated.winningsBalance)
    }

    suspend fun setGeoLocation(state: String): Result<UserProfile> {
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val check = GeoComplianceEngine.checkCompliance(state)
        val isBlocked = check is GeoCheckResult.Blocked
        val updated = profile.copy(stateLocation = state, isGeoBlocked = isBlocked)
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun verifyAge(dob: String): Result<UserProfile> {
        val check = AgeVerificationEngine.calculateAge(dob)
        if (check is AgeCheckResult.Underage) {
            return Result.failure(Exception(check.reason))
        }
        if (check is AgeCheckResult.InvalidFormat) {
            return Result.failure(Exception(check.message))
        }
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val updated = profile.copy(isAgeVerified = true, dateOfBirth = dob)
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun updateResponsibleLimits(
        depositLimit: Double,
        singleBetLimit: Double,
        dailyLossLimit: Double,
        sessionMins: Int
    ): Result<UserProfile> {
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val updated = profile.copy(
            dailyDepositLimit = depositLimit,
            singleBetLimit = singleBetLimit,
            dailyLossLimit = dailyLossLimit,
            sessionTimeLimitMinutes = sessionMins
        )
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun applySelfExclusion(durationHours: Int): Result<UserProfile> {
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val until = System.currentTimeMillis() + (durationHours * 3600 * 1000L)
        val updated = profile.copy(selfExclusionUntil = until)
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun submitAmlKyc(pan: String, aadhaar: String): Result<UserProfile> {
        val panPattern = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        if (!panPattern.matches(pan.trim().uppercase())) {
            return Result.failure(Exception("Invalid PAN Card format (expected 10 characters e.g. ABCDE1234F)"))
        }
        val profile = dao.getUserProfileSync() ?: return Result.failure(Exception("Profile not found"))
        val updated = profile.copy(
            kycPan = pan.trim().uppercase(),
            kycAadhaar = aadhaar.trim(),
            kycVerified = true,
            isAmlVerified = true
        )
        dao.updateUserProfile(updated)
        return Result.success(updated)
    }

    suspend fun claimDailyBonus(bonusAmount: Double = 500.0): Boolean {
        val wallet = dao.getWalletSync() ?: return false
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (now - wallet.lastDailyClaimTimestamp < oneDayMillis && wallet.lastDailyClaimTimestamp != 0L) {
            return false
        }

        val updated = wallet.copy(
            bonusBalance = wallet.bonusBalance + bonusAmount,
            lastDailyClaimTimestamp = now
        )
        dao.updateWallet(updated)
        dao.insertTransaction(
            TransactionRecord(
                type = "DAILY_BONUS",
                category = "BONUS",
                amount = bonusAmount,
                description = "Daily VIP Reward Bonus"
            )
        )
        return true
    }
}
