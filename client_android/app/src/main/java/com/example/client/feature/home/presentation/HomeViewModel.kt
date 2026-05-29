package com.yogurtvpn.client.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogurtvpn.client.core.storage.TokenStorage
import com.yogurtvpn.client.feature.home.domain.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val accessStatus: String? = null,
    val hasRequest: Boolean = false,
    val vlessLink: String? = null,
    val userEmail: String? = null,
    val error: String? = null,
    val info: String? = null
)

class HomeViewModel(
    private val repository: HomeRepository,
    private val storage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeVlessLink()
    }

    private fun observeVlessLink() {
        viewModelScope.launch {
            storage.vlessLink.collect { link ->
                _uiState.value = _uiState.value.copy(vlessLink = link)
            }
        }
    }

    fun refresh() = loadData()

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = storage.token.firstOrNull() ?: return@launch
            val email = storage.userEmail.firstOrNull()
            val savedLink = storage.vlessLink.firstOrNull()

            val status = repository.getAccessStatus(token)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                userEmail = email,
                hasRequest = status.hasRequest,
                accessStatus = status.status,
                vlessLink = status.vlessLink ?: savedLink
            )
        }
    }

    fun requestAccess() {
        viewModelScope.launch {
            val token = storage.token.firstOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val success = repository.requestAccess(token)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasRequest = true,
                    accessStatus = "PENDING"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось отправить заявку"
                )
            }
        }
    }

    fun refreshVpnConfig() {
        viewModelScope.launch {
            val token = storage.token.firstOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val link = repository.getVpnConfig(token)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                vlessLink = link,
                info = if (link != null) "Конфигурация обновлена" else "Не удалось получить конфигурацию"
            )
        }
    }

    fun onVlessLinkPasted(link: String) {
        viewModelScope.launch {
            storage.saveVlessLink(link)
            _uiState.value = _uiState.value.copy(
                vlessLink = link,
                info = "Ссылка сохранена"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearInfo() {
        _uiState.value = _uiState.value.copy(info = null)
    }
}