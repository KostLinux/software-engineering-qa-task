package com.example.pingpong.config

object AppConfig {
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val upstreamBaseUrl: String = System.getenv("UPSTREAM_BASE_URL") ?: "https://jsonplaceholder.typicode.com"

    const val RATE_LIMIT_REQUESTS = 20
    const val RATE_LIMIT_WINDOW_SECONDS = 60L

    const val CACHE_TTL_SECONDS = 60L
    const val PING_STATS_WINDOW_SECONDS = 60L

    const val FIRST_USER_ID = 1
    const val LAST_USER_ID = 10

    const val LEADERBOARD_DEFAULT_LIMIT = 5
    const val LEADERBOARD_MAX_LIMIT = 10

    const val BASE_BONUS = 1000.0
    const val BONUS_PER_POST = 10.0
    const val MAX_BONUS = 2000.0
    const val CURRENCY = "EUR"
}
