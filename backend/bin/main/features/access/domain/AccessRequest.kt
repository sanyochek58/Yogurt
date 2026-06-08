package com.yogurtvpn.features.access.domain

import java.time.LocalDateTime
import java.util.UUID

data class AccessRequest(
    val id: UUID,
    val userId: UUID,
    val userEmail: String,
    val status: AccessRequestStatus,
    val requestedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val reviewedBy: UUID?
)

enum class AccessRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
}
