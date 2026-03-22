package com.example.frontend.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Availability(
    val id: Int = 0,
    val ownerId: Int,
    val ownerType: OwnerType,
    val dayOfWeek: Int,
    val startTime: String, // ex: "09:00"
    val endTime: String
)