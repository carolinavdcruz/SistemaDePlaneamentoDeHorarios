package com.example.frontend.ui.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.remote.api.TeacherApi
import com.example.frontend.data.remote.dto.NotifyStudentsRequest
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.Red
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.viewmodel.student.StudentViewModel
import kotlinx.coroutines.launch

@Composable
fun TeacherLessonsScreen(teacherId: Int) {
    val studentViewModel = remember { AppModule.provideStudentViewModel() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SavedLessonsSection(teacherId = teacherId, studentViewModel = studentViewModel)
        NotifyStudentsCard(teacherId = teacherId, studentViewModel = studentViewModel)
    }
}

@Composable
fun SavedLessonsSection(teacherId: Int, studentViewModel: StudentViewModel) {

    val lessonViewModel = remember { AppModule.provideLessonViewModel() }
    val lessons by lessonViewModel.lessons.collectAsState()
    val isLoading by lessonViewModel.isLoading.collectAsState()
    val errorMessage by lessonViewModel.errorMessage.collectAsState()
    val successMessage by lessonViewModel.successMessage.collectAsState()
    val students by studentViewModel.students.collectAsState()

    val scope = rememberCoroutineScope()

    // Nomes dos alunos do professor, para não mostrar apenas o id na aula.
    LaunchedEffect(teacherId) {
        studentViewModel.loadStudentsByTeacherId(teacherId)
    }
    val studentsById = remember(students) { students.associateBy { it.id } }

    var selectedDate by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Aulas gravadas",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta o horário real persistido e o histórico do professor",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedDate,
                onValueChange = { selectedDate = it },
                label = { Text("Data da semana (AAAA-MM-DD)") },
                placeholder = { Text("2026-07-07") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    lessonViewModel.loadWeek(
                        teacherId = teacherId,
                        date = selectedDate
                    )
                },
                enabled = !isLoading && selectedDate.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Carregar semana")
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                label = { Text("Histórico: de (AAAA-MM-DD)") },
                placeholder = { Text("2026-07-01") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = toDate,
                onValueChange = { toDate = it },
                label = { Text("Histórico: até (AAAA-MM-DD)") },
                placeholder = { Text("2026-07-31") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    lessonViewModel.loadHistory(
                        teacherId = teacherId,
                        from = fromDate,
                        to = toDate
                    )
                },
                enabled = !isLoading && fromDate.isNotBlank() && toDate.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Carregar histórico")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (successMessage != null) {
                Text(
                    text = successMessage ?: "",
                    color = StatusActive,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }

                lessons.isEmpty() -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Background.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, InputBorder.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "Nenhuma aula carregada",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp)
                        )
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        lessons
                            .sortedWith(compareBy({ it.date }, { it.startTime }))
                            .forEach { lesson ->
                                SavedLessonCard(
                                    lesson = lesson,
                                    studentsById = studentsById,
                                    onCancelLesson = {
                                        lessonViewModel.cancelLesson(lesson.id) {
                                            if (selectedDate.isNotBlank()) {
                                                lessonViewModel.loadWeek(teacherId, selectedDate)
                                            }
                                        }
                                    },
                                    onCancelSeries = {
                                        val seriesId = lesson.seriesId
                                        if (seriesId != null) {
                                            lessonViewModel.cancelSeries(seriesId) {
                                                if (selectedDate.isNotBlank()) {
                                                    lessonViewModel.loadWeek(
                                                        teacherId,
                                                        selectedDate
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onMarkAttendance = { studentId, attended ->
                                        lessonViewModel.markAttendance(
                                            lesson.id,
                                            studentId,
                                            attended
                                        ) {
                                            if (selectedDate.isNotBlank()) {
                                                lessonViewModel.loadWeek(teacherId, selectedDate)
                                            } else if (fromDate.isNotBlank() && toDate.isNotBlank()) {
                                                lessonViewModel.loadHistory(
                                                    teacherId,
                                                    fromDate,
                                                    toDate
                                                )
                                            }
                                        }
                                    },
                                    onReschedule = { date, startTime, endTime ->
                                        lessonViewModel.updateLesson(
                                            lessonId = lesson.id,
                                            date = date,
                                            startTime = startTime,
                                            endTime = endTime
                                        ) {
                                            if (selectedDate.isNotBlank()) {
                                                lessonViewModel.loadWeek(teacherId, selectedDate)
                                            }
                                        }
                                    }
                                )
                            }
                    }
                }
            }
        }
    }
}


/**
 * Cartão para o professor enviar um aviso/email livre aos seus alunos
 * (todos, ou apenas os selecionados). Usa POST /teachers/{teacherId}/notify.
 */
@Composable
fun NotifyStudentsCard(teacherId: Int, studentViewModel: StudentViewModel) {
    val teacherApi = remember { TeacherApi() }
    val students by studentViewModel.students.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(teacherId) {
        studentViewModel.loadStudentsByTeacherId(teacherId)
    }

    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var sendToAll by remember { mutableStateOf(true) }
    val selectedStudentIds = remember { mutableStateOf(setOf<Int>()) }
    var isSending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Enviar aviso aos alunos",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Envia um email livre, escrito por ti, para todos os alunos ou só para alguns.",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Assunto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Mensagem") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = sendToAll,
                    onCheckedChange = {
                        sendToAll = it
                        if (it) selectedStudentIds.value = emptySet()
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentPurple)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (sendToAll) "Enviar a todos os alunos" else "Enviar só aos selecionados",
                    color = White,
                    fontSize = 13.sp
                )
            }

            if (!sendToAll) {
                Spacer(modifier = Modifier.height(8.dp))
                if (students.isEmpty()) {
                    Text("Sem alunos associados", color = TextSecondary, fontSize = 12.sp)
                } else {
                    Column {
                        students.forEach { student ->
                            val checked = selectedStudentIds.value.contains(student.id)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        selectedStudentIds.value = if (isChecked) {
                                            selectedStudentIds.value + student.id
                                        } else {
                                            selectedStudentIds.value - student.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentPurple)
                                )
                                Text(student.name, color = White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (feedback != null) {
                Text(
                    text = feedback ?: "",
                    color = StatusActive,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    feedback = null
                    isSending = true
                    scope.launch {
                        try {
                            val response = teacherApi.notifyStudents(
                                teacherId = teacherId,
                                request = NotifyStudentsRequest(
                                    studentIds = if (sendToAll) null else selectedStudentIds.value.toList(),
                                    subject = subject,
                                    message = message
                                )
                            )
                            feedback = "Aviso enviado a ${response.sentTo} aluno(s)."
                            subject = ""
                            message = ""
                        } catch (e: Exception) {
                            feedback = "Erro ao enviar aviso: ${e.message}"
                        } finally {
                            isSending = false
                        }
                    }
                },
                enabled = !isSending && subject.isNotBlank() && message.isNotBlank() &&
                        (sendToAll || selectedStudentIds.value.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Enviar")
                }
            }
        }
    }
}
