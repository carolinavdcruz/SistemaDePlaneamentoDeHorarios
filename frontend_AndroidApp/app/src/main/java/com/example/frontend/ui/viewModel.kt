package com.example.frontend.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    fun load() {
        isLoading = true
    }
}