package com.example.frontend.data.model

data class ScheduledSession (
    val teacherId: Int,
    val studentIds: List<Int>,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)