package com.example.frontend.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary

@Composable
fun AvailabilitySelector() {
    val context = LocalContext.current
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed")) }
    
    // Estados para as horas
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("17:00") }

    fun showTimePicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true).show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Weekly Availability",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Seletor de Dias
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                        },
                    color = if (isSelected) AccentPurple else Background,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, InputBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = day.first().toString(),
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Intervalo de Horas funcional
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeBox(
                time = startTime,
                modifier = Modifier.weight(1f),
                onClick = { showTimePicker(startTime) { startTime = it } }
            )
            Text("to", color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
            TimeBox(
                time = endTime,
                modifier = Modifier.weight(1f),
                onClick = { showTimePicker(endTime) { endTime = it } }
            )
        }
    }
}

@Composable
fun TimeBox(time: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        color = Background,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(time, color = Color.White, fontSize = 14.sp)
            Icon(Icons.Default.DateRange, "", tint = AccentPurple, modifier = Modifier.size(16.dp))
        }
    }
}
