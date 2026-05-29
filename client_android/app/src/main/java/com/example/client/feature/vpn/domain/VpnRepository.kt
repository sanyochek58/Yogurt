package com.yogurtvpn.client.feature.vpn.domain

interface VpnRepository {
    suspend fun saveVlessLink(link: String)
    suspend fun getSavedLink(): String?
    fun isValidVlessLink(link: String): Boolean
}