package com.example.frontend.model

import java.util.UUID

data class Student(
    val id: UUID,
    val name: String,
    val email: String,
    val maxDailySessions: Int
)