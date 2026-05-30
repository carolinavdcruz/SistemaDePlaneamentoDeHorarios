package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teacher")
data class TeacherEntity(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val email: String,
    val password: String
    //val restrictions: Restrictions
)