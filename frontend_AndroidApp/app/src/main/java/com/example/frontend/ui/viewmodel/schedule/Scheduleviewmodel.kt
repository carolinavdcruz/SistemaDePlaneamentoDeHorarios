package com.example.frontend.ui.viewmodel.schedule

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.model.ScheduledSession
import com.example.frontend.data.remote.api.GoogleCalendarManager
import com.example.frontend.data.remote.api.ScheduleApi
import com.example.frontend.data.repository.StudentRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
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
    private val scheduleApi: ScheduleApi ,
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    //private var currentScheduleId: Int? = null
    private var lastSessions: List<ScheduledSession> = emptyList()
    private var lastStudentNames: Map<Int, String> = emptyMap()


    // Google Sign-In
    fun buildGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR)
            )
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun hasCalendarPermission(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(
            account,
            Scope(CalendarScopes.CALENDAR)
        )
    }

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

                if (sessions.isEmpty()) {
                    _uiState.value = ScheduleUiState.Empty("Sem sobreposições de disponibilidade encontradas.")
                } else {
                    lastSessions = sessions
                    lastStudentNames = studentNames
                    _uiState.value = ScheduleUiState.Success(
                        sessions = sessions,
                        studentName = studentNames
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error(
                    e.localizedMessage ?: "Erro inesperado ao criar horário."
                )
            }
        }
    }

    fun acceptSchedule(context: Context) {
        viewModelScope.launch {
            try {
                // Verifica se já está autenticado no Google
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account == null) {
                    // Não autenticado! -> sinaliza a UI para lançar o Sign-In
                    _uiState.value = ScheduleUiState.Error("GOOGLE_SIGN_IN_REQUIRED")
                    return@launch
                }

                // Adiciona sessões ao Google Calendar
                val manager = GoogleCalendarManager(context)
                val result  = manager.addSessionsToCalendar(lastSessions, lastStudentNames)

                if (result.isSuccess) {
                    _uiState.value = ScheduleUiState.Accepted
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                    _uiState.value = ScheduleUiState.Error("Erro ao adicionar ao Google Calendar: $msg")
                }

            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error(
                    e.localizedMessage ?: "Erro ao aceitar horário."
                )
            }
        }
    }

    fun setUiError(message: String) {
        _uiState.value = ScheduleUiState.Error(message)
    }


}