package com.yogurtvpn.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
)

private val logger = LoggerFactory.getLogger("StatusPages")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException>{ call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", cause.message ?: "INVALID REQUEST")
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", cause.message ?: "NOT_FOUND"))
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception: ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_SERVER_ERROR", "An internal server error occurred"))
        }
    }
}

class NotFoundException(message: String) : Exception(message)