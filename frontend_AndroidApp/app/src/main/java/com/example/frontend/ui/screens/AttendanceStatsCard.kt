package com.example.frontend.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.theme.lightRed

/**
 * Resumo de presenças de um aluno (nº de aulas, presenças, faltas, % assiduidade).
 * Usado no perfil do próprio aluno e no detalhe do aluno visto pelo professor.
 */
@Composable
fun AttendanceStatsCard(studentId: Int) {
    val viewModel = remember { AppModule.provideLessonViewModel() }
    val summary by viewModel.attendanceSummary.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadAttendanceSummary(studentId)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Assiduidade",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (summary == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(24.dp))
                }
                return@Column
            }

            val s = summary!!

            if (s.totalLessons == 0) {
                Text(
                    "Ainda não há aulas registadas.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttendanceStat(label = "Total", value = s.totalLessons.toString(), color = White)
                AttendanceStat(label = "Presenças", value = s.attended.toString(), color = StatusActive)
                AttendanceStat(label = "Faltas", value = s.missed.toString(), color = lightRed)
                AttendanceStat(label = "Por marcar", value = s.pending.toString(), color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val ratePercent = (s.attendanceRate * 100).toInt()
            Text(
                "Taxa de assiduidade: $ratePercent%",
                color = if (ratePercent >= 75) StatusActive else lightRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { s.attendanceRate.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (ratePercent >= 75) StatusActive else lightRed,
                trackColor = InputBorder
            )
        }
    }
}

@Composable
private fun AttendanceStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}
