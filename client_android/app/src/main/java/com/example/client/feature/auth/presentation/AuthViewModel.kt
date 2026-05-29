package com.yogurtvpn.client.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.feature.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun login() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                repository.login(_email.value.trim(), _password.value)
                _uiState.value = AuthUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = parseError(e))
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                repository.register(_email.value.trim(), _password.value)
                _uiState.value = AuthUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = parseError(e))
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
            _email.value = ""
            _password.value = ""
        }
    }

    private fun parseError(e: Exception): String = when {
        e.message?.contains("400") == true -> "Неверный email или пароль"
        e.message?.contains("409") == true -> "Пользователь уже существует"
        e.message?.contains("Unable to resolve") == true -> "Нет подключения к серверу"
        else -> "Что-то пошло не так"
    }
}