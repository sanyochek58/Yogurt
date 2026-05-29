package com.example.client.feature.auth.data

import com.example.client.feature.auth.domain.AuthRepository
import com.yogurtvpn.client.core.network.YogurtApi
import com.yogurtvpn.client.core.storage.TokenStorage
import com.yogurtvpn.client.feature.auth.domain.AuthResult
import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl(
    private val api: YogurtApi,
    private val storage: TokenStorage
): AuthRepository {

    override suspend fun register(email: String, password: String): AuthResult {
        val response = api.register(email, password)
        storage.saveAuth(response.token, response.userId, response.email)
        return AuthResult(response.token, response.userId, response.email, response.role)
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val response = api.login(email, password)
        storage.saveAuth(response.token, response.userId, response.email)
        return AuthResult(response.token, response.userId, response.email, response.role)
    }

    override suspend fun logout() {
        storage.clear()
    }

    override suspend fun getSavedToken(): String? =
        storage.token.firstOrNull()
}