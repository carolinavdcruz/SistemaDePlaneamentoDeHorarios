package com.example.frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "availability")
data class AvailabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: Int,
    val ownerType: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

/*
Cria automaticamente a tabela:
availability(
    id INTEGER PRIMARY KEY,
    ownerId INTEGER,
    ownerType TEXT,
    dayOfWeek INTEGER,
    startTime TEXT,
    endTime TEXT
)
 */