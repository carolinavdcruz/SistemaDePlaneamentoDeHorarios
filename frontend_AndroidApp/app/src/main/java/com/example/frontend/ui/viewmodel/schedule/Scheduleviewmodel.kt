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
import com.example.frontend.data.remote.dto.SaveScheduleRequest
import com.example.frontend.data.remote.dto.SessionRequest
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.data.repository.RestrictionsRepository
import com.example.frontend.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScheduleUiState {
    object Idle : ScheduleUiState()
    object Loading : ScheduleUiState()
    object Accepted : ScheduleUiState()

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
    private val studentRepository: StudentRepository,
    private val scheduleApi: ScheduleApi = ScheduleApi()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    private var currentScheduleId: Int? = null  // guarda o id após guardar

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSchedule(teacherId: Int) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                // 1. Restrições
                val restrictionsEntity = restrictionsRepository.getByTeacherId(teacherId)
                println(">>> restrictionsEntity: $restrictionsEntity")
                if (restrictionsEntity == null) {
                    _uiState.value = ScheduleUiState.Empty("Sem restrições definidas.")
                    return@launch
                }

                // 2. Disponibilidades do professor
                val teacherAvailabilities = availabilityRepository.getByOwner(teacherId, OwnerType.TEACHER)
                println(">>> teacherAvailabilities: ${teacherAvailabilities.size}")
                if (teacherAvailabilities.isEmpty()) {
                    _uiState.value = ScheduleUiState.Empty("O professor não tem disponibilidades.")
                    return@launch
                }

                // 3. Alunos
                val students = studentRepository.getByTeacherId(teacherId)
                println(">>> students: ${students.size}")
                if (students.isEmpty()) {
                    _uiState.value = ScheduleUiState.Empty("Sem alunos associados.")
                    return@launch
                }

                // 4. Disponibilidades dos alunos
                val studentAvailabilities = students.associate { student ->
                    val avails = availabilityRepository.getByOwner(student.id, OwnerType.STUDENT)
                    println(">>> aluno ${student.id} tem ${avails.size} disponibilidades")
                    val slots = TimeSlotProcessor.processAll(
                        avails.map { Triple(it.dayOfWeek, it.startTime, it.endTime) },
                        slotDurationMinutes = restrictionsEntity.sessionDurationMinutes.toLong()
                    )
                    println(">>> aluno ${student.id} tem ${slots.size} slots processados")
                    student.id to slots
                }

                // 5. Gerar
                val sessions = ScheduleGenerator().create(
                    teacherId             = teacherId,
                    teacherSlots          = TimeSlotProcessor.processAll(
                        teacherAvailabilities.map { Triple(it.dayOfWeek, it.startTime, it.endTime) },
                        slotDurationMinutes = restrictionsEntity.sessionDurationMinutes.toLong()
                    ),
                    students              = students,
                    studentAvailabilities = studentAvailabilities,
                    restrictions          = Restrictions(
                        teacherId                   = teacherId,
                        maxDailyHours               = restrictionsEntity.maxDailyHours,
                        sessionDurationMinutes      = restrictionsEntity.sessionDurationMinutes,
                        maxParticipantsPerSession   = restrictionsEntity.maxParticipantsPerSession,
                        maxSessionsPerStudentPerDay = restrictionsEntity.maxSessionsPerStudentPerDay
                    )
                )
                println(">>> sessions geradas: ${sessions.size}")

                _uiState.value = if (sessions.isEmpty()) {
                    ScheduleUiState.Empty("Sem sobreposições encontradas.")
                } else {
                    ScheduleUiState.Success(sessions, students.associate { it.id to it.name })
                }

            } catch (e: Exception) {
                println(">>> ERRO: ${e.message}")
                _uiState.value = ScheduleUiState.Error(e.localizedMessage ?: "Erro inesperado.")
            }
        }
    }

    // Após gerar, guarda no backend
    fun saveSchedule(teacherId: Int, sessions: List<ScheduledSession>) {
        viewModelScope.launch {
            try {
                val scheduleId = scheduleApi.saveSchedule(
                    SaveScheduleRequest(
                        teacherId = teacherId,
                        sessions = sessions.map {
                            SessionRequest(
                                dayOfWeek = it.dayOfWeek,
                                startTime = it.startTime,
                                endTime = it.endTime,
                                studentIds = it.studentIds
                            )
                        }
                    )
                )
                currentScheduleId = scheduleId
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erro ao guardar horário: ${e.message}")
            }
        }
    }

    fun acceptSchedule() {
        val id = currentScheduleId ?: return
        viewModelScope.launch {
            try {
                scheduleApi.acceptSchedule(id)
                _uiState.value = ScheduleUiState.Accepted
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erro ao aceitar horário: ${e.message}")
            }
        }
    }

    fun rejectSchedule() {
        val id = currentScheduleId ?: return
        viewModelScope.launch {
            try {
                scheduleApi.rejectSchedule(id)
                _uiState.value = ScheduleUiState.Idle
                currentScheduleId = null
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erro ao rejeitar horário: ${e.message}")
            }
        }
    }
}
