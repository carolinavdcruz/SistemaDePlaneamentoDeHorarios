package com.example.frontend.ui.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChooseTeacherViewModel(
    private val teacherRepository: TeacherRepository,
    private val studentRepository: StudentRepository,
    private val sessionManager: SessionManager
) : ViewModel(){

    private val _teachers = MutableStateFlow<List<TeacherEntity>>(emptyList())
    val teachers: StateFlow<List<TeacherEntity>> = _teachers

    private val _selectedTeacherId = MutableStateFlow<Int?>(null)
    val selectedTeacherId: StateFlow<Int?> = _selectedTeacherId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _assignSuccess = MutableStateFlow(false)
    val assignSuccess: StateFlow<Boolean> = _assignSuccess

    fun loadTeachers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _teachers.value = teacherRepository.getAll()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar professores."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTeacherSelected(teacherId: Int) {
        _selectedTeacherId.value = teacherId
        clearErrorMessage()
    }

    fun assignTeacherToStudent() {

        val studentId = sessionManager.getUserId()
        val teacherId = _selectedTeacherId.value
        val role = sessionManager.getUserRole()

        if (studentId == -1){
            _errorMessage.value = "Sessao nao encontrada. Por favor volta a fazer login."
            return
        }

        if (role != OwnerType.STUDENT){
            _errorMessage.value = "Apenas alunos podem escolher professores."
            return
        }

        if (teacherId == null) {
            _errorMessage.value = "Por favor selecione um professor."
            return
        }

        viewModelScope.launch {

            _isLoading.value = true

            try {
                studentRepository.assignTeacherToStudent(studentId, teacherId)
                _assignSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao associar professor ao aluno."
            } finally {
                _isLoading.value = false
            }
        }

    }

    fun onAssignSuccessNavigated() {
        _assignSuccess.value = false
    }

    fun onErrorDismissed() {
        _errorMessage.value = null
    }

    private fun clearErrorMessage(){
        if (_errorMessage.value != null){
            _errorMessage.value = null
        }
    }


}
