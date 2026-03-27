package com.example.frontend.ui.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val role: String = "",

    // Teacher only
    val classesCount: Int = 0,
    val studentsCount: Int = 0,
    val rating: Float = 0f,

    // Student only
    val maxDailySessions: Int = 0,
    val subjectsCount: Int = 0,
    val studentClassesCount: Int = 0,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(
    private val studentDao: StudentDao,
    private val teacherDao: TeacherDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = sessionManager.getUserId()
            val role   = sessionManager.getUserRole()

            if (userId == -1) {
                _uiState.update { it.copy(isLoading = false, isLoggedOut = true) }
                return@launch
            }

            try {
                when (role) {
                    "Student" -> {
                        val student = studentDao.getById(userId)
                        if (student != null) {
                            _uiState.update {
                                it.copy(
                                    name             = student.name,
                                    email            = student.email,
                                    role             = "Student",
                                    maxDailySessions = student.maxDailySessions,
                                    isLoading        = false
                                )
                            }
                        }
                    }
                    "Teacher" -> {
                        val teacher = teacherDao.getById(userId)
                        if (teacher != null) {
                            _uiState.update {
                                it.copy(
                                    name      = teacher.name,
                                    email     = teacher.email,
                                    role      = "Teacher",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.localizedMessage)
                }
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun onErrorDismissed() { _uiState.update { it.copy(errorMessage = null) } }
    fun onLogoutNavigated() { _uiState.update { it.copy(isLoggedOut = false) } }
}