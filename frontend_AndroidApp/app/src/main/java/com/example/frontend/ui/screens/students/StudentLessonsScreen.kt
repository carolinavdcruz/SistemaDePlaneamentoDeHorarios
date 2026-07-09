package com.example.frontend.ui.screens.students

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.data.remote.api.LessonApi
import com.example.frontend.data.remote.dto.LessonResponse
import com.example.frontend.ui.screens.StudentLessonRow
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.Red
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White

@Composable
fun StudentLessonsScreen(studentId: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        StudentScheduleSection(studentId = studentId)
    }
}

/**
 * Horário do ALUNO no seu ecrã principal: carrega automaticamente as aulas
 * da semana atual (com qualquer professor) via GET /lessons/student/week.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudentScheduleSection(studentId: Int) {
    val lessonApi = remember { LessonApi() }

    var lessons by remember { mutableStateOf<List<LessonResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studentId) {
        isLoading = true
        errorMessage = null
        try {
            val today = java.time.LocalDate.now().toString()
            lessons = lessonApi.getWeekForStudent(studentId, today)
        } catch (e: Exception) {
            errorMessage = "Não foi possível carregar o teu horário: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "O teu horário desta semana",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> CircularProgressIndicator(color = AccentPurple)
                errorMessage != null -> Text(errorMessage ?: "", color = Red, fontSize = 13.sp)
                lessons.isEmpty() -> Text(
                    text = "Ainda não tens aulas agendadas para esta semana.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    lessons
                        .sortedWith(compareBy({ it.date }, { it.startTime }))
                        .forEach { lesson -> StudentLessonRow(lesson) }
                }
            }
        }
    }
}
