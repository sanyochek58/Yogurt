package com.yogurtvpn.features.access.data

import com.yogurtvpn.features.access.domain.AccessRequest
import com.yogurtvpn.features.access.domain.AccessRequestStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object AccessRequestTable: Table("access_requests") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id")
    val status = varchar("status", 50).default("PENDING")
    val requestedAt = datetime("requested_at").default(LocalDateTime.now())
    val reviewedAt = datetime( "reviewed_at").nullable()
    val reviewedBy = uuid("reviewed_by").nullable()

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toAccessRequest() = AccessRequest(
    id = this[AccessRequestTable.id],
    userId = this[AccessRequestTable.userId],
    status = AccessRequestStatus.valueOf(this[AccessRequestTable.status]),
    requestedAt = this[AccessRequestTable.requestedAt],
    reviewedAt = this[AccessRequestTable.reviewedAt],
    reviewedBy = this[AccessRequestTable.reviewedBy]
)