package com.yogurtvpn.features.auth.domain

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

enum class UserRole {
    ADMIN,
    USER;

    companion object {
        fun fromString(value: String): UserRole = entries.find{
            it.name == value.uppercase()
        } ?: throw IllegalArgumentException("Unknown user role: $value")
    }
}


