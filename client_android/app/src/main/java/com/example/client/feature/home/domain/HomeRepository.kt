package com.yogurtvpn.client.feature.home.domain

interface HomeRepository {
    suspend fun getAccessStatus(token: String): AccessStatus
    suspend fun requestAccess(token: String): Boolean
    suspend fun getVpnConfig(token: String): String?
}