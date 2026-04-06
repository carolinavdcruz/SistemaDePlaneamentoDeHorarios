package com.example.frontend.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TimeSlot(
    val id: Int,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAvailable: Boolean = true // UI logic: true se não estiver cheio
) {
    val displayTime: String
        @RequiresApi(Build.VERSION_CODES.O)
        get() = "${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
}