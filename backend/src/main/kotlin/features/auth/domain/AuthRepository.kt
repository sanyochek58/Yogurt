package com.yogurtvpn.features.auth.domain

import java.util.UUID

interface AuthRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun create(email: String, passwordHash: String): User
    suspend fun existsByEmail(email: String): Boolean
}