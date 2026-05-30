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

    private val _currentTeacherId = MutableStateFlow<Int?>(null)
    val currentTeacherId: StateFlow<Int?> = _currentTeacherId

    private val _currentTeacherName = MutableStateFlow<String?>(null)
    val currentTeacherName: StateFlow<String?> = _currentTeacherName

    private val _isChangingTeacher = MutableStateFlow(false)
    val isChangingTeacher: StateFlow<Boolean> = _isChangingTeacher

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
                val teacherList = teacherRepository.getAll()
                _teachers.value = teacherList

                val studentId = sessionManager.getUserId()
                val role = sessionManager.getUserRole()

                if (studentId != -1 && role == OwnerType.STUDENT) {
                    val student = studentRepository.getById(studentId)
                    val assignedTeacherId = student?.teacherId

                    _currentTeacherId.value = assignedTeacherId
                    _selectedTeacherId.value = assignedTeacherId

                    _currentTeacherName.value = assignedTeacherId?.let { teacherId ->
                        teacherList.firstOrNull { it.id == teacherId }?.name
                            ?: teacherRepository.getById(teacherId)?.name
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar professores."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTeacherSelected(teacherId: Int) {
        if (_currentTeacherId.value != null && !_isChangingTeacher.value) return
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
                _currentTeacherId.value = teacherId
                _currentTeacherName.value = _teachers.value.firstOrNull { it.id == teacherId }?.name
                _isChangingTeacher.value = false
                _assignSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao associar professor ao aluno."
            } finally {
                _isLoading.value = false
            }

        }

    }

    fun enableChangeMode() {
        _isChangingTeacher.value = true
        clearErrorMessage()
    }

    fun cancelChangeMode() {
        _isChangingTeacher.value = false
        _selectedTeacherId.value = _currentTeacherId.value
        clearErrorMessage()
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
