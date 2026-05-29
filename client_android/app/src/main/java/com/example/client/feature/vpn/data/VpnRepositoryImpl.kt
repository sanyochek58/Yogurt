package com.yogurtvpn.client.feature.vpn.data

import com.yogurtvpn.client.core.storage.TokenStorage
import com.yogurtvpn.client.feature.vpn.domain.VpnRepository
import kotlinx.coroutines.flow.firstOrNull

class VpnRepositoryImpl(
    private val storage: TokenStorage
): VpnRepository {

    override suspend fun saveVlessLink(link: String) {
        storage.saveVlessLink(link)
    }

    override suspend fun getSavedLink(): String? =
        storage.vlessLink.firstOrNull()

    override fun isValidVlessLink(link: String): Boolean =
        link.startsWith("vless://") && link.contains("@")

}