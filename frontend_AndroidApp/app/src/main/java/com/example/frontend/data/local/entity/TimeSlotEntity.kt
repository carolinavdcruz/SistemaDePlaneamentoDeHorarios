package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeslots")
data class TimeSlotEntity(
    @PrimaryKey val id: Int,
    val dayOfWeek: Int,
    val startTime: String,   // Armazenado como "HH:mm:ss"
    val endTime: String
)