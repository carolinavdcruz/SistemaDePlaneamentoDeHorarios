package com.example.frontend.ui.viewmodel.schedule

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.model.Restrictions
import com.example.frontend.data.model.ScheduledSession
import com.example.frontend.data.model.scheduling.ScheduleGenerator
import com.example.frontend.data.model.scheduling.TimeSlotProcessor
import com.example.frontend.data.remote.api.GoogleCalendarManager
import com.example.frontend.data.remote.api.ScheduleApi
import com.example.frontend.data.remote.dto.SaveScheduleRequest
import com.example.frontend.data.remote.dto.SessionRequest
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.data.repository.RestrictionsRepository
import com.example.frontend.data.repository.StudentRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScheduleUiState {
    object Idle     : ScheduleUiState()
    object Loading  : ScheduleUiState()
    object Accepted : ScheduleUiState()

    data class Success(
        val sessions: List<ScheduledSession>,
        val studentName: Map<Int, String>
    ) : ScheduleUiState()

    data class Empty(val reason: String)  : ScheduleUiState()
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

    private var currentScheduleId: Int? = null
    private var lastSessions: List<ScheduledSession> = emptyList()
    private var lastStudentNames: Map<Int, String> = emptyMap()

    // Google Sign-In

    // Devolve o Intent do ecrã de seleção de conta Google.
    // Chama-o antes de acceptSchedule; lança com startActivityForResult(RC = 9001).
    fun buildGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR)
            )
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    // Criar

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

                // 5. Gerar horário
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

                if (sessions.isEmpty()) {
                    _uiState.value = ScheduleUiState.Empty("Sem sobreposições encontradas.")
                } else {
                    // Guarda localmente para usar no accept
                    lastSessions     = sessions
                    lastStudentNames = students.associate { it.id to it.name }
                    _uiState.value   = ScheduleUiState.Success(lastSessions, lastStudentNames)
                }

            } catch (e: Exception) {
                println(">>> ERRO: ${e.message}")
                _uiState.value = ScheduleUiState.Error(e.localizedMessage ?: "Erro inesperado.")
            }
        }
    }

    // Guardar no backend
    fun saveSchedule(teacherId: Int, sessions: List<ScheduledSession>) {
        viewModelScope.launch {
            try {
                val scheduleId = scheduleApi.saveSchedule(
                    SaveScheduleRequest(
                        teacherId = teacherId,
                        sessions  = sessions.map {
                            SessionRequest(
                                dayOfWeek  = it.dayOfWeek,
                                startTime  = it.startTime,
                                endTime    = it.endTime,
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

    // Aceitar

    // Chama este método APÓS o utilizador ter feito Google Sign-In com sucesso.
    fun acceptSchedule(context: Context) {
        val id = currentScheduleId ?: return
        viewModelScope.launch {
            try {
                // 1. Aceita no backend
                scheduleApi.acceptSchedule(id)

                // 2. Verifica se já está autenticado no Google
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account == null) {
                    // Não autenticado: sinaliza a UI para lançar o Sign-In
                    _uiState.value = ScheduleUiState.Error("GOOGLE_SIGN_IN_REQUIRED")
                    return@launch
                }

                // 3. Adiciona sessões ao Google Calendar
                val manager = GoogleCalendarManager(context)
                val result  = manager.addSessionsToCalendar(lastSessions, lastStudentNames)

                if (result.isSuccess) {
                    println(">>> ${result.getOrDefault(0)} eventos adicionados ao Google Calendar")
                    _uiState.value = ScheduleUiState.Accepted
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                    _uiState.value = ScheduleUiState.Error("Horário aceite, mas erro no Calendar: $msg")
                }

            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erro ao aceitar horário: ${e.message}")
            }
        }
    }

    //Rejeitar
    fun rejectSchedule() {
        val id = currentScheduleId ?: return
        viewModelScope.launch {
            try {
                scheduleApi.rejectSchedule(id)
                _uiState.value   = ScheduleUiState.Idle
                currentScheduleId = null
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erro ao rejeitar horário: ${e.message}")
            }
        }
    }
}