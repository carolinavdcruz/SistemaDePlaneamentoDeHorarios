package com.example.frontend.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val id: Int = 0,
    val name: String,
    val email: String,
    //val restrictions: Restrictions
)