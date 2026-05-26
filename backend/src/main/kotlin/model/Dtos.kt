package model

import kotlinx.serialization.Serializable

@Serializable
data class AvailabilityRequest(
    val ownerId: Int,
    val ownerType: String,   // "TEACHER" ou "STUDENT"
    val dayOfWeek: Int,
    val startTime: String,   // "09:00"
    val endTime: String      // "11:00"
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
data class TeacherResponse(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class StudentResponse(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class RestrictionsRequest(
    val teacherId: Int,
    val sessionDurationMinutes: Int,
    val maxDailyHours: Int,
    val maxParticipantsPerSession: Int,
    val maxSessionsPerStudentPerDay: Int
)

@Serializable
data class RestrictionsResponse(
    val teacherId: Int,
    val sessionDurationMinutes: Int,
    val maxDailyHours: Int,
    val maxParticipantsPerSession: Int,
    val maxSessionsPerStudentPerDay: Int
)

@Serializable
data class SessionRequest(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val studentIds: List<Int>
)

@Serializable
data class SaveScheduleRequest(
    val teacherId: Int,
    val sessions: List<SessionRequest>
)

@Serializable
data class ScheduleResponse(
    val scheduleId: Int,
    val status: String,
    val sessions: List<SessionResponse>
)

@Serializable
data class SessionResponse(
    val sessionId: Int,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val studentIds: List<Int>
)