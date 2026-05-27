package com.yogurtvpn

import com.yogurtvpn.features.auth.domain.AuthService
import com.yogurtvpn.features.auth.domain.JwtService
import com.yogurtvpn.features.auth.presentation.authRoutes
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

fun Application.configureRouting(authService: AuthService, jwtService: JwtService) {
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
        authRoutes(authService, jwtService)
    }
}
