package com.yogurtvpn.features.access.presentation

import com.yogurtvpn.features.access.domain.AccessRequest
import com.yogurtvpn.features.access.domain.AccessRequestService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AccessRequestResponse(
    val id: String,
    val userId: String,
    val userEmail: String,
    val status: String,
    val requestedAt: String,
    val reviewedAt: String?,
    val reviewedBy: String?
)

fun AccessRequest.toResponse() = AccessRequestResponse(
    id = id.toString(),
    userId = userId.toString(),
    userEmail = userEmail,
    status = status.name,
    requestedAt = requestedAt.toString(),
    reviewedAt = reviewedAt?.toString(),
    reviewedBy = reviewedBy?.toString()
)

fun Route.accessRoutes(service: AccessRequestService) {
    authenticate("auth-user"){
        route("/api/v1/user/access-requests") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.subject!!)

                val request = service.requestAccess(userId)
                call.respond(HttpStatusCode.Created, request.toResponse())
            }

            get("/my") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.subject!!)

                val request = service.getMyRequest(userId)
                call.respond(HttpStatusCode.OK, request.toResponse())
            }
        }
    }

    authenticate("auth-admin"){
        route("/api/v1/admin/access-requests") {
            get {
                val requests = service.getAll()
                call.respond(HttpStatusCode.OK, requests.map { it.toResponse() })
            }

            post("/{id}/approve") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.subject!!)
                val requestId = UUID.fromString(call.parameters["id"] ?: throw IllegalArgumentException("Missing request id"))

                val approved = service.approve(requestId, userId)
                call.respond(HttpStatusCode.OK, approved.toResponse())
            }

            post("/{id}/reject") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.subject!!)
                val requestId = UUID.fromString(call.parameters["id"] ?: throw IllegalArgumentException("Missing request id"))

                val rejected = service.reject(requestId, userId)
                call.respond(HttpStatusCode.OK, rejected.toResponse())
            }
        }
    }
}