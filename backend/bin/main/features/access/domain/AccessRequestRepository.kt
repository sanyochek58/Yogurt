package com.yogurtvpn.features.access.domain

import java.util.UUID

interface AccessRequestRepository {
    suspend fun create(userId: UUID): AccessRequest
    suspend fun findById(id: UUID): AccessRequest?
    suspend fun findByUserId(userId: UUID): List<AccessRequest>
    suspend fun findAllPending(): List<AccessRequest>
    suspend fun findAll(): List<AccessRequest>
    suspend fun approve(id: UUID, adminId: UUID): AccessRequest
    suspend fun reject(id: UUID, adminId: UUID): AccessRequest
    suspend fun hasActiveRequest(userId: UUID): Boolean
}