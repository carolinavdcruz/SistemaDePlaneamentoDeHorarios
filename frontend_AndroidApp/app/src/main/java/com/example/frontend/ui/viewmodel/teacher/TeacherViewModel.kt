package com.example.frontend.ui.viewmodel.teacher

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.dao.TimeSlotDao
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.TimeSlot
import com.example.frontend.data.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TeacherViewModel(private val repository: TeacherRepository) : ViewModel() {

    private val _teachers = MutableStateFlow<List< TeacherEntity>>(emptyList())
    val teachers: StateFlow<List<TeacherEntity>> = _teachers

    fun loadTeachers() {
        viewModelScope.launch {
            _teachers.value = repository.getAll()
        }
    }

    fun addTeacher(name: String, email: String) {
        viewModelScope.launch {
            val student = TeacherEntity(
                name = name,
                email = email,
            )
            repository.insert(student)
            loadTeachers()
        }
    }

    fun updateTeacher(student: TeacherEntity) {
        viewModelScope.launch {
            repository.update(student)
            loadTeachers()
        }
    }

    fun deleteTeacher(student: TeacherEntity) {
        viewModelScope.launch {
            repository.delete(student)
            loadTeachers()
        }
    }

    private val _selectedDay = MutableLiveData(1) // 1 = Segunda

    // SwitchMap garante que se o dia mudar, a lista de slots atualiza
    val dailySlots: LiveData<List<TimeSlot>> = _selectedDay.switchMap { day ->
        repository.getSlotsForDay(day).asLiveData()
    }

    fun setAvailability(teacherId: Int, startTime: String, endTime: String) {
        viewModelScope.launch {
            val currentDay = _selectedDay.value ?: 1
            repository.syncAvailability(teacherId, currentDay, startTime, endTime)
        }
    }

    fun changeDay(day: Int) {
        _selectedDay.value = day
    }
}