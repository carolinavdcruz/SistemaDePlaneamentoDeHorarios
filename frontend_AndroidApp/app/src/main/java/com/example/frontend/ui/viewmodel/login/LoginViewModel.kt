package com.example.frontend.ui.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val studentDao: StudentDao,
    private val teacherDao: TeacherDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _email = MutableStateFlow("m@example.com")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // true quando o login for bem sucedido — a UI navega ao detetar isto
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setEmail(value: String) { _email.value = value; clearError() }

    fun setPassword(value: String) { _password.value = value; clearError() }

    fun togglePasswordVisibility() { _isPasswordVisible.value = !_isPasswordVisible.value }

    fun onLoginNavigation() { _loginSuccess.value = false }

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

    fun login() {
        if (!validateLogin()) return
        viewModelScope.launch {
            _isLoading.value = true
            val email = _email.value.trim()

            // Procura primeiro em Student, depois em Teacher
            val student = studentDao.getByEmail(email)
            if (student != null) {
                sessionManager.saveSession(userId = student.id, role = "Student")
                _loginSuccess.value = true
                _isLoading.value    = false
                return@launch
            }
            val teacher = teacherDao.getByEmail(email)
            if (teacher != null) {
                sessionManager.saveSession(userId = teacher.id, role = "Teacher")
                _loginSuccess.value = true
                _isLoading.value    = false
                return@launch
            }
            // Nenhum utilizador encontrado
            _errorMessage.value = "Email ou password incorretos."
            _isLoading.value    = false
        }
    }

    private fun clearError() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }
}
