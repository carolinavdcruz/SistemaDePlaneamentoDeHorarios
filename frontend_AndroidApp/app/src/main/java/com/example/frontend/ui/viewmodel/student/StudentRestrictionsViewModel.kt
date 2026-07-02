package com.example.frontend.ui.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.repository.StudentRestrictionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentRestrictionsViewModel(
    private val studentRestrictionsRepository: StudentRestrictionsRepository
) : ViewModel() {

    private val _weeklyHours = MutableStateFlow("")
    val weeklyHours: StateFlow<String> = _weeklyHours

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadRestrictions(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val restrictions = studentRestrictionsRepository.getByStudentId(studentId)

                if (restrictions != null) {
                    _weeklyHours.value = restrictions.weeklyHours.toString()
                } else {
                    setDefaultValues()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar restricoes do aluno."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setWeeklyHours(value: String) {
        if (value.all { it.isDigit() } || value.isEmpty()) {
            _weeklyHours.value = value
            _isSaved.value = false
            clearError()
        }
    }

    fun saveRestrictions(studentId: Int) {
        val weeklyHoursValue = _weeklyHours.value.toIntOrNull()

        _isSaved.value = false

        if (weeklyHoursValue == null || weeklyHoursValue <= 0) {
            _errorMessage.value = "Desired weekly hours invalid."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                studentRestrictionsRepository.save(studentId, weeklyHoursValue)
                _isSaved.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao guardar restricoes do aluno."
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
        _weeklyHours.value = "3"
    }

    private fun clearError() {
        if (_errorMessage.value != null) {
            _errorMessage.value = null
        }
    }


}