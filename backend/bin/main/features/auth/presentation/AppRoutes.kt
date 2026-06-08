package com.yogurtvpn.features.auth.presentation

import com.yogurtvpn.features.auth.domain.AuthService
import com.yogurtvpn.features.auth.domain.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RegisterRequest(
    val email: String, val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val role: String
)

fun Route.authRoutes(
    authService: AuthService,
    jwtService: JwtService,
) {
    route("/api/v1/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val user = authService.register(request.email, request.password)
            val token = jwtService.generateToken(user)

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    token = token,
                    userId = user.id.toString(),
                    email = user.email,
                    role = user.role.name
                )
            )
        }

        post("/login"){
            val request = call.receive<LoginRequest>()
            val user = authService.login(request.email, request.password)
            val token = jwtService.generateToken(user)

            call.respond(HttpStatusCode.OK, AuthResponse(
                token = token,
                userId = user.id.toString(),
                email = user.email,
                role = user.role.name
            ))
        }
    }
}