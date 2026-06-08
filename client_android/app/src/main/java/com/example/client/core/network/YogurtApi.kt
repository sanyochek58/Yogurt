package com.yogurtvpn.client.core.network

import com.yogurtvpn.client.core.network.dto.AccessRequestResponse
import com.yogurtvpn.client.core.network.dto.AuthResponse
import com.yogurtvpn.client.core.network.dto.LoginRequest
import com.yogurtvpn.client.core.network.dto.RegisterRequest
import com.yogurtvpn.client.core.network.dto.VpnConfigResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType

class YogurtApi(
    private val client: HttpClient,
    private val baseUrl: String = "https://yogurtdev.site"
) {

    suspend fun register(email: String, password: String): AuthResponse =
        client.post("$baseUrl/api/v1/auth/register"){
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password))
        }.body()

    suspend fun login(email: String, password: String): AuthResponse =
        client.post("$baseUrl/api/v1/auth/login"){
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun requestAccess(token: String): AccessRequestResponse =
        client.post("$baseUrl/api/v1/user/access-requests"){
            bearerAuth(token)
        }.body()

    suspend fun getMyAccessRequest(token: String): AccessRequestResponse =
        client.get("$baseUrl/api/v1/user/access-requests/my"){
            bearerAuth(token)
        }.body()

    suspend fun getVpnConfig(token: String): VpnConfigResponse =
        client.get("$baseUrl/api/v1/user/vpn-config"){
            bearerAuth(token)
        }.body()
}