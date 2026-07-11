package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.AttendanceSummaryResponse
import com.example.frontend.data.remote.dto.CancelSeriesResponse
import com.example.frontend.data.remote.dto.CreateLessonsRequest
import com.example.frontend.data.remote.dto.LessonResponse
import com.example.frontend.data.remote.dto.MarkAttendanceRequest
import com.example.frontend.data.remote.dto.UpdateLessonRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class LessonApi {

    private val baseUrl = "http://10.0.2.2:8080"

    // Cria (e persiste) aulas concretas a partir das disponibilidades/restrições do
    // professor, opcionalmente repetidas por várias semanas (recorrência).
    suspend fun create(request: CreateLessonsRequest): List<LessonResponse> {
        return client.post("$baseUrl/lessons/create") {
            setBody(request)
        }.body()
    }

    suspend fun getById(lessonId: Int): LessonResponse {
        return client.get("$baseUrl/lessons/$lessonId").body()
    }

    suspend fun getHistory(teacherId: Int, from: String, to: String): List<LessonResponse> {
        return client.get("$baseUrl/lessons/history") {
            parameter("teacherId", teacherId)
            parameter("from", from)
            parameter("to", to)
        }.body()
    }

    // Horário (todas as aulas) da semana que contém `date` (qualquer dia dessa semana).
    suspend fun getWeek(teacherId: Int, date: String): List<LessonResponse> {
        return client.get("$baseUrl/lessons/week") {
            parameter("teacherId", teacherId)
            parameter("date", date)
        }.body()
    }

    // Horário do ALUNO (qualquer professor) para a semana que contém `date`.
    suspend fun getWeekForStudent(studentId: Int, date: String): List<LessonResponse> {
        return client.get("$baseUrl/lessons/student/week") {
            parameter("studentId", studentId)
            parameter("date", date)
        }.body()
    }

    suspend fun getHistoryForStudent(studentId: Int, from: String, to: String): List<LessonResponse> {
        return client.get("$baseUrl/lessons/student/history") {
            parameter("studentId", studentId)
            parameter("from", from)
            parameter("to", to)
        }.body()
    }

    // Edita uma ocorrência isolada (data/hora); destaca-a da série.
    // Pode devolver 409 (conflito de horário) - tratar a exceção no chamador.
    suspend fun update(lessonId: Int, request: UpdateLessonRequest): LessonResponse {
        return client.patch("$baseUrl/lessons/$lessonId") {
            setBody(request)
        }.body()
    }

    suspend fun cancelLesson(lessonId: Int) {
        client.patch("$baseUrl/lessons/$lessonId/cancel")
    }

    suspend fun cancelSeries(seriesId: String): CancelSeriesResponse {
        return client.delete("$baseUrl/lessons/series/$seriesId").body()
    }

    suspend fun markAttendance(lessonId: Int, studentId: Int, attended: Boolean) {
        client.patch("$baseUrl/lessons/$lessonId/students/$studentId/attendance") {
            setBody(MarkAttendanceRequest(attended))
        }
    }

    suspend fun getAttendanceSummary(studentId: Int): AttendanceSummaryResponse {
        return client.get("$baseUrl/lessons/students/$studentId/attendance-summary").body()
    }
}
