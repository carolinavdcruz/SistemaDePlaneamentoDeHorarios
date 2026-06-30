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
    val teacherId: Int? = null,
    val maxDailySessions: Int = 1,
)

data class Teacher(
    val id: Int,
    val name: String,
    val email: String,
    //val sessionDurationMinutes: Int = 60,
    //val maxParticipantsPerSession: Int = 5
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

data class Restrictions(
    val teacherId: Int? = null,
    val sessionDurationMinutes: Int = 60,
    val maxDailyHours: Int = 3,
    val maxParticipantsPerSession: Int = 5,
    val maxSessionsPerStudentPerDay: Int = 1
)

data class StudentRestrictions(
    val studentId: Int,
    val weeklyHours: Int
)

data class Lesson(
    val id: Int,
    val teacherId: Int,
    val seriesId: String?,
    val date: java.time.LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: database.tables.LessonStatus,
    val students: List<LessonStudent>
)

data class LessonStudent(
    val studentId: Int,
    val attended: Boolean?,
    val attendedAt: java.time.Instant?
)