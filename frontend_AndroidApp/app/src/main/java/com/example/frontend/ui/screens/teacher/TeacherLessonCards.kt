package com.example.frontend.ui.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.remote.dto.LessonResponse
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.Red
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.theme.appTextFieldColors

@Composable
fun SavedLessonCard(
    lesson: LessonResponse,
    studentsById: Map<Int, StudentEntity>,
    onCancelLesson: () -> Unit,
    onCancelSeries: () -> Unit,
    onMarkAttendance: (studentId: Int, attended: Boolean) -> Unit,
    onReschedule: (date: String, startTime: String, endTime: String) -> Unit
) {
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var showCancelLessonConfirm by remember { mutableStateOf(false) }
    var showCancelSeriesConfirm by remember { mutableStateOf(false) }

    Surface(
        color = AccentPurple.copy(alpha = 0.12f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
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
                text = "Series ID: ${lesson.seriesId ?: "sem série"}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (lesson.students.isEmpty()) {
                Text(text = "Sem alunos nesta aula", color = TextSecondary, fontSize = 12.sp)
            } else {
                Text(
                    text = "Presenças",
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    lesson.students.forEach { student ->
                        val studentName = studentsById[student.studentId]?.name
                            ?: "Aluno ${student.studentId}"
                        AttendanceRow(
                            studentName = studentName,
                            attended = student.attended,
                            enabled = lesson.status != "CANCELLED",
                            onMark = { attended -> onMarkAttendance(student.studentId, attended) }
                        )
                    }
                }
            }

            if (lesson.status != "CANCELLED") {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCancelLessonConfirm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar aula")
                    }

                    if (lesson.seriesId != null) {
                        Button(
                            onClick = { showCancelSeriesConfirm = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar série")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showRescheduleDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text("Remarcar aula")
                }
            }
        }
    }

    if (showRescheduleDialog) {
        RescheduleDialog(
            initialDate = lesson.date,
            initialStartTime = lesson.startTime,
            initialEndTime = lesson.endTime,
            onDismiss = { showRescheduleDialog = false },
            onConfirm = { date, startTime, endTime ->
                onReschedule(date, startTime, endTime)
                showRescheduleDialog = false
            }
        )
    }

    if (showCancelLessonConfirm) {
        ConfirmDialog(
            title = "Cancelar esta aula?",
            message = "Esta ação não pode ser desfeita. Os alunos vão receber um email a avisar do cancelamento.",
            confirmLabel = "Cancelar aula",
            onDismiss = { showCancelLessonConfirm = false },
            onConfirm = {
                onCancelLesson()
                showCancelLessonConfirm = false
            }
        )
    }

    if (showCancelSeriesConfirm) {
        ConfirmDialog(
            title = "Cancelar toda a série?",
            message = "Todas as aulas futuras desta série recorrente vão ser canceladas. Esta ação não pode ser desfeita. Os alunos vão receber um email a avisar.",
            confirmLabel = "Cancelar série",
            onDismiss = { showCancelSeriesConfirm = false },
            onConfirm = {
                onCancelSeries()
                showCancelSeriesConfirm = false
            }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message, color = TextSecondary, fontSize = 13.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) {
                Text("Voltar", color = White)
            }
        }
    )
}

@Composable
private fun AttendanceRow(
    studentName: String,
    attended: Boolean?,
    enabled: Boolean,
    onMark: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(studentName, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = when (attended) {
                    true -> "Presente"
                    false -> "Faltou"
                    null -> "Por marcar"
                },
                color = when (attended) {
                    true -> StatusActive
                    false -> Red
                    null -> TextSecondary
                },
                fontSize = 11.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = { onMark(true) },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (attended == true) StatusActive else StatusActive.copy(alpha = 0.25f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Presente", fontSize = 11.sp)
            }
            Button(
                onClick = { onMark(false) },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (attended == false) Red else Red.copy(alpha = 0.25f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Falta", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun RescheduleDialog(
    initialDate: String,
    initialStartTime: String,
    initialEndTime: String,
    onDismiss: () -> Unit,
    onConfirm: (date: String, startTime: String, endTime: String) -> Unit
) {
    var date by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        titleContentColor = White,
        textContentColor = White,
        title = { Text("Remarcar aula") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "O aluno recebe um email automático com a data/hora antiga e a nova.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data (AAAA-MM-DD)") },
                    singleLine = true,
                    colors = appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Início (HH:MM)") },
                    singleLine = true,
                    colors = appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Fim (HH:MM)") },
                    singleLine = true,
                    colors = appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(date, startTime, endTime) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) {
                Text("Cancelar", color = White)
            }
        }
    )
}