package com.yogurtvpn.client.feature.vpn.presentation

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogurtvpn.client.core.storage.TokenStorage
import com.yogurtvpn.client.feature.vpn.domain.VpnRepository
import com.yogurtvpn.client.vpnservice.YogurtVpnService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VpnUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val needsPermission: Boolean = false,
    val vlessLink: String? = null,
    val error: String? = null,
    val pendingPrepareIntent: Intent? = null
)

class VpnViewModel(
    private val vpnRepository: VpnRepository,
    private val storage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    init {
        loadSavedLink()
        syncRunningState()
    }

    private fun loadSavedLink() {
        viewModelScope.launch {
            storage.vlessLink.collect { link ->
                _uiState.value = _uiState.value.copy(vlessLink = link)
            }
        }
    }

    private fun syncRunningState() {
        _uiState.value = _uiState.value.copy(
            isConnected = YogurtVpnService.isRunning
        )
    }

    fun saveLink(link: String) {
        viewModelScope.launch {
            if (vpnRepository.isValidVlessLink(link)) {
                vpnRepository.saveVlessLink(link)
                _uiState.value = _uiState.value.copy(vlessLink = link)
            }
        }
    }

    fun connect(context: Context) {
        viewModelScope.launch {
            val link = _uiState.value.vlessLink
            if (link.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Сначала вставьте VLESS-ссылку"
                )
                return@launch
            }

            // Запрашиваем разрешение VPN если нужно
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                _uiState.value = _uiState.value.copy(
                    needsPermission = true,
                    pendingPrepareIntent = prepareIntent
                )
                return@launch
            }

            startVpnFlow(context, link)
        }
    }

    fun onPermissionResult(granted: Boolean, context: Context) {
        _uiState.value = _uiState.value.copy(
            needsPermission = false,
            pendingPrepareIntent = null
        )
        if (granted) {
            val link = _uiState.value.vlessLink ?: return
            viewModelScope.launch {
                startVpnFlow(context, link)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                error = "Разрешение VPN не получено"
            )
        }
    }

    private suspend fun startVpnFlow(context: Context, link: String) {
        _uiState.value = _uiState.value.copy(
            isConnecting = true,
            error = null
        )

        val intent = Intent(context, YogurtVpnService::class.java).apply {
            action = YogurtVpnService.ACTION_CONNECT
            putExtra(YogurtVpnService.EXTRA_VLESS_LINK, link)
        }
        context.startService(intent)

        delay(1500)

        _uiState.value = _uiState.value.copy(
            isConnecting = false,
            isConnected = true
        )
    }

    fun disconnect(context: Context) {
        viewModelScope.launch {
            val intent = Intent(context, YogurtVpnService::class.java).apply {
                action = YogurtVpnService.ACTION_DISCONNECT
            }
            context.startService(intent)
            delay(500)
            _uiState.value = _uiState.value.copy(
                isConnected = false,
                isConnecting = false
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}