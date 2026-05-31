package com.yogurtvpn.features.access.data

import com.yogurtvpn.features.access.domain.AccessRequest
import com.yogurtvpn.features.access.domain.AccessRequestRepository
import com.yogurtvpn.features.access.domain.AccessRequestStatus
import com.yogurtvpn.features.auth.data.UserTable
import com.yogurtvpn.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.UUID

class AccessRequestRepositoryImpl: AccessRequestRepository {

    private fun baseQuery() = AccessRequestTable
        .join(UserTable, JoinType.INNER, onColumn = AccessRequestTable.userId, otherColumn = UserTable.id)
        .selectAll()

    override suspend fun create(userId: UUID): AccessRequest = dbQuery {
        val insertedId = AccessRequestTable
            .insertReturning { it[AccessRequestTable.userId] = userId }
            .single()[AccessRequestTable.id]

        baseQuery()
            .where { AccessRequestTable.id eq insertedId }
            .single()
            .toAccessRequest()
    }

    override suspend fun findById(id: UUID): AccessRequest? = dbQuery {
        baseQuery()
            .where { AccessRequestTable.id eq id }
            .firstOrNull()
            ?.toAccessRequest()
    }

    override suspend fun findByUserId(userId: UUID): List<AccessRequest> = dbQuery {
        baseQuery()
            .where { AccessRequestTable.userId eq userId }
            .map { it.toAccessRequest() }
    }

    override suspend fun findAllPending(): List<AccessRequest> = dbQuery {
        baseQuery()
            .where(AccessRequestTable.status eq AccessRequestStatus.PENDING.name)
            .map { it.toAccessRequest() }
    }

    override suspend fun findAll(): List<AccessRequest> = dbQuery {
        baseQuery()
            .orderBy(AccessRequestTable.requestedAt to org.jetbrains.exposed.sql.SortOrder.DESC)
            .map { it.toAccessRequest() }
    }

    override suspend fun approve(id: UUID, adminId: UUID): AccessRequest = dbQuery {
        AccessRequestTable.update(where = { AccessRequestTable.id eq id }) {
            it[status] = AccessRequestStatus.APPROVED.name
            it[reviewedAt] = LocalDateTime.now()
            it[reviewedBy] = adminId
        }

        baseQuery()
            .where { AccessRequestTable.id eq id }
            .single()
            .toAccessRequest()
    }

    override suspend fun reject(id: UUID, adminId: UUID): AccessRequest = dbQuery {
        AccessRequestTable.update(where = { AccessRequestTable.id eq id }) {
            it[status] = AccessRequestStatus.REJECTED.name
            it[reviewedAt] = LocalDateTime.now()
            it[reviewedBy] = adminId
        }

        baseQuery()
            .where { AccessRequestTable.id eq id }
            .single()
            .toAccessRequest()
    }

    override suspend fun hasActiveRequest(userId: UUID): Boolean = dbQuery {
        AccessRequestTable
            .selectAll()
            .where { AccessRequestTable.userId eq userId }
            .any {
                val status = AccessRequestStatus.valueOf(it[AccessRequestTable.status])
                status == AccessRequestStatus.PENDING || status == AccessRequestStatus.APPROVED
            }
    }
}
