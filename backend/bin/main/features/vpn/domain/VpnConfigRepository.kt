package com.yogurtvpn.features.vpn.domain

import com.yogurtvpn.features.access.domain.AccessRequest
import java.util.UUID

interface VpnConfigRepository {
    suspend fun create(userId: UUID, vlessId: UUID, vlessLink: String): VpnConfig
    suspend fun findByUserId(userId: UUID): VpnConfig?
}