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

@Serializable
data class SaveScheduleRequest(
    val teacherId: Int,
    val title: String? = null,
    val weekStart: String,
    val weekEnd: String,
    val sessions: List<SessionRequest>
)

@Serializable
data class SessionRequest(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val studentIds: List<Int>
)

@Serializable
data class SchedulePlanResponse(
    val id: Int,
    val teacherId: Int,
    val title: String,
    val status: String,
    val weekStart: String,
    val weekEnd: String,
    val createdAt: String,
    val acceptedAt: String?,
    val sessions: List<ScheduleSessionResponse>
)