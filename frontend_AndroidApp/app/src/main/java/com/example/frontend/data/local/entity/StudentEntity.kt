package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student")
data class StudentEntity(
    @PrimaryKey val id: Int = 0,
    val teacherId: Int? = null,
    val name: String,
    val email: String,
    val maxDailySessions: Int
)