package com.yogurtvpn.features.access.domain

import com.yogurtvpn.features.vpn.domain.VpnConfigService
import java.util.UUID

class AccessRequestService(
    private val repository: AccessRequestRepository,
    private val vpnConfigService: VpnConfigService
) {

    suspend fun requestAccess(userId: UUID): AccessRequest {
        if(repository.hasActiveRequest(userId)) {
            throw IllegalArgumentException("You already have a pending or approved request")
        }
        return repository.create(userId)
    }

    suspend fun getMyRequest(userId: UUID): AccessRequest =
        repository.findByUserId(userId).firstOrNull() ?: throw IllegalArgumentException("No access request found")

    suspend fun getAllPending(): List<AccessRequest> =
        repository.findAllPending()

    suspend fun getAll(): List<AccessRequest> =
        repository.findAll()

    suspend fun approve(requestId: UUID, adminId: UUID): AccessRequest {
        val request = repository.findById(requestId) ?: throw IllegalArgumentException("No access request found")

        if(request.status != AccessRequestStatus.PENDING) {
            throw IllegalArgumentException("Request is already ${request.status.name.lowercase()}")
        }

        val approved = repository.approve(requestId, adminId)
        vpnConfigService.createConfigForUSer(approved.userId)
        return approved
    }

    suspend fun reject(requestId: UUID, adminId: UUID): AccessRequest {
        val request = repository.findById(requestId) ?: throw IllegalArgumentException("No access request found")

        if(request.status != AccessRequestStatus.PENDING) {
            throw IllegalArgumentException("Request is already ${request.status.name.lowercase()}")
        }

        return repository.reject(requestId, adminId)
    }
}