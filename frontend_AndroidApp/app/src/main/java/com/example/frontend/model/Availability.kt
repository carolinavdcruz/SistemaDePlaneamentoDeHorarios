package com.example.frontend.model

import java.time.LocalTime
import java.util.UUID

data class Availability(
    val id: UUID,
    val ownerId: UUID,
    val ownerType: OwnerType,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)