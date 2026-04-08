package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restrictions",)
data class RestrictionsEntity (
    @PrimaryKey(autoGenerate = false) val teacherId: Int,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int = 1,
    val maxSessionsPerStudentPerDay: Int = 1
)