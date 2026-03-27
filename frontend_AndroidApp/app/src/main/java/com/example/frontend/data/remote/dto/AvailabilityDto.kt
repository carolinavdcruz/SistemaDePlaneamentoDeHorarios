package com.example.frontend.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AvailabilityRequest(
    val ownerId: Int,
    val ownerType: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

@Serializable
data class AvailabilityResponse(
    val id: Int,
    val ownerId: Int,
    val ownerType: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)