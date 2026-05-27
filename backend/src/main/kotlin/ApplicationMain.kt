package com.yogurtvpn

import com.yogurtvpn.features.auth.data.AuthRepositoryImpl
import com.yogurtvpn.features.auth.domain.AuthService
import com.yogurtvpn.features.auth.domain.JwtService
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

    DatabaseFactory.init(jdbcUrl, username, password)

    val authRepository = AuthRepositoryImpl()
    val jwtService = JwtService(jwtSecret,jwtIssuer, jwtAudience)
    val authService = AuthService(authRepository)

    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureSecurity(jwtSecret, jwtIssuer, jwtAudience)
    configureRouting(authService, jwtService)
}