package com.yogurtvpn.features.vpn.data

import com.yogurtvpn.features.vpn.domain.EmailService
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.math.log

class EmailServiceImpl: EmailService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun sendVlessLink(userId: UUID, vlessLink: String) {
        logger.info("[EMAIL STUB] Sending vless link to userId: $userId")
        logger.info("[EMAIL STUB] Sending vless link to vless link: $vlessLink")

    }
}