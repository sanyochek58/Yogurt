package com.yogurtvpn.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
)

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
            call.respond(HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_SERVER_ERROR", "An internal server error occurred"))
        }
    }
}

class NotFoundException(message: String) : Exception(message)