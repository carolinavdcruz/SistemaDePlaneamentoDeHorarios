package com.example.frontend.ui.screens.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.data.model.ScheduledSession
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.Red
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextMain
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.viewmodel.schedule.ScheduleUiState

private val DAY_NAMES = mapOf(
    1 to "Segunda",
    2 to "Terça",
    3 to "Quarta",
    4 to "Quinta",
    5 to "Sexta",
    6 to "Sábado",
    7 to "Domingo"
)

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
                "Proposta de horário semanal",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is ScheduleUiState.Idle,
                is ScheduleUiState.Empty -> {
                    val message = if (uiState is ScheduleUiState.Empty) {
                        uiState.reason
                    } else {
                        "Carrega em \"Criar Horário\" para veres a proposta."
                    }
                    ScheduleEmptyState(message)
                }

                is ScheduleUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }

                is ScheduleUiState.Error -> {
                    Text("Erro: ${uiState.message}", color = Red, fontSize = 13.sp)
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

@Composable
private fun ScheduleEmptyState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Icon(Icons.Default.DateRange, "", tint = TextSecondary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Sem proposta ativa",
            color = TextMain,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(message, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun StudentChip(name: String) {
    Surface(
        color = AccentPurple.copy(alpha = 0.20f),
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

@Composable
private fun CalendarSessionCard(
    session: ScheduledSession,
    studentNames: Map<Int, String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AccentPurple.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${session.startTime} - ${session.endTime}",
                color = AccentPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (session.studentIds.isEmpty()) {
                Text(
                    text = "Sem alunos atribuídos",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.studentIds.forEach { studentId ->
                        StudentChip(name = studentNames[studentId] ?: "Aluno $studentId")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDayColumn(
    dayOfWeek: Int,
    sessions: List<ScheduledSession>,
    studentNames: Map<Int, String>
) {
    Surface(
        modifier = Modifier.width(220.dp),
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = DAY_NAMES[dayOfWeek] ?: "Dia $dayOfWeek",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (sessions.isEmpty()) "Sem sessões" else "${sessions.size} sessão(ões)",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (sessions.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Background.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, InputBorder.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "Dia livre",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sessions.forEach { session ->
                        CalendarSessionCard(session = session, studentNames = studentNames)
                    }
                }
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
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        (1..7).forEach { day ->
            WeekDayColumn(
                dayOfWeek = day,
                sessions = grouped[day].orEmpty().sortedBy { it.startTime },
                studentNames = studentNames
            )
        }
    }
}
