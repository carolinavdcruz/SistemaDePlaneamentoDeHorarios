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
    private val availabilityRepository: AvailabilityRepository,
    private val restrictionsRepository: RestrictionsRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSchedule(teacherId: Int) {

        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading

            try {
                // 1. Buscar restrições do professor
                val restrictionsEntity = restrictionsRepository.getByTeacherId(teacherId)
                if (restrictionsEntity == null) {
                    _uiState.value = ScheduleUiState.Empty(
                        "Sem restrições definidas. Configura as restrições primeiro."
                    )
                    return@launch
                }
                val restrictions = Restrictions(
                    teacherId                 = teacherId,
                    maxDailyHours             = restrictionsEntity.maxDailyHours,
                    sessionDurationMinutes    = restrictionsEntity.sessionDurationMinutes,
                    maxParticipantsPerSession = restrictionsEntity.maxParticipantsPerSession,
                    maxSessionsPerStudentPerDay = restrictionsEntity.maxSessionsPerStudentPerDay
                )

                // 2. Buscar disponibilidade do professor → converter em TimeSlots de 1h
                val teacherAvailabilities = availabilityRepository.getByOwner(teacherId, OwnerType.TEACHER)
                if (teacherAvailabilities.isEmpty()) {
                    _uiState.value = ScheduleUiState.Empty(
                        "O professor não tem disponibilidades definidas."
                    )
                    return@launch
                }
                val teacherSlots: List<TimeSlot> = TimeSlotProcessor.processAll(
                    teacherAvailabilities.map { Triple(it.dayOfWeek, it.startTime, it.endTime) },
                    slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                )

                // 3. Buscar alunos associados ao professor
                val students: List<StudentEntity> = studentRepository.getByTeacherId(teacherId)
                val studentNames = students.associate { it.id to it.name }
                if (students.isEmpty()) {
                     ScheduleUiState.Empty(
                        "Sem alunos associados. Adiciona alunos primeiro."
                    )
                    return@launch
                }

                // 4. Buscar disponibilidades de cada aluno → Map<studentId, List<TimeSlot>>
                val studentAvailabilities: Map<Int, List<TimeSlot>> = students.associate { student ->
                    val avails = availabilityRepository.getByOwner(student.id, OwnerType.STUDENT)
                    val slots = TimeSlotProcessor.processAll(
                        avails.map { Triple(it.dayOfWeek, it.startTime, it.endTime) },
                        slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                    )
                    student.id to slots
                }

                // 5. Gerar horário com o ScheduleGenerator
                val sessions = ScheduleGenerator().generate(
                    teacherId            = teacherId,
                    teacherSlots         = teacherSlots,
                    students             = students,
                    studentAvailabilities = studentAvailabilities,
                    restrictions         = restrictions
                )

                _uiState.value = if (sessions.isEmpty()) {
                    ScheduleUiState.Empty("Sem sobreposições de disponibilidade encontradas.")
                } else {
                    ScheduleUiState.Success(sessions,studentNames)
                }

            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error(
                    e.localizedMessage ?: "Erro inesperado ao gerar horário."
                )
            }
        }
    }
}