package com.example.frontend.ui.viewmodel.register

import androidx.lifecycle.ViewModel
import com.example.frontend.data.model.RegisterValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _selectedRole = MutableStateFlow("Student")
    val selectedRole: StateFlow<String> = _selectedRole

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

    fun setSelectedRole(role: String) {
        _selectedRole.value = role
        clearError()
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun validateRegister(): Boolean {
        _errorMessage.value = RegisterValidator.validate(
            email = _email.value,
            password = _password.value
        )

        return _errorMessage.value == null
    }

    private fun clearError() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }
}
