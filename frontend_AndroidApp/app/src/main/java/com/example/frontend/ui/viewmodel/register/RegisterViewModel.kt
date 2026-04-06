package com.example.frontend.ui.viewmodel.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.RegisterValidator
import com.example.frontend.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel (
    private val studentDao: StudentDao,
    private val teacherDao: TeacherDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

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

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setName(value: String) {
        _name.value = value
        clearError()
    }

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
            name = _name.value,
            email = _email.value,
            password = _password.value
        )
        return _errorMessage.value == null
    }

    fun register() {
        if (!validateRegister()) return
        viewModelScope.launch {
            _isLoading.value = true
            val name  = _name.value.trim()
            val email = _email.value.trim()
            val role  = _selectedRole.value

            try {
                when (role) {
                    "Student" -> {
                        val entity = StudentEntity(name = name, email = email, maxDailySessions = 0)
                        studentDao.insert(entity)
                        val saved = studentDao.getByEmail(email)!!
                        sessionManager.saveSession(userId = saved.id, role = "Student")
                    }
                    "Teacher" -> {
                        val entity = TeacherEntity(name = name, email = email)
                        teacherDao.insert(entity)
                        val saved = teacherDao.getByEmail(email)!!
                        sessionManager.saveSession(userId = saved.id, role = "Teacher")
                    }
                }
                _registerSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao criar conta: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun clearError() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }
}