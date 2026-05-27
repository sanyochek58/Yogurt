package com.yogurtvpn.features.vpn.data

import com.yogurtvpn.features.auth.data.UserTable
import com.yogurtvpn.features.vpn.domain.VpnConfig
import com.yogurtvpn.features.vpn.domain.VpnConfigRepository
import db.migration.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class VpnConfigRepositoryImpl: VpnConfigRepository {

    override suspend fun findByUserId(userId: UUID): VpnConfig? = dbQuery {
        VpnConfigsTable
            .selectAll()
            .where{ VpnConfigsTable.userId eq userId }
            .firstOrNull()
            ?.toVpnConfig()
    }

    override suspend fun create(userId: UUID, vlessId: UUID, vlessLink: String): VpnConfig = dbQuery {
        VpnConfigsTable
            .insertReturning {
                it[VpnConfigsTable.userId] = userId
                it[VpnConfigsTable.vlessUUID] = vlessUUID
                it[VpnConfigsTable.vlessLink] = vlessLink
            }
            .single()
            .toVpnConfig()
    }
}