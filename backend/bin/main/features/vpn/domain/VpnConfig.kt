package com.yogurtvpn.features.vpn.domain

import java.time.LocalDateTime
import java.util.UUID

data class VpnConfig(
    val id: UUID,
    val userId: UUID,
    val vlessUUID: UUID,
    val vlessLink: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
)
