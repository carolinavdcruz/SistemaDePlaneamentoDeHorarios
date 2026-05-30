package com.example.frontend.ui.viewmodel.profile

import com.example.frontend.data.model.OwnerType

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val role: OwnerType? = null,
    val teacherName: String? = null,
    val maxDailySessions: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)