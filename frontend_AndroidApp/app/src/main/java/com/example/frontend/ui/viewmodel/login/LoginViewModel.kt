package com.example.frontend.ui.viewmodel.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _email = MutableStateFlow("m@example.com")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun setEmail(value: String) {
        _email.value = value
        clearError()
    }

    fun setPassword(value: String) {
        _password.value = value
        clearError()
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun validateLogin(): Boolean {
        val currentEmail = _email.value.trim()
        val currentPassword = _password.value

        _errorMessage.value = when {
            currentEmail.isBlank() -> "O email e obrigatorio."
            currentPassword.isBlank() -> "A password e obrigatoria."
            else -> null
        }

        return _errorMessage.value == null
    }

    private fun clearError() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }
}
