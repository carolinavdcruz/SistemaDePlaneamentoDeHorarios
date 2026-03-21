package com.example.frontend.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.model.Student
import com.example.frontend.data.remote.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {

    private val repo = StudentRepository()

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students

    fun loadStudents() {
        viewModelScope.launch {
            _students.value = repo.getStudents()
        }
    }

    fun createStudent(student: Student) {
        viewModelScope.launch {
            repo.createStudent(student)
            loadStudents()
        }
    }

    fun updateStudent(id: Long, student: Student) {
        viewModelScope.launch {
            repo.updateStudent(id, student)
            loadStudents()
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch {
            repo.deleteStudent(id)
            loadStudents()
        }
    }
}