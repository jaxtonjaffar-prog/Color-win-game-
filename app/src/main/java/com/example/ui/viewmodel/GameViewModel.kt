package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class DrawCelebration(
    val round: DrawRound,
    val winAmount: Double,
    val hadBets: Boolean
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _selectedMode = MutableStateFlow(GameMode.WINGO_1MIN)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _currentPeriodId = MutableStateFlow("")
    val currentPeriodId: StateFlow<String> = _currentPeriodId.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isBettingLocked = MutableStateFlow(false)
    val isBettingLocked: StateFlow<Boolean> = _isBettingLocked.asStateFlow()

    private val _selectedBetTarget = MutableStateFlow<BetTarget?>(null)
    val selectedBetTarget: StateFlow<BetTarget?> = _selectedBetTarget.asStateFlow()

    private val _celebration = MutableStateFlow<DrawCelebration?>(null)
    val celebration: StateFlow<DrawCelebration?> = _celebration.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _messageSnackbar = MutableSharedFlow<String>()
    val messageSnackbar: SharedFlow<String> = _messageSnackbar.asSharedFlow()

    private val _forcedResult = MutableStateFlow<Int?>(null)
    val forcedResult: StateFlow<Int?> = _forcedResult.asStateFlow()

    val wallet: StateFlow<WalletAccount?>
    val userProfile: StateFlow<UserProfile?>
    val allBets: StateFlow<List<UserBet>>
    val recentRounds: StateFlow<List<DrawRound>>
    val allTransactions: StateFlow<List<TransactionRecord>>
    val pendingWithdrawals: StateFlow<List<TransactionRecord>>
    val gameSettings: StateFlow<GameSettings>

    val activeRoundUserBets: StateFlow<List<UserBet>>

    private var timerJob: Job? = null
    private var currentRoundIndex: Long = 101L

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameDao())

        wallet = repository.getWalletFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        userProfile = repository.getUserProfileFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        allBets = repository.getAllBetsFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allTransactions = repository.getTransactionsFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingWithdrawals = repository.getPendingWithdrawalsFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        gameSettings = repository.getGameSettingsFlow().map { it ?: GameSettings() }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GameSettings()
        )

        recentRounds = _selectedMode.flatMapLatest { mode ->
            repository.getRecentRoundsFlow(mode)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeRoundUserBets = combine(allBets, _currentPeriodId) { bets, periodId ->
            bets.filter { it.periodId == periodId }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeIfEmpty()
            currentRoundIndex = repository.getNextRoundIndex(_selectedMode.value)
            startNewPeriod()
        }
    }

    fun selectMode(mode: GameMode) {
        if (_selectedMode.value == mode) return
        _selectedMode.value = mode
        viewModelScope.launch {
            currentRoundIndex = repository.getNextRoundIndex(mode)
            startNewPeriod()
        }
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    private fun startNewPeriod() {
        timerJob?.cancel()
        val mode = _selectedMode.value
        val periodId = GameUtils.generatePeriodId(mode, currentRoundIndex)
        _currentPeriodId.value = periodId
        _remainingSeconds.value = mode.seconds
        _isBettingLocked.value = false

        timerJob = viewModelScope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000L)
                val newSeconds = _remainingSeconds.value - 1
                _remainingSeconds.value = newSeconds

                if (newSeconds <= 5 && !_isBettingLocked.value) {
                    _isBettingLocked.value = true
                    vibrateHaptic(100)
                }

                if (newSeconds in 1..5) {
                    vibrateTick()
                }

                if (newSeconds == 0) {
                    performDraw(periodId, mode)
                }
            }
        }
    }

    private suspend fun performDraw(periodId: String, mode: GameMode) {
        vibrateHaptic(300)
        val (round, totalWin) = repository.resolveDraw(periodId, mode)
        val betsOnPeriod = repository.getAllBetsFlow().first().filter { it.periodId == periodId }
        val hadBets = betsOnPeriod.isNotEmpty()

        _celebration.value = DrawCelebration(
            round = round,
            winAmount = totalWin,
            hadBets = hadBets
        )

        if (totalWin > 0) {
            vibrateSuccess()
        }

        currentRoundIndex++
        delay(2500L)
        startNewPeriod()
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    fun openBetSheet(target: BetTarget) {
        if (userProfile.value?.isLoggedIn != true) {
            viewModelScope.launch {
                _messageSnackbar.emit("Guest browsing mode! Please Login to place wagers.")
            }
            return
        }
        if (_isBettingLocked.value) {
            viewModelScope.launch {
                _messageSnackbar.emit("Betting closed for this round! Wait for next round.")
            }
            return
        }
        _selectedBetTarget.value = target
    }

    fun closeBetSheet() {
        _selectedBetTarget.value = null
    }

    fun placeBet(chipAmount: Double, multiplier: Int) {
        val target = _selectedBetTarget.value ?: return
        val currentPeriod = _currentPeriodId.value
        val mode = _selectedMode.value

        if (_isBettingLocked.value) {
            viewModelScope.launch {
                _messageSnackbar.emit("Betting closed for this round! Wait for next round.")
            }
            closeBetSheet()
            return
        }

        viewModelScope.launch {
            val result = repository.placeBet(currentPeriod, mode, target, chipAmount, multiplier)
            result.onSuccess {
                vibrateHaptic(50)
                _messageSnackbar.emit("Bet placed: ₹${(chipAmount * multiplier).toInt()} on ${target.displayLabel}")
                closeBetSheet()
            }.onFailure { err ->
                _messageSnackbar.emit(err.message ?: "Failed to place bet")
            }
        }
    }

    fun repeatLastBet() {
        if (userProfile.value?.isLoggedIn != true) {
            viewModelScope.launch {
                _messageSnackbar.emit("Please Login to place wagers!")
            }
            return
        }
        if (_isBettingLocked.value) {
            viewModelScope.launch {
                _messageSnackbar.emit("Betting closed for this round!")
            }
            return
        }

        viewModelScope.launch {
            val lastBet = repository.getLastBet()
            if (lastBet == null) {
                _messageSnackbar.emit("No previous bet found to repeat!")
                return@launch
            }
            val result = repository.placeBet(
                _currentPeriodId.value,
                _selectedMode.value,
                lastBet.target,
                lastBet.chipAmount,
                lastBet.multiplier
            )
            result.onSuccess {
                vibrateHaptic(50)
                _messageSnackbar.emit("Repeated Bet: ₹${lastBet.totalAmount.toInt()} on ${lastBet.target.displayLabel}")
            }.onFailure {
                _messageSnackbar.emit(it.message ?: "Failed to repeat bet")
            }
        }
    }

    fun placeRandomBet(amount: Double = 100.0) {
        if (userProfile.value?.isLoggedIn != true) {
            viewModelScope.launch {
                _messageSnackbar.emit("Please Login to place wagers!")
            }
            return
        }
        if (_isBettingLocked.value) {
            viewModelScope.launch {
                _messageSnackbar.emit("Betting closed for this round!")
            }
            return
        }

        val randomChoices: List<BetTarget> = listOf(
            BetTarget.ColorBet(ColorChoice.GREEN),
            BetTarget.ColorBet(ColorChoice.RED),
            BetTarget.ColorBet(ColorChoice.VIOLET),
            BetTarget.SizeBet(SizeChoice.BIG),
            BetTarget.SizeBet(SizeChoice.SMALL),
            BetTarget.NumberBet(Random.nextInt(0, 10))
        )
        val pickedTarget = randomChoices.random()

        viewModelScope.launch {
            val result = repository.placeBet(
                _currentPeriodId.value,
                _selectedMode.value,
                pickedTarget,
                amount,
                1
            )
            result.onSuccess {
                vibrateHaptic(50)
                _messageSnackbar.emit("Lucky Bet Placed: ₹${amount.toInt()} on ${pickedTarget.displayLabel}!")
            }.onFailure {
                _messageSnackbar.emit(it.message ?: "Failed to place bet")
            }
        }
    }

    fun login(phoneOrEmail: String, referralCode: String?) {
        viewModelScope.launch {
            repository.loginWithOtp(phoneOrEmail, referralCode)
            vibrateSuccess()
            _messageSnackbar.emit("Welcome back, $phoneOrEmail! Logged in.")
        }
    }

    fun setGuestMode() {
        viewModelScope.launch {
            repository.setGuestMode()
            _messageSnackbar.emit("Switched to Guest Mode (Browsing only)")
        }
    }

    fun submitKyc(pan: String, aadhaar: String) {
        viewModelScope.launch {
            repository.submitKyc(pan, aadhaar)
            vibrateSuccess()
            _messageSnackbar.emit("KYC Submitted & Verified Successfully!")
        }
    }

    fun linkBank(account: String, ifsc: String, holder: String, upi: String) {
        viewModelScope.launch {
            repository.linkBankDetails(account, ifsc, holder, upi)
            vibrateSuccess()
            _messageSnackbar.emit("Bank and UPI linked successfully!")
        }
    }

    fun deposit(method: String, amount: Double) {
        viewModelScope.launch {
            val res = repository.deposit(method, amount)
            res.onSuccess {
                vibrateSuccess()
                _messageSnackbar.emit("₹${amount.toInt()} deposited via $method! Balance updated.")
            }
        }
    }

    fun withdraw(amount: Double, method: String, destination: String) {
        viewModelScope.launch {
            val res = repository.withdraw(amount, method, destination)
            res.onSuccess {
                vibrateSuccess()
                _messageSnackbar.emit("Withdrawal request of ₹${amount.toInt()} placed! Processing within 24h.")
            }.onFailure {
                _messageSnackbar.emit(it.message ?: "Withdrawal failed")
            }
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            val success = repository.claimDailyBonus(500.0)
            if (success) {
                vibrateSuccess()
                _messageSnackbar.emit("Claimed ₹500 Daily VIP Bonus!")
            } else {
                _messageSnackbar.emit("Daily bonus already claimed today. Come back tomorrow!")
            }
        }
    }

    fun overrideNextResult(number: Int?) {
        _forcedResult.value = number
        repository.setForcedNextResult(number)
        viewModelScope.launch {
            if (number != null) {
                _messageSnackbar.emit("Admin Override: Next draw forced to Number $number!")
            } else {
                _messageSnackbar.emit("Admin Override Cleared: Auto Random Draw")
            }
        }
    }

    fun approveWithdrawal(id: Long) {
        viewModelScope.launch {
            repository.approveWithdrawal(id)
            vibrateSuccess()
            _messageSnackbar.emit("Withdrawal #$id Approved! Payout processed.")
        }
    }

    fun rejectWithdrawal(id: Long, amount: Double) {
        viewModelScope.launch {
            repository.rejectWithdrawal(id, amount)
            _messageSnackbar.emit("Withdrawal #$id Rejected. ₹${amount.toInt()} refunded.")
        }
    }

    fun adminSetBalance(main: Double, bonus: Double, win: Double) {
        viewModelScope.launch {
            repository.adminSetWalletBalance(main, bonus, win)
            vibrateSuccess()
            _messageSnackbar.emit("Admin: Wallet balances updated!")
        }
    }

    fun adminToggleKyc(status: Boolean) {
        viewModelScope.launch {
            repository.adminToggleKyc(status)
            _messageSnackbar.emit("Admin: KYC verification status changed to $status")
        }
    }

    fun updateGameSettings(settings: GameSettings) {
        viewModelScope.launch {
            repository.updateGameSettings(settings)
            vibrateSuccess()
            _messageSnackbar.emit("Admin: Game settings & payout configuration saved!")
        }
    }

    fun adminToggleFreeze(status: Boolean) {
        viewModelScope.launch {
            repository.adminToggleFreeze(status)
            _messageSnackbar.emit("Admin: Account status is now ${if (status) "FROZEN" else "ACTIVE"}")
        }
    }

    private fun vibrateHaptic(ms: Long) {
        if (!_soundEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(ms)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateTick() {
        if (!_soundEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateSuccess() {
        if (!_soundEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (_: Exception) {}
    }
}
