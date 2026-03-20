package com.example.frontend.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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

    // ESTADOS GLOBAIS (O Assistente de IA vai atualizar estes valores)
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed")) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("17:00") }

    // Estados do Assistente de IA
    var selectedTab by remember { mutableStateOf("Text") }
    var aiPrompt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- 1. SELETOR MANUAL ---
        Text("Manual Adjustment", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Surface(
                    modifier = Modifier.size(40.dp).clickable {
                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                    },
                    color = if (isSelected) AccentPurple else Background,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, InputBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(day.first().toString(), color = if (isSelected) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TimeBox(time = startTime, modifier = Modifier.weight(1f), onClick = { showTimePicker(context, startTime) { startTime = it } })
            Text("to", color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
            TimeBox(time = endTime, modifier = Modifier.weight(1f), onClick = { showTimePicker(context, endTime) { endTime = it } })
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = InputBorder, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. ASSISTENTE DE IA (A PARTE DA IMAGEM) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2D2D3D))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = AccentPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Done, null, tint = AccentPurple, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Availability Assistant", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Describe your week naturally", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seletor de Tabs (Simplificado)
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp).background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp)).padding(4.dp)
            ) {
                listOf("Text", "Doc", "Audio").forEach { tab ->
                    val isTabSelected = selectedTab == tab
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTabSelected) AccentPurple else Color.Transparent)
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tab, color = if (isTabSelected) Color.White else TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Texto da IA
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                color = Background,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, InputBorder.copy(0.5f))
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    if (aiPrompt.isEmpty()) {
                        Text("e.g. I'm available Mon and Fri 10am-2pm", color = TextSecondary.copy(0.5f), fontSize = 13.sp)
                    }
                    BasicTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    /* AQUI: Chamas o teu Backend.
                       O Backend responde com JSON.
                       Ex: Se a IA devolver {"days": ["Mon"], "start": "10:00", "end": "14:00"}
                       Tu fazes:
                       selectedDays = setOf("Mon")
                       startTime = "10:00"
                       endTime = "14:00"
                    */
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple.copy(0.15f), contentColor = AccentPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Process with AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

private fun showTimePicker(context: Context, currentTime: String, onTimeSelected: (String) -> Unit) {
    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
        val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
        onTimeSelected(formattedTime)
    }, hour, minute, true).show()
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
