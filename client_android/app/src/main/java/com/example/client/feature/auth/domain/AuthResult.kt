package com.yogurtvpn.client.feature.auth.domain


data class AuthResult(
    val token: String,
    val userId: String,
    val email: String,
    val role: String
)
