package com.example.pingpong.plugins

import com.example.pingpong.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RateLimitConfig {
    var limit: Int = AppConfig.RATE_LIMIT_REQUESTS
    var windowSeconds: Long = AppConfig.RATE_LIMIT_WINDOW_SECONDS
}

private class Window(val startedAt: Long) {
    val count = AtomicInteger(0)
}

val RateLimiting = createRouteScopedPlugin("RateLimiting", ::RateLimitConfig) {
    val limit = pluginConfig.limit
    val windowMillis = pluginConfig.windowSeconds * 1000
    val windows = ConcurrentHashMap<String, Window>()

    onCall { call ->
        val now = System.currentTimeMillis()
        val window = windows.compute(call.request.origin.remoteHost) { _, existing ->
            if (existing == null || now - existing.startedAt >= windowMillis) Window(now) else existing
        }!!

        val used = window.count.incrementAndGet()
        if (used > limit) {
            val retryAfter = ((window.startedAt + windowMillis - now) / 1000).coerceAtLeast(1)
            call.response.header(HttpHeaders.RetryAfter, retryAfter)
        }
        check(used <= limit) { "Rate limit of $limit requests exceeded" }
    }
}
