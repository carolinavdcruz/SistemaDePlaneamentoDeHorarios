package com.example.frontend.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.remote.api.GoogleCalendarManager
import com.example.frontend.ui.screens.schedule.ProposedScheduleCard
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.viewmodel.schedule.ScheduleUiState
import com.example.frontend.ui.viewmodel.schedule.ScheduleViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

// Button Create Schedule
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GenerateScheduleButton(teacherId: Int) {

    val context = LocalContext.current

    val viewModel: ScheduleViewModel = remember { AppModule.provideScheduleViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is ScheduleUiState.Loading

    val lessonViewModel = remember { AppModule.provideLessonViewModel() }
    val lessonIsLoading by lessonViewModel.isLoading.collectAsState()

    val studentViewModel = remember { AppModule.provideStudentViewModel() }

    val students by studentViewModel.students.collectAsState()

    val scope = rememberCoroutineScope()
    val calendarManager = remember { GoogleCalendarManager(context) }

    LaunchedEffect(teacherId) {
        studentViewModel.loadStudentsByTeacherId(teacherId)
    }

    val studentsById = remember(students) { students.associateBy { it.id } }

    var startDate by remember { mutableStateOf("") }
    var repeatEnabled by remember { mutableStateOf(false) }
    var occurrences by remember { mutableStateOf(4) }

    val acceptWithRealLessons = {
        if (startDate.isBlank()) {
            viewModel.setUiError("Indica a data de início antes de aceitar o horário.")
        } else {
            lessonViewModel.generate(
                teacherId = teacherId,
                startDate = startDate,
                recurrence = if (repeatEnabled) "WEEKLY" else "NONE",
                occurrences = if (repeatEnabled) occurrences else 1
            ) { generatedLessons ->

                val studentNames = studentsById.mapValues { it.value.name }

                scope.launch {
                    val result = calendarManager.addLessonsToCalendar(
                        lessons = generatedLessons,
                        studentNames = studentNames
                    )

                    if (result.isSuccess) {
                        viewModel.markAccepted()
                    } else {
                        viewModel.setUiError(
                            "As aulas foram gravadas, mas houve erro ao adicionar ao Google Calendar: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            }
        }
    }
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
                acceptWithRealLessons()
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

        Surface(
            color = CardBackground.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, InputBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configuração da aceitação",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Define a data de início e, se quiseres, repete o horário por várias semanas.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Data de início (AAAA-MM-DD)") },
                    placeholder = { Text("2026-07-13") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Repetir semanalmente",
                        color = White,
                        fontSize = 14.sp
                    )

                    Switch(
                        checked = repeatEnabled,
                        onCheckedChange = { repeatEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple
                        )
                    )
                }

                if (repeatEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Número de semanas",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { if (occurrences > 1) occurrences-- },
                                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-", color = White)
                            }

                            Text(
                                text = occurrences.toString(),
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { if (occurrences < 52) occurrences++ },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+", color = White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState is ScheduleUiState.Success) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(context)

                        when {
                            startDate.isBlank() -> {
                                viewModel.setUiError("Indica a data de início antes de aceitar o horário.")
                            }

                            account == null || !viewModel.hasCalendarPermission(context) -> {
                                signInLauncher.launch(viewModel.buildGoogleSignInIntent(context))
                            }

                            else -> {
                                acceptWithRealLessons()
                            }
                        }
                    },
                    enabled = uiState is ScheduleUiState.Success && !isLoading && !lessonIsLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (lessonIsLoading) {
                        CircularProgressIndicator(
                            color = White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Aceitar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

            }
            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(12.dp))
        }

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
