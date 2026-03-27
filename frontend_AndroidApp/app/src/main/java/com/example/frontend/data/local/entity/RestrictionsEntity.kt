package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "restrictions",
    foreignKeys = [
        ForeignKey(
            entity = TeacherEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["teacherId"])]
)
data class RestrictionsEntity (
    @PrimaryKey(autoGenerate = false) val teacherId: Int,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int
)