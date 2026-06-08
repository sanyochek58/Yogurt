package com.yogurtvpn.features.vpn.presentation

import com.yogurtvpn.features.vpn.domain.VpnConfig
import com.yogurtvpn.features.vpn.domain.VpnConfigService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class VpnConfigResponse(
    val vlessLink: String,
    val isActive: Boolean,
)

fun Route.vpnRoutes(service: VpnConfigService) {
    authenticate("auth-user") {
        route("/api/v1/user/vpn-config") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.subject!!)

                val config = service.getConfigForUser(userId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "VPN config not found")
                    )

                call.respond(
                    HttpStatusCode.OK,
                    VpnConfigResponse(
                        vlessLink = config.vlessLink,
                        isActive = config.isActive
                    )
                )
            }
        }
    }
}