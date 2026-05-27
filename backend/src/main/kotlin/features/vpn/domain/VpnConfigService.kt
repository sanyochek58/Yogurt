package com.yogurtvpn.features.vpn.domain

import java.util.UUID

class VpnConfigService(
    private val repository: VpnConfigRepository,
    private val serverHost: String,
    private val serverPort: Int,
    private val publicKey: String,
    private val shortId: String,
    private val emailService: EmailService
) {

    suspend fun createConfigForUSer(userId: UUID): VpnConfig {
        val existingConfig = repository.findByUserId(userId)
        if (existingConfig != null) {
            return existingConfig
        }

        val vlessUuid = UUID.randomUUID()
        val vlessLink = buildVlessLink(vlessUuid)

        val config = repository.create(userId, vlessUuid, vlessLink)
        emailService.sendVlessLink(userId, vlessLink)
        return config
    }

    suspend fun getConfigForUser(userId: UUID): VpnConfig? =
        repository.findByUserId(userId)

    private fun buildVlessLink(vlessId: UUID): String =
        "vless://$vlessId@$serverHost:$serverPort" +
                "?type=tcp@security=reality" +
                "&pbk=$publicKey" +
                "&fp=chrome" +
                "&sni=www.microsoft.com" +
                "&flow=xtls-rprx-vision" +
                "#YogurtVPN"
}