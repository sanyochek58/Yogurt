package com.yogurtvpn.features.vpn.data

import com.fasterxml.jackson.databind.type.LogicalType
import com.yogurtvpn.features.vpn.domain.VpnConfig
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object VpnConfigsTable : Table("vpn_configs") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id")
    val vlessUUID = uuid("vless_id").autoGenerate()
    val vlessLink = text("vless_link")
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}


fun ResultRow.toVpnConfig() = VpnConfig(
    id = this[VpnConfigsTable.id],
    userId = this[VpnConfigsTable.userId],
    vlessUUID = this[VpnConfigsTable.vlessUUID],
    vlessLink = this[VpnConfigsTable.vlessLink],
    isActive = this[VpnConfigsTable.isActive],
    createdAt = this[VpnConfigsTable.createdAt]
)
