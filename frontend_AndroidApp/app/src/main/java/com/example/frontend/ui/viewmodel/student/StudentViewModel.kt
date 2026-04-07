package com.example.frontend.ui.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<StudentEntity>>(emptyList())
    val students: StateFlow<List<StudentEntity>> = _students

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun loadStudents() {
        viewModelScope.launch {
            _students.value = repository.getAll()
        }
    }

    fun setName(value: String){
        _name.value = value
        clearErrorMessage()
    }

    fun setEmail(value: String){
        _email.value = value
        clearErrorMessage()
    }

    fun addStudent() {

        val currentName = _name.value
        val currentEmail = _email.value

        if (currentName.isBlank()) {
            _errorMessage.value = "O nome do aluno é obrigatorio."
            return
        }

        if (currentEmail.isBlank()) {
            _errorMessage.value = "O email do aluno é obrigatorio."
            return
        }

        viewModelScope.launch {
            val student = StudentEntity(
                name = currentName,
                email = currentEmail,
                maxDailySessions = 1
            )
            repository.insert(student)
            clearForm()
            loadStudents()
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.update(student)
            loadStudents()
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.delete(student)
            loadStudents()
        }
    }

    private fun clearForm() {
        _name.value = ""
        _email.value = ""
        clearErrorMessage()
    }

    private fun clearErrorMessage() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }


}