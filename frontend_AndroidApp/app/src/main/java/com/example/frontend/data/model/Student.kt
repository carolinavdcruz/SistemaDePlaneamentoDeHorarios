package com.example.frontend.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: Int? = null,
    val name: String,
    val email: String,
    val maxDailySessions: Int
)