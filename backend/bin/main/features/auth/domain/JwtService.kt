package com.yogurtvpn.features.auth.domain

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.jetbrains.exposed.sql.Except
import java.util.Date
import java.util.UUID

class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val expirationsMs: Long = 24*60*60*1000L
) {

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(user:User): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(user.id.toString())
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationsMs))
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT? =
        try{
            JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)
        }catch (e: Exception){
            null
        }

    fun extractUserId(jwt: DecodedJWT): UUID =
        UUID.fromString(jwt.subject)
}