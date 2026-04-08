package com.example.frontend.data.model

data class Restrictions (
    val teacherId: Int,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int = 1,
    val maxSessionsPerStudentPerDay: Int = 1
)