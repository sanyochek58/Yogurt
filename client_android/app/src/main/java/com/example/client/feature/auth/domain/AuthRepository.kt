package com.example.client.feature.auth.domain

import com.yogurtvpn.client.feature.auth.domain.AuthResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(email: String, password: String): AuthResult
    suspend fun logout()
    suspend fun getSavedToken(): String?
}