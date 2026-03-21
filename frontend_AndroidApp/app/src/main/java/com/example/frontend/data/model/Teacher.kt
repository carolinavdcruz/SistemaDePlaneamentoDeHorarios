package com.example.frontend.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val id: Int? = null,
    val name: String,
    val email: String,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int
)