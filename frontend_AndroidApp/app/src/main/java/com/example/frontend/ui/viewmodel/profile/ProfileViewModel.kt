package com.example.frontend.ui.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = sessionManager.getUserId()
            val role = sessionManager.getUserRole()

            if (userId == -1) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sessao nao encontrada. Por favor volta a fazer login."
                    )
                }
                return@launch
            }

            try {
                when (role) {

                    OwnerType.STUDENT -> {

                        val student = studentRepository.getById(userId)

                        if (student != null) {

                            val teacherName = student.teacherId?.let { teacherId ->
                                teacherRepository.getById(teacherId)?.name
                                    ?: run {
                                        teacherRepository.getAll()
                                        teacherRepository.getById(teacherId)?.name
                                    }
                            }

                            _uiState.update {
                                it.copy(
                                    name = student.name,
                                    email = student.email,
                                    role = OwnerType.STUDENT,
                                    teacherName = teacherName,
                                    maxDailySessions = student.maxDailySessions,
                                    isLoading = false
                                )
                            }

                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Aluno nao encontrado."
                                )
                            }
                        }
                    }

                    OwnerType.TEACHER -> {

                        val teacher = teacherRepository.getById(userId)

                        if (teacher != null) {
                            _uiState.update {
                                it.copy(
                                    name = teacher.name,
                                    email = teacher.email,
                                    role = OwnerType.TEACHER,
                                    teacherName = null,
                                    isLoading = false
                                )
                            }

                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Professor nao encontrado."
                                )
                            }
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Tipo de utilizador invalido."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Erro ao carregar perfil."
                    )
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

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onLogoutNavigated() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }
}
