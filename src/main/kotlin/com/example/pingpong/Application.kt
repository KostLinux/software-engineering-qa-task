package com.example.pingpong

import com.example.pingpong.api.performanceRoutes
import com.example.pingpong.api.pingRoutes
import com.example.pingpong.client.PlaceholderClient
import com.example.pingpong.config.AppConfig
import com.example.pingpong.domain.PerformanceService
import com.example.pingpong.plugins.configureErrorHandling
import com.example.pingpong.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = AppConfig.port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(XForwardedHeaders)
    install(CallLogging)
    configureSerialization()
    configureErrorHandling()

    val client = PlaceholderClient(AppConfig.upstreamBaseUrl)
    val service = PerformanceService(client)

    environment.monitor.subscribe(ApplicationStopped) { client.close() }

    routing {
        pingRoutes()
        performanceRoutes(service)
    }
}
