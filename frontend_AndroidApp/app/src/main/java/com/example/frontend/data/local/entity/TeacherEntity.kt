package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.frontend.data.model.Restrictions

@Entity(tableName = "teacher", primaryKeys = ["id"])
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    //val restrictions: Restrictions
)