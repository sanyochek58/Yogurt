package com.yogurtvpn

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

    DatabaseFactory.init(jdbcUrl, username, password)

    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureRouting()
}