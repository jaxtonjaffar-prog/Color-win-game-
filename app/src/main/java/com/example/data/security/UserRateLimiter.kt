package com.example.data.security

import java.util.concurrent.ConcurrentHashMap

sealed class RateLimitResult {
    object Allowed : RateLimitResult()
    data class Throttled(val waitMs: Long, val reason: String) : RateLimitResult()
}

/**
 * Per-User Anti-Cheat & Fair Play Rate Limiter.
 * 
 * Protects against:
 * - Automated betting bots and macro clickers.
 * - Double-spend / race conditions during rapid bet placement.
 * - Network burst flooding.
 * 
 * Rules:
 * 1. Minimum 300ms spacing between distinct betting actions per user.
 * 2. Sliding window: Maximum 5 wagers within a 3000ms sliding window.
 * 3. 5-second cooldown if threshold is breached.
 */
class UserRateLimiter(
    private val minIntervalMs: Long = 300L,
    private val windowMs: Long = 3000L,
    private val maxRequestsPerWindow: Int = 5,
    private val cooldownPenaltyMs: Long = 5000L
) {
    private val userTimestamps = ConcurrentHashMap<String, ArrayDeque<Long>>()
    private val userLastRequest = ConcurrentHashMap<String, Long>()
    private val userCooldownUntil = ConcurrentHashMap<String, Long>()

    @Synchronized
    fun checkBettingAllowed(userId: String): RateLimitResult {
        val now = System.currentTimeMillis()

        // 1. Check if user is in penalty cooldown
        val cooldownUntil = userCooldownUntil[userId] ?: 0L
        if (now < cooldownUntil) {
            val remaining = cooldownUntil - now
            return RateLimitResult.Throttled(
                waitMs = remaining,
                reason = "Rate limit penalty active. Please wait ${(remaining / 1000.0).toString().take(3)}s."
            )
        }

        // 2. Minimum interval throttle (anti double-click / macro bot)
        val lastTime = userLastRequest[userId] ?: 0L
        val elapsedSinceLast = now - lastTime
        if (elapsedSinceLast < minIntervalMs) {
            val waitMs = minIntervalMs - elapsedSinceLast
            return RateLimitResult.Throttled(
                waitMs = waitMs,
                reason = "Betting too fast. Please slow down (wait ${waitMs}ms)."
            )
        }

        // 3. Sliding window check
        val queue = userTimestamps.computeIfAbsent(userId) { ArrayDeque() }
        // Evict expired timestamps
        while (queue.isNotEmpty() && now - queue.first() > windowMs) {
            queue.removeFirst()
        }

        if (queue.size >= maxRequestsPerWindow) {
            // Apply cooldown penalty
            userCooldownUntil[userId] = now + cooldownPenaltyMs
            return RateLimitResult.Throttled(
                waitMs = cooldownPenaltyMs,
                reason = "Exceeded $maxRequestsPerWindow wagers in 3 seconds. Anti-bot cooldown applied (5s)."
            )
        }

        // Record request
        queue.addLast(now)
        userLastRequest[userId] = now

        return RateLimitResult.Allowed
    }

    fun resetUser(userId: String) {
        userTimestamps.remove(userId)
        userLastRequest.remove(userId)
        userCooldownUntil.remove(userId)
    }
}
