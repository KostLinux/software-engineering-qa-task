package com.example.pingpong.domain

import com.example.pingpong.config.AppConfig
import kotlinx.serialization.Serializable

enum class Tier(val minCompletionRatio: Double, val multiplier: Double) {
    GOLD(0.60, 1.5),
    SILVER(0.40, 1.25),
    BRONZE(0.00, 1.0);

    companion object {
        fun forCompletionRatio(ratio: Double): Tier = entries.first { ratio >= it.minCompletionRatio }
    }
}

object BonusCalculator {
    fun calculate(tier: Tier, postsCount: Int): Double {
        val performanceBonus = AppConfig.BASE_BONUS * tier.multiplier
        val articleBonus = postsCount * AppConfig.BONUS_PER_POST
        val total = performanceBonus + articleBonus
        return total.coerceAtMost(AppConfig.MAX_BONUS).roundTo2()
    }
}

@Serializable
data class UserPerformance(
    val userId: Int,
    val name: String,
    val email: String,
    val company: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Double,
    val tier: Tier,
    val postsCount: Int,
    val bonus: Double,
    val currency: String,
)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val name: String,
    val completionRate: Double,
    val completedTasks: Int,
    val tier: Tier,
)

val rankingOrder: Comparator<UserPerformance> =
    compareByDescending<UserPerformance> { it.completionRate }
        .thenByDescending { it.completedTasks }
        .thenByDescending { it.userId }

internal fun Double.roundTo2(): Double = Math.round(this * 100) / 100.0
