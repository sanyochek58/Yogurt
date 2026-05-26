package com.yogurtvpn

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val version: String
)

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "OK!",
                    version = "1.0.0"
                )
            )
        }
    }
}
