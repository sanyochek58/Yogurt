package com.yogurtvpn.features.auth.data


import com.yogurtvpn.features.auth.domain.User
import com.yogurtvpn.features.auth.domain.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Table.Dual.uuid
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object UserTable: Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255)
    val password = varchar("password", 255)
    val role = varchar("role", 50).default("USER")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toUser() = User(
    id = this[UserTable.id],
    email = this[UserTable.email],
    passwordHash = this[UserTable.password],
    role = UserRole.fromString(this[UserTable.role]),
    createdAt = this[UserTable.createdAt],
    updatedAt = this[UserTable.updatedAt]
)