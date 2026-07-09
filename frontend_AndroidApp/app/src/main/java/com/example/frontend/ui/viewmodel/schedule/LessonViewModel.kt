package com.example.frontend.ui.viewmodel.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.remote.api.LessonApi
import com.example.frontend.data.remote.dto.AttendanceSummaryResponse
import com.example.frontend.data.remote.dto.GenerateLessonsRequest
import com.example.frontend.data.remote.dto.LessonResponse
import com.example.frontend.data.remote.dto.UpdateLessonRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LessonViewModel(
    private val lessonApi: LessonApi
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<LessonResponse>>(emptyList())
    val lessons: StateFlow<List<LessonResponse>> = _lessons

    private val _selectedLesson = MutableStateFlow<LessonResponse?>(null)
    val selectedLesson: StateFlow<LessonResponse?> = _selectedLesson

    private val _attendanceSummary = MutableStateFlow<AttendanceSummaryResponse?>(null)
    val attendanceSummary: StateFlow<AttendanceSummaryResponse?> = _attendanceSummary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun generate(
        teacherId: Int,
        startDate: String,
        recurrence: String,
        occurrences: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val result = lessonApi.generate(
                    GenerateLessonsRequest(
                        teacherId = teacherId,
                        startDate = startDate,
                        recurrence = recurrence,
                        occurrences = occurrences
                    )
                )
                _lessons.value = result
                _successMessage.value = "${result.size} aula(s) gerada(s) com sucesso."
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao gerar aulas."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLessonById(lessonId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                _selectedLesson.value = lessonApi.getById(lessonId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar aula."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWeek(teacherId: Int, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                _lessons.value = lessonApi.getWeek(teacherId, date)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar semana."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHistory(teacherId: Int, from: String, to: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                _lessons.value = lessonApi.getHistory(teacherId, from, to)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar histórico."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelLesson(lessonId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                lessonApi.cancelLesson(lessonId)
                _successMessage.value = "Aula cancelada com sucesso."
                onSuccess?.invoke()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao cancelar aula."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelSeries(seriesId: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val response = lessonApi.cancelSeries(seriesId)
                _successMessage.value = "${response.cancelledCount} aula(s) cancelada(s) na série."
                onSuccess?.invoke()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao cancelar série."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateLesson(
        lessonId: Int,
        date: String,
        startTime: String,
        endTime: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val updated = lessonApi.update(
                    lessonId = lessonId,
                    request = UpdateLessonRequest(
                        date = date,
                        startTime = startTime,
                        endTime = endTime
                    )
                )
                _selectedLesson.value = updated
                _successMessage.value = "Aula atualizada com sucesso."
                onSuccess?.invoke()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao atualizar aula."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAttendance(lessonId: Int, studentId: Int, attended: Boolean, onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                lessonApi.markAttendance(lessonId, studentId, attended)
                _successMessage.value = "Presença atualizada com sucesso."
                onSuccess?.invoke()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao atualizar presença."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAttendanceSummary(studentId: Int) {
        viewModelScope.launch {
            _errorMessage.value = null

            try {
                _attendanceSummary.value = lessonApi.getAttendanceSummary(studentId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erro ao carregar assiduidade."
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun clearSelectedLesson() {
        _selectedLesson.value = null
    }
}