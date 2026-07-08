package com.example.frontend.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StudentRequest(
    val name: String,
    val email: String,
    val password: String,
    val teacherId: Int? = null,
)

@Serializable
data class StudentResponse(
    val id: Int,
    val name: String,
    val email: String,
    //val password: String,
    val teacherId: Int?,
)

@Serializable
data class AssignTeacherRequest(
    val studentId: Int,
    val teacherId: Int
)

@Serializable
data class TeacherRequest(
    val name: String,
    val email: String,
    val password: String
    //val sessionDurationMinutes: Int = 60,
    //val maxParticipantsPerSession: Int = 5
)

@Serializable
data class TeacherResponse(
    val id: Int,
    val name: String,
    val email: String,
    //val password: String
)

@Serializable
data class AvailabilityRequest(
    val ownerId: Int,
    val ownerType: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

@Serializable
data class AvailabilityResponse(
    val id: Int,
    val ownerId: Int,
    val ownerType: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

@Serializable
data class RestrictionsRequest(
    val teacherId: Int,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int,
    val maxSessionsPerStudentPerDay: Int
)

@Serializable
data class RestrictionsResponse(
    val teacherId: Int,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int,
    val maxSessionsPerStudentPerDay: Int
)

@Serializable
data class StudentRestrictionsRequest(
    val studentId: Int,
    val weeklyHours: Int
)

@Serializable
data class StudentRestrictionsResponse(
    val studentId: Int,
    val weeklyHours: Int
)

@Serializable
data class ScheduleCreateRequest(
    val teacherId: Int
)

@Serializable
data class ScheduleSessionResponse(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val studentIds: List<Int>
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val userId: Int,
    val ownerType: String
)

// ---- Lessons / histórico / recorrência / presenças ----

@Serializable
data class GenerateLessonsRequest(
    val teacherId: Int,
    val startDate: String,
    val recurrence: String = "WEEKLY", // "NONE" ou "WEEKLY"
    val occurrences: Int = 1
)

@Serializable
data class LessonStudentResponse(
    val studentId: Int,
    val attended: Boolean?,
    val attendedAt: String?
)

@Serializable
data class LessonResponse(
    val id: Int,
    val teacherId: Int,
    val seriesId: String?,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val students: List<LessonStudentResponse>
)

@Serializable
data class MarkAttendanceRequest(
    val attended: Boolean
)

@Serializable
data class UpdateLessonRequest(
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)

@Serializable
data class CancelSeriesResponse(
    val cancelledCount: Int
)

@Serializable
data class AttendanceSummaryResponse(
    val studentId: Int,
    val totalLessons: Int,
    val attended: Int,
    val missed: Int,
    val pending: Int,
    val attendanceRate: Double
)

@Serializable
data class NotifyStudentsRequest(
    val studentIds: List<Int>? = null,
    val subject: String,
    val message: String
)

@Serializable
data class NotifyStudentsResponse(
    val sentTo: Int
)
