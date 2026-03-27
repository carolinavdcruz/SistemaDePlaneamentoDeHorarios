package com.example.frontend.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.InputBorder


@Composable
fun AvailabilitySelector(
    ownerId: Int,
    ownerType: String
) {
    val context = LocalContext.current

    // ViewModel (com AppModule)
    val viewModel = remember {
        AppModule.provideAvailabilityViewModel()
    }

    LaunchedEffect(Unit) {
        viewModel.load(ownerId, ownerType)
    }

    val availabilityList by viewModel.availabilityList.collectAsState()

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // ESTADOS GLOBAIS (O Assistente de IA vai atualizar estes valores)
    val selectedDays by viewModel.selectedDays.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val endTime by viewModel.endTime.collectAsState()

    // Estados do Assistente de IA
    var selectedTab by remember { mutableStateOf("Text") }
    var aiPrompt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- 1. SELETOR MANUAL ---
        Text("Manual Adjustment", color = Color.White, fontSize = 16.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                val isSelected = selectedDays.contains(day)

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { viewModel.toggleDay(day) },
                    color = if (isSelected) AccentPurple else Background,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, InputBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(day.first().toString(), color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            TimeBox(
                time = startTime,
                modifier = Modifier.weight(1f),
                onClick = {
                    showTimePicker(context, startTime) {
                        viewModel.setStartTime(it)
                    }
                }
            )

            Text(" to ")

            TimeBox(
                time = endTime,
                modifier = Modifier.weight(1f),
                onClick = {
                    showTimePicker(context, endTime) {
                        viewModel.setEndTime(it)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
/*
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
 */

        // BOTÃO (GUARDA NA BD)
        Button(
            onClick = {
                viewModel.saveAvailability(ownerId, ownerType)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Disponibilidade")
        }

        Spacer(modifier = Modifier.height(16.dp))

        availabilityList.forEach {
            Text(
                text = "Dia: ${it.dayOfWeek} | ${it.startTime} - ${it.endTime}",
                color = Color.White,
                fontSize = 12.sp
            )
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

@Preview(showBackground = true)
@Composable
fun AvailabilitySelectorPreview() {
    AvailabilitySelector(
        ownerId = 1,
        ownerType = "TEACHER"
    )
}