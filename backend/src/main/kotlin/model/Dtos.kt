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