package com.example.frontend.model

import java.util.UUID

data class Teacher(
    val id: UUID,
    val name: String,
    val email: String,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int
)