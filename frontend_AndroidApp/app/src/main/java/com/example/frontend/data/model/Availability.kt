package com.example.frontend.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Availability(
    val id: Int? = null,
    val teacherId: Int? = null,
    val studentId: Int? = null,
    val dayOfWeek: Int,
    val startTime: String, // ex: "09:00"
    val endTime: String

)