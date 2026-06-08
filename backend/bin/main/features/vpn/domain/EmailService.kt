package com.yogurtvpn.features.vpn.domain

import java.util.UUID

interface EmailService {
    suspend fun sendVlessLink(userId: UUID, vlessLink: String)
}