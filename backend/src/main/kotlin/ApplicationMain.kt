package com.yogurtvpn

import com.yogurtvpn.features.access.data.AccessRequestRepositoryImpl
import com.yogurtvpn.features.access.domain.AccessRequestService
import com.yogurtvpn.features.auth.data.AuthRepositoryImpl
import com.yogurtvpn.features.auth.domain.AuthService
import com.yogurtvpn.features.auth.domain.JwtService
import com.yogurtvpn.features.vpn.data.EmailServiceImpl
import com.yogurtvpn.features.vpn.data.VpnConfigRepositoryImpl
import com.yogurtvpn.features.vpn.domain.VpnConfigService
import com.yogurtvpn.plugins.configureSecurity
import com.yogurtvpn.plugins.configureStatusPages
import db.migration.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(){

    val jdbcUrl = environment.config.property("db.jdbcUrl").getString()
    val username = environment.config.property("db.username").getString()
    val password = environment.config.property("db.password").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val serverHost = environment.config.property("xray.serverHost").getString()
    val serverPort = environment.config.property("xray.serverPort").getString().toInt()
    val publicKey = environment.config.property("xray.publicKey").getString()
    val shortId = environment.config.property("xray.shortId").getString()

    DatabaseFactory.init(jdbcUrl, username, password)

    val authRepository = AuthRepositoryImpl()
    val accessRequestRepository = AccessRequestRepositoryImpl()
    val vpnConfigRepository = VpnConfigRepositoryImpl()


    val jwtService = JwtService(jwtSecret,jwtIssuer, jwtAudience)
    val authService = AuthService(authRepository)
    val emailService = EmailServiceImpl()
    val vpnConfigService = VpnConfigService(
        repository = vpnConfigRepository,
        serverHost = serverHost,
        serverPort = serverPort,
        publicKey = publicKey,
        shortId = shortId,
        emailService = emailService,
    )

    val accessRequestService = AccessRequestService(
        repository = accessRequestRepository,
        vpnConfigService = vpnConfigService,
    )

    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureSecurity(jwtSecret, jwtIssuer, jwtAudience)
    configureRouting(authService, jwtService, accessRequestService)
}