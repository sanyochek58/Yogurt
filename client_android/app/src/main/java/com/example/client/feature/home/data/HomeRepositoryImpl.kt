package com.yogurtvpn.client.feature.home.data

import com.yogurtvpn.client.core.network.YogurtApi
import com.yogurtvpn.client.core.storage.TokenStorage
import com.yogurtvpn.client.feature.home.domain.AccessStatus
import com.yogurtvpn.client.feature.home.domain.HomeRepository
import java.lang.Exception

class HomeRepositoryImpl(
    private val api: YogurtApi,
    private val storage: TokenStorage
): HomeRepository {


    override suspend fun getAccessStatus(token: String): AccessStatus {
        return try {
            val request = api.getMyAccessRequest(token)
            if (request.status == "APPROVED") {
                val config = try {
                    api.getVpnConfig(token)
                } catch (e: Exception) { null }
                AccessStatus(
                    hasRequest = true,
                    status = "APPROVED",
                    vlessLink = config?.vlessLink
                )
            } else {
                AccessStatus(
                    hasRequest = true,
                    status = request.status,
                    vlessLink = null
                )
            }
        } catch (e: Exception) {
            AccessStatus(hasRequest = false, status = null, vlessLink = null)
        }
    }

    override suspend fun requestAccess(token: String): Boolean {
        return try {
            api.requestAccess(token)
            true
        }catch (e: Exception){
            false
        }
    }

    override suspend fun getVpnConfig(token: String): String? {
        return try {
            val config = api.getVpnConfig(token)
            storage.saveVlessLink(config.vlessLink)
            config.vlessLink
        }catch (e: Exception){
            null
        }
    }
}