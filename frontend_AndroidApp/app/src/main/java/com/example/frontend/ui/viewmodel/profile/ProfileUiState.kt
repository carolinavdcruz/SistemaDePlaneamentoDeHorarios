package com.example.frontend.ui.viewmodel.profile

import com.example.frontend.data.model.OwnerType

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val role: OwnerType? = null,

    // Teacher only
    val classesCount: Int = 0,
    val studentsCount: Int = 0,
    val rating: Float = 0f,

    // Student only
    val maxDailySessions: Int = 0,
    val subjectsCount: Int = 0,
    val studentClassesCount: Int = 0,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)