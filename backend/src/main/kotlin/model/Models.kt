package model

import java.time.LocalTime

data class TimeSlot(
    val id: Int = 0,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class Student(
    val id: Int,
    val name: String,
    val email: String,
    val maxDailySessions: Int = 1
)

data class Teacher(
    val id: Int,
    val name: String,
    val email: String,
    val sessionDurationMinutes: Int = 60,
    val maxParticipantsPerSession: Int = 5
)

data class Session(
    val slot: TimeSlot,
    val studentIds: List<Int>
)

// usado no AvailabilityService
data class TimeSlotEntity(
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)