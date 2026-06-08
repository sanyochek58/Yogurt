package com.yogurtvpn.features.auth.data

import com.yogurtvpn.features.auth.domain.AuthRepository
import com.yogurtvpn.features.auth.domain.User
import com.yogurtvpn.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class AuthRepositoryImpl: AuthRepository {
    override suspend fun create(email: String, passwordHash: String): User = dbQuery {
        UserTable.insertReturning {
            it[UserTable.email] = email
            it[UserTable.password] = passwordHash
        }
            .single()
            .toUser()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UserTable.selectAll()
            .where { UserTable.email eq email }
            .firstOrNull()
            ?.toUser()
    }

    override suspend fun findById(id: UUID): User? = dbQuery{
        UserTable.selectAll()
            .where { UserTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun existsByEmail(email: String): Boolean = dbQuery{
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .count() > 0
    }
}