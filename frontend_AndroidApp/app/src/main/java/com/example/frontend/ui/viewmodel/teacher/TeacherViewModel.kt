package com.example.frontend.ui.viewmodel.teacher


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TeacherViewModel(
    private val teacherRepository: TeacherRepository
) : ViewModel() {

    private val _teachers = MutableStateFlow<List< TeacherEntity>>(emptyList())
    val teachers: StateFlow<List<TeacherEntity>> = _teachers

    fun loadTeachers() {
        viewModelScope.launch {
            _teachers.value = teacherRepository.getAll()
        }
    }

    fun addTeacher(name: String, email: String, password: String) {
        viewModelScope.launch {
            val teacher = TeacherEntity(
                name = name,
                email = email,
                password = password
            )
            teacherRepository.insert(teacher)
            loadTeachers()
        }
    }


    fun updateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            teacherRepository.update(teacher)
            loadTeachers()
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            teacherRepository.delete(teacher)
            loadTeachers()
        }
    }

    fun deleteAllTeachers() {
        viewModelScope.launch {
            teacherRepository.deleteAll()
            loadTeachers()
        }
    }
}



    /*

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

     */
