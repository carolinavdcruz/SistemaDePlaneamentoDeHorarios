package com.example.frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {

    private val repository = AppModule.studentRepository

    private val _students = MutableStateFlow<List<StudentEntity>>(emptyList())
    val students: StateFlow<List<StudentEntity>> = _students

    fun loadStudents() {
        viewModelScope.launch {
            _students.value = repository.getAll()
        }
    }

    fun addStudent(name: String, email: String) {
        viewModelScope.launch {
            val student = StudentEntity(
                name = name,
                email = email,
                maxDailySessions = 1
            )
            repository.insert(student)
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
}