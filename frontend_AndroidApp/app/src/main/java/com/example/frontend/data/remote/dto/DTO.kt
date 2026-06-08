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
data class SaveScheduleRequest(
    val teacherId: Int,
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
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val userId: Int,
    val ownerType: String
)
