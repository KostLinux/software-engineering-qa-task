package com.example.pingpong.api

import com.example.pingpong.config.AppConfig
import com.example.pingpong.domain.LeaderboardEntry
import com.example.pingpong.domain.PerformanceService
import com.example.pingpong.domain.UserPerformance
import com.example.pingpong.plugins.RateLimiting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.performanceRoutes(service: PerformanceService) {
    val performanceCache = ResponseCache<UserPerformance>()
    val leaderboardCache = ResponseCache<List<LeaderboardEntry>>()

    route("/api/v1") {
        install(RateLimiting)

        get(Regex("""/users/(?<id>\d+)/performance""")) {
            val id = call.parameters["id"]!!.toInt()
            call.respond(performanceCache.getOrPut(call) { service.performanceFor(id) })
        }

        get("/leaderboard") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: AppConfig.LEADERBOARD_DEFAULT_LIMIT
            require(limit in 1..AppConfig.LEADERBOARD_MAX_LIMIT) {
                "limit must be between 1 and ${AppConfig.LEADERBOARD_MAX_LIMIT}"
            }
            call.respond(leaderboardCache.getOrPut(call) { service.leaderboard(limit) })
        }
    }
}
