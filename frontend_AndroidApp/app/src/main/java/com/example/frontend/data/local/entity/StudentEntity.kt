package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student", primaryKeys = ["id"])
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val maxDailySessions: Int
)