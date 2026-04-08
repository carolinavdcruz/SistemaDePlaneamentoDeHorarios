package com.example.frontend.ui.viewmodel.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.RestrictionsEntity
import com.example.frontend.data.repository.RestrictionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RestrictionsViewModel(
    private val restrictionsRepository: RestrictionsRepository
): ViewModel(){

    private val _maxDailyHours = MutableStateFlow("")
    val maxDailyHours: StateFlow<String> = _maxDailyHours

    private val _sessionDurationMinutes = MutableStateFlow("")
    val sessionDurationMinutes: StateFlow<String> = _sessionDurationMinutes

    private val _maxParticipantsPerSession = MutableStateFlow("")
    val maxParticipantsPerSession: StateFlow<String> = _maxParticipantsPerSession

    private val _maxSessionsPerStudentPerDay = MutableStateFlow("")
    val maxSessionsPerStudentPerDay: StateFlow<String> = _maxSessionsPerStudentPerDay

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadRestrictions(teacherId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val restrictions = restrictionsRepository.getByTeacherId(teacherId)

                if (restrictions != null) {
                    _maxDailyHours.value = restrictions.maxDailyHours.toString()
                    _sessionDurationMinutes.value = restrictions.sessionDurationMinutes.toString()
                    _maxParticipantsPerSession.value = restrictions.maxParticipantsPerSession.toString()
                    _maxSessionsPerStudentPerDay.value = restrictions.maxSessionsPerStudentPerDay.toString()
                } else {
                    setDefaultValues()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar restricoes."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setMaxDailyHours(value: String) {
        _maxDailyHours.value = value
        _isSaved.value = false
        clearError()
    }

    fun setSessionDurationMinutes(value: String) {
        _sessionDurationMinutes.value = value
        _isSaved.value = false
        clearError()
    }

    fun setMaxParticipantsPerSession(value: String) {
        _maxParticipantsPerSession.value = value
        _isSaved.value = false
        clearError()
    }

    fun setMaxSessionsPerStudentPerDay(value: String) {
        _maxSessionsPerStudentPerDay.value = value
        _isSaved.value = false
        clearError()
    }

    fun saveRestrictions(teacherId: Int) {
        val maxDailyHoursValue = _maxDailyHours.value.toIntOrNull()
        val sessionDurationValue = _sessionDurationMinutes.value.toIntOrNull()
        val maxParticipantsValue = _maxParticipantsPerSession.value.toIntOrNull()
        val maxSessionsPerStudentValue = _maxSessionsPerStudentPerDay.value.toIntOrNull()

        _isSaved.value = false

        if (maxDailyHoursValue == null || maxDailyHoursValue <= 0) {
            _errorMessage.value = "Max daily hours invalid."
            return
        }

        if (sessionDurationValue == null || sessionDurationValue <= 0) {
            _errorMessage.value = "Session duration invalid."
            return
        }

        if (maxParticipantsValue == null || maxParticipantsValue <= 0) {
            _errorMessage.value = "Max participants invalid."
            return
        }

        if (maxSessionsPerStudentValue == null || maxSessionsPerStudentValue <= 0) {
            _errorMessage.value = "Max sessions per student in a day invalid."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val restrictionOfTeacherID = restrictionsRepository.getByTeacherId(teacherId)

                val restrictions = RestrictionsEntity(
                    teacherId = teacherId,
                    maxDailyHours = maxDailyHoursValue,
                    sessionDurationMinutes = sessionDurationValue,
                    maxParticipantsPerSession = maxParticipantsValue,
                    maxSessionsPerStudentPerDay = maxSessionsPerStudentValue
                )

                if (restrictionOfTeacherID == null) {
                    restrictionsRepository.insert(restrictions)
                } else {
                    restrictionsRepository.update(restrictions)
                }

                _isSaved.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao guardar restricoes."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSaveHandled() {
        _isSaved.value = false
    }

    fun onErrorDismissed() {
        _errorMessage.value = null
    }

    private fun setDefaultValues() {
        _maxDailyHours.value = "8"
        _sessionDurationMinutes.value = "60"
        _maxParticipantsPerSession.value = "3"
        _maxSessionsPerStudentPerDay.value = "1"
    }

    private fun clearError() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }
}