package com.yogurtvpn.client.feature.home.domain

data class AccessStatus(
    val hasRequest: Boolean,
    val status: String?,
    val vlessLink: String?
)
