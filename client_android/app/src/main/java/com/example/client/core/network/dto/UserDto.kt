package com.yogurtvpn.client.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
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

@Serializable
data class AccessRequestResponse(
    val id: String,
    val userId: String,
    val status: String,
    val requestedAt: String,
    val reviewedAt: String? = null,
    val reviewedBy: String? = null
)

@Serializable
data class VpnConfigResponse(
    val vlessLink: String,
    val isActive: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)