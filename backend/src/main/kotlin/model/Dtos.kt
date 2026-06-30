package model

import kotlinx.serialization.Serializable

@Serializable
data class AvailabilityRequest(
    val ownerId: Int,
    val ownerType: OwnerType,
    val dayOfWeek: Int,
    val startTime: String,   // "09:00"
    val endTime: String      // "11:00"
)

@Serializable
data class AvailabilityResponse(
    val id: Int,
    val ownerId: Int,
    val ownerType: OwnerType,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

@Serializable
data class TeacherRequest(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class TeacherResponse(
    val id: Int,
    val name: String,
    val email: String,
    //val password: String,
)

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
data class StudentRestrictionsRequest (
    val studentId: Int,
    val weeklyHours: Int
)

@Serializable
data class StudentRestrictionsResponse (
    val studentId: Int,
    val weeklyHours: Int
)

@Serializable
data class AssignTeacherRequest(
    val studentId: Int,
    val teacherId: Int
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
    val ownerType: OwnerType
)

// Lessons / histórico / recorrência / presenças
@Serializable
enum class RecurrenceType { NONE, WEEKLY }

@Serializable
data class GenerateLessonsRequest(
    val teacherId: Int,
    val startDate: String,           // "2026-09-07" (segunda-feira da semana de início)
    val recurrence: RecurrenceType = RecurrenceType.WEEKLY,
    val occurrences: Int = 1         // nº de semanas a gerar (1 = só essa semana)
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
    val date: String,                // "2026-09-07"
    val startTime: String,
    val endTime: String,
    val status: String,
    val students: List<LessonStudentResponse>
)

@Serializable
data class MarkAttendanceRequest(
    val attended: Boolean
)