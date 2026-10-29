package com.example.pingpong.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import java.io.IOException

@Serializable
data class ErrorResponse(val error: String, val message: String)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_request", cause.message ?: "Invalid request"))
        }
        exception<IllegalStateException> { call, cause ->
            call.application.environment.log.warn("Upstream call failed: {}", cause.message)
            call.respond(HttpStatusCode.BadGateway, ErrorResponse("upstream_unavailable", "Upstream service unavailable"))
        }
        exception<IOException> { call, cause ->
            call.application.environment.log.warn("Upstream unreachable: {}", cause.message)
            call.respond(HttpStatusCode.BadGateway, ErrorResponse("upstream_unavailable", "Upstream service unavailable"))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Something went wrong"))
        }
    }
}
