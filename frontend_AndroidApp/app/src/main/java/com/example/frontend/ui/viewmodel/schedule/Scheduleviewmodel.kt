package com.example.frontend.ui.viewmodel.schedule

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.model.Restrictions
import com.example.frontend.data.model.ScheduledSession
import com.example.frontend.data.model.TimeSlot
import com.example.frontend.data.model.scheduling.ScheduleGenerator
import com.example.frontend.data.model.scheduling.TimeSlotProcessor
import com.example.frontend.data.remote.api.ScheduleApi
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.data.repository.RestrictionsRepository
import com.example.frontend.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScheduleUiState {
    object Idle : ScheduleUiState()
    object Loading : ScheduleUiState()
    data class Success(
        val sessions: List<ScheduledSession>,
        val studentName: Map<Int, String>
    ) : ScheduleUiState()
    data class Empty(val reason: String) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}

class ScheduleViewModel(
    private val scheduleApi: ScheduleApi,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSchedule(teacherId: Int) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading

            try {
                val remoteSessions = scheduleApi.generateSchedule(teacherId)
                val students = studentRepository.getByTeacherId(teacherId)
                val studentNames = students.associate { it.id to it.name }

                val sessions = remoteSessions.map {
                    ScheduledSession(
                        teacherId = teacherId,
                        studentIds = it.studentIds,
                        dayOfWeek = it.dayOfWeek,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                }

                _uiState.value = if (sessions.isEmpty()) {
                    ScheduleUiState.Empty("Sem sobreposições de disponibilidade encontradas.")
                } else {
                    ScheduleUiState.Success(
                        sessions = sessions,
                        studentName = studentNames
                    )
                }

            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error(
                    e.localizedMessage ?: "Erro inesperado ao gerar horário."
                )
            }
        }
    }
}
