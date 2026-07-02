package com.example.frontend.ui.screens

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
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
import com.example.frontend.ui.viewmodel.schedule.ScheduleViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

private val DAY_NAMES = mapOf(
    1 to "Monday", 2 to "Tuesday", 3 to "Wednesday",
    4 to "Thursday", 5 to "Friday", 6 to "Saturday", 7 to "Sunday"
)

// Button Create Schedule
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GenerateScheduleButton(teacherId: Int) {
    val context = LocalContext.current
    val viewModel: ScheduleViewModel = remember { AppModule.provideScheduleViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is ScheduleUiState.Loading

    //val googleCalen = GoogleCalendarManager(LocalContext.current)

    // Google Sign-In launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d(
            "ScheduleScreen",
            "Google Sign-In resultCode=${result.resultCode}, data=${result.data}"
        )

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            android.util.Log.d(
                "ScheduleScreen",
                "Google Sign-In OK, account=${account.email}"
            )

            if (viewModel.hasCalendarPermission(context)) {
                viewModel.acceptSchedule(context)
            } else {
                viewModel.setUiError("Permissão Google Calendar não concedida.")
            }
        } catch (e: ApiException) {
            android.util.Log.e(
                "ScheduleScreen",
                "Google Sign-In ApiException: code=${e.statusCode}, message=${e.message}",
                e
            )
            viewModel.setUiError("Google Sign-In falhou. Código=${e.statusCode}")
        } catch (e: Exception) {
            android.util.Log.e(
                "ScheduleScreen",
                "Google Sign-In erro inesperado: ${e.message}",
                e
            )
            viewModel.setUiError("Erro inesperado no login Google.")
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ScheduleUiState.Error &&
            (uiState as ScheduleUiState.Error).message == "GOOGLE_SIGN_IN_REQUIRED"
        ) {
            signInLauncher.launch(viewModel.buildGoogleSignInIntent(context))
        }
    }

    Column {
        ProposedScheduleCard(uiState = uiState)
        Spacer(modifier = Modifier.height(20.dp))

        if (uiState is ScheduleUiState.Success) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        val activity = context as Activity
                        val account = GoogleSignIn.getLastSignedInAccount(context)

                        when {
                            account == null || !viewModel.hasCalendarPermission(context) -> {
                                signInLauncher.launch(viewModel.buildGoogleSignInIntent(context))
                            }

                            else -> {
                                viewModel.acceptSchedule(context)
                            }
                        }

                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Aceitar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            RecurrenceGenerateSection(teacherId = teacherId)
            Spacer(modifier = Modifier.height(12.dp))
        }

        SavedLessonsSection(teacherId = teacherId)
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState is ScheduleUiState.Accepted) {
            Surface(
                color = StatusActive.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Horário aceite e adicionado ao Google Calendar!",
                    color = StatusActive,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.generateSchedule(teacherId) },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Criar Horário", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                color = White,
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

// Empty State
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
            "No active proposal",
            color = TextMain,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(message, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun StudentCard(name: String) {
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
fun CalendarSessionCard(
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
                    text = "No students assigned",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.studentIds.forEach { studentId ->
                        StudentCard(
                            name = studentNames[studentId] ?: "Student $studentId"
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WeekDayColumn(
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
                text = DAY_NAMES[dayOfWeek] ?: "Day $dayOfWeek",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (sessions.isEmpty()) "No sessions" else "${sessions.size} session(s)",
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
                        text = "Free day",
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
                        CalendarSessionCard(
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
fun WeeklyScheduleView(
    sessions: List<ScheduledSession>,
    studentNames: Map<Int, String>
) {
    val grouped = sessions.groupBy { it.dayOfWeek }
    val orderedDays = (1..7).toList()
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        orderedDays.forEach { day ->
            WeekDayColumn(
                dayOfWeek = day,
                sessions = grouped[day].orEmpty().sortedBy { it.startTime },
                studentNames = studentNames
            )
        }
    }
}

/**
 * Secção para persistir o horário gerado como aulas concretas, com data de início
 * e opção de repetir a mesma semana N vezes (recorrência tipo Google Calendar).
 * Usa LessonViewModel/POST /lessons/generate (separado do fluxo "Aceitar" com o
 * Google Calendar acima, que continua a usar o ScheduleViewModel).
 */
@Composable
fun RecurrenceGenerateSection(teacherId: Int) {
    val lessonViewModel = remember { AppModule.provideLessonViewModel() }
    val lessons by lessonViewModel.lessons.collectAsState()
    val isLoading by lessonViewModel.isLoading.collectAsState()
    val errorMessage by lessonViewModel.errorMessage.collectAsState()

    var startDate by remember { mutableStateOf("") } // "2026-09-07"
    var repeatEnabled by remember { mutableStateOf(false) }
    var occurrences by remember { mutableStateOf(4) }

    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Gerar e gravar aulas",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Cria aulas com data real (segunda-feira da semana de início)",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Data de início (segunda-feira, AAAA-MM-DD)") },
                placeholder = { Text("2026-09-07") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Repetir semanalmente", color = White, fontSize = 14.sp)
                Switch(
                    checked = repeatEnabled,
                    onCheckedChange = { repeatEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentPurple)
                )
            }

            if (repeatEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Nº de semanas:", color = TextSecondary, fontSize = 13.sp)
                    IconButton(onClick = { if (occurrences > 1) occurrences-- }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary)
                    }
                    Text(occurrences.toString(), color = White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (occurrences < 52) occurrences++ }) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = AccentPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(errorMessage ?: "", color = Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    lessonViewModel.generate(
                        teacherId = teacherId,
                        startDate = startDate,
                        recurrence = if (repeatEnabled) "WEEKLY" else "NONE",
                        occurrences = if (repeatEnabled) occurrences else 1
                    )
                },
                enabled = !isLoading && startDate.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (repeatEnabled) "Gravar e repetir $occurrences semanas" else "Gravar esta semana",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (lessons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "${lessons.size} aula(s) gravada(s) com sucesso",
                    color = StatusActive,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SavedLessonsSection(teacherId: Int) {
    val lessonViewModel = remember { AppModule.provideLessonViewModel() }

    val lessons by lessonViewModel.lessons.collectAsState()
    val isLoading by lessonViewModel.isLoading.collectAsState()
    val errorMessage by lessonViewModel.errorMessage.collectAsState()
    val successMessage by lessonViewModel.successMessage.collectAsState()

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
                        lessons.forEach { lesson ->
                            SavedLessonCard(
                                lesson = lesson,
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
                                                lessonViewModel.loadWeek(teacherId, selectedDate)
                                            }
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

@Composable
fun SavedLessonCard(
    lesson: com.example.frontend.data.remote.dto.LessonResponse,
    onCancelLesson: () -> Unit,
    onCancelSeries: () -> Unit
) {
    Surface(
        color = AccentPurple.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${lesson.date} | ${lesson.startTime} - ${lesson.endTime}",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Estado: ${lesson.status}",
                color = if (lesson.status == "CANCELLED") Red else StatusActive,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Teacher ID: ${lesson.teacherId}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Text(
                text = "Students: ${
                    if (lesson.students.isEmpty()) {
                        "sem alunos"
                    } else {
                        lesson.students.joinToString(", ") { "Aluno ${it.studentId}" }
                    }
                }",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Text(
                text = "Series ID: ${lesson.seriesId ?: "sem série"}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            if (lesson.status != "CANCELLED") {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCancelLesson,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar aula")
                    }

                    if (lesson.seriesId != null) {
                        Button(
                            onClick = onCancelSeries,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar série")
                        }
                    }
                }
            }
        }
    }
}

