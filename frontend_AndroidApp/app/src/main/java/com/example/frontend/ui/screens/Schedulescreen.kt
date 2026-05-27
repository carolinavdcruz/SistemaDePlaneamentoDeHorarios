package com.example.frontend.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.model.ScheduledSession
import com.example.frontend.ui.theme.*
import com.example.frontend.ui.viewmodel.schedule.ScheduleUiState
import com.example.frontend.ui.viewmodel.schedule.ScheduleViewModel

private val DAY_NAMES = mapOf(
    1 to "Monday", 2 to "Tuesday", 3 to "Wednesday",
    4 to "Thursday",  5 to "Friday", 6 to "Saturday", 7 to "Sunday"
)

// Button Create Schedule
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GenerateScheduleButton(teacherId: Int) {
    val viewModel: ScheduleViewModel = remember { AppModule.provideScheduleViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is ScheduleUiState.Loading

    // Guarda automaticamente quando gera com sucesso
    LaunchedEffect(uiState) {
        if (uiState is ScheduleUiState.Success) {
            viewModel.saveSchedule(teacherId, (uiState as ScheduleUiState.Success).sessions)
        }
    }

    Column {
        ProposedScheduleCard(uiState = uiState)

        Spacer(modifier = Modifier.height(20.dp))

        // Botões aceitar/rejeitar só aparecem quando há horário gerado
        if (uiState is ScheduleUiState.Success) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rejeitar
                OutlinedButton(
                    onClick = { viewModel.rejectSchedule() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Rejeitar", color = Color.Red, fontSize = 14.sp)
                }

                // Aceitar
                Button(
                    onClick = { viewModel.acceptSchedule() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Aceitar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Mensagem de aceite
        if (uiState is ScheduleUiState.Accepted) {
            Surface(
                color = StatusActive.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "✓ Horário aceite e guardado!",
                    color = StatusActive,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Botão gerar
        Button(
            onClick = { viewModel.generateSchedule(teacherId) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gerar Horário", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Card of schedule
@Composable
fun ProposedScheduleCard(uiState: ScheduleUiState) {
    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Proposed Weekly Schedule",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is ScheduleUiState.Idle,
                is ScheduleUiState.Empty -> {
                    val msg = if (uiState is ScheduleUiState.Empty) uiState.reason
                    else "Generate to see blocks"
                    ScheduleEmptyState(msg)
                }
                is ScheduleUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
                is ScheduleUiState.Error -> {
                    Text("Erro: ${uiState.message}", color = Color.Red, fontSize = 13.sp)
                }
                is ScheduleUiState.Success -> {
                    Text(
                        "${uiState.sessions.size} sessão(ões) gerada(s)",
                        color = StatusActive,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    WeeklyScheduleView(
                        sessions = uiState.sessions,
                        studentNames = uiState.studentName
                    )
                }
                is ScheduleUiState.Accepted -> {
                    ScheduleEmptyState("Horário aceite com sucesso!")
                }
            }
        }
    }
}

// Empty State
@Composable
private fun ScheduleEmptyState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        Icon(Icons.Default.DateRange, "", tint = TextSecondary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("No active proposal", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(message, color = TextSecondary, fontSize = 12.sp)
    }
}

// Individual session card
@Composable
fun SessionCardSmall(session: ScheduledSession) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, null, tint = AccentPurple, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    DAY_NAMES[session.dayOfWeek] ?: "Dia ${session.dayOfWeek}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text("${session.startTime} – ${session.endTime}", color = TextSecondary, fontSize = 12.sp)
            }
            Surface(color = AccentPurple.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
                Text(
                    "${session.studentIds.size} aluno(s)",
                    color = AccentPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyScheduleView(
    sessions: List<ScheduledSession>,
    studentNames: Map<Int, String>
) {
    val grouped = sessions.groupBy { it.dayOfWeek }
    val orderedDays = (1..7).toList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        orderedDays.forEach { day ->
            DayScheduleCard(
                dayOfWeek = day,
                sessions = grouped[day].orEmpty().sortedBy { it.startTime },
                studentNames = studentNames
            )
        }
    }
}

@Composable
fun DayScheduleCard(
    dayOfWeek: Int,
    sessions: List<ScheduledSession>,
    studentNames: Map<Int, String>
) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = DAY_NAMES[dayOfWeek] ?: "Day $dayOfWeek",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (sessions.isEmpty()) {
                Text(
                    text = "No sessions scheduled",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sessions.forEach { session ->
                        SessionDetailCard(
                            session = session,
                            studentNames = studentNames
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionDetailCard(
    session: ScheduledSession,
    studentNames: Map<Int, String>
) {
    Surface(
        color = Background,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${session.startTime} - ${session.endTime}",
                color = AccentPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (session.studentIds.isEmpty()) {
                Text(
                    text = "No students assigned",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    session.studentIds.forEach { studentId ->
                        StudentChip(
                            name = studentNames[studentId] ?: "Student $studentId"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentChip(name: String) {
    Surface(
        color = AccentPurple.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = name,
            color = AccentPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

