package com.example.pingpong.api

import com.example.pingpong.metrics.PingSample
import com.example.pingpong.metrics.PingStats
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pingRoutes() {
    get("/ping") {
        val startedAt = System.nanoTime()
        call.respondText("pong")
        PingStats.record(PingSample(startedAt, System.nanoTime() - startedAt))
    }

    get("/ping/stats") {
        call.respond(PingStats.snapshot())
    }
}
