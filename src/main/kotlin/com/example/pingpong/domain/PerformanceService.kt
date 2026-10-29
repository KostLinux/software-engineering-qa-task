package com.example.pingpong.domain

import com.example.pingpong.client.PlaceholderClient
import com.example.pingpong.client.Todo
import com.example.pingpong.client.User
import com.example.pingpong.config.AppConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PerformanceService(private val client: PlaceholderClient) {

    suspend fun performanceFor(userId: Int): UserPerformance = coroutineScope {
        val user = async { client.user(userId) }
        val todos = async { client.todos(userId) }
        val posts = async { client.posts(userId) }

        val ratio = completionRatio(todos.await())
        val tier = Tier.forCompletionRatio(ratio)
        val postsCount = posts.await().size

        UserPerformance(
            userId = user.await().id,
            name = user.await().name,
            email = user.await().email,
            company = user.await().company.name,
            totalTasks = todos.await().size,
            completedTasks = todos.await().count { it.completed },
            completionRate = (ratio * 100).roundTo2(),
            tier = tier,
            postsCount = postsCount,
            bonus = BonusCalculator.calculate(tier, postsCount),
            currency = AppConfig.CURRENCY,
        )
    }

    suspend fun leaderboard(limit: Int): List<LeaderboardEntry> = coroutineScope {
        client.users(AppConfig.FIRST_USER_ID, AppConfig.LAST_USER_ID)
            .map { user -> async { user to performanceFor(user.id) } }
            .awaitAll()
            .sortedWith(compareBy(rankingOrder) { it.second })
            .take(limit)
            .mapIndexed { index, (user, p) ->
                LeaderboardEntry(
                    rank = index + 1,
                    userId = p.userId,
                    name = user.username,
                    completionRate = p.completionRate,
                    completedTasks = p.completedTasks,
                    tier = p.tier,
                )
            }
    }

    private fun completionRatio(todos: List<Todo>): Double {
        if (todos.isEmpty()) return 0.0
        val perTaskShare = 1.0 / todos.size
        return todos.fold(0.0) { acc, todo -> if (todo.completed) acc + perTaskShare else acc }
    }
}
