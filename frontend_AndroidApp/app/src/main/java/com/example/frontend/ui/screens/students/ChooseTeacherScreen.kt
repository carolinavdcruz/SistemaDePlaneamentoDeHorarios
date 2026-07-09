package com.example.frontend.ui.screens.students

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.viewmodel.student.ChooseTeacherViewModel

@Composable
fun ChooseTeacherScreen(
    viewModel: ChooseTeacherViewModel = remember { AppModule.provideChooseTeacherViewModel() },
    onTeacherAssigned: () -> Unit
) {
    val teachers by viewModel.teachers.collectAsState()
    val selectedTeacherId by viewModel.selectedTeacherId.collectAsState()
    val currentTeacherId by viewModel.currentTeacherId.collectAsState()
    val currentTeacherName by viewModel.currentTeacherName.collectAsState()
    val isChangingTeacher by viewModel.isChangingTeacher.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val assignSuccess by viewModel.assignSuccess.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTeachers()
    }

    LaunchedEffect(assignSuccess) {
        if (assignSuccess) {
            viewModel.onAssignSuccessNavigated()
            onTeacherAssigned()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        if (isLoading && teachers.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AccentPurple
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Choose Your Teacher",
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (currentTeacherId == null) {
                        "Select one teacher to work with"
                    } else if (isChangingTeacher) {
                        "Choose a new teacher to replace your current one"
                    } else {
                        "You already have a teacher assigned"
                    },
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (currentTeacherId != null && !isChangingTeacher) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CardBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AccentPurple)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Current teacher",
                                color = AccentPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentTeacherName ?: "Professor atribuido",
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(teachers) { teacher ->
                        TeacherChoiceCard(
                            teacher = teacher,
                            isSelected = selectedTeacherId == teacher.id,
                            enabled = currentTeacherId == null || isChangingTeacher,
                            onClick = {
                                if (currentTeacherId == null || isChangingTeacher) {
                                    viewModel.onTeacherSelected(teacher.id)
                                }
                            }
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (currentTeacherId != null && !isChangingTeacher) {
                    Button(
                        onClick = { viewModel.enableChangeMode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Trocar Professor", color = White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column {
                        if (isChangingTeacher) {
                            Button(
                                onClick = { viewModel.cancelChangeMode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, InputBorder)
                            ) {
                                Text("Cancelar", color = White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = { viewModel.assignTeacherToStudent() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isChangingTeacher) "Confirmar Novo Professor" else "Confirm Teacher",
                                color = White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherChoiceCard(
    teacher: TeacherEntity,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        color = CardBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AccentPurple else InputBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = if (isSelected) AccentPurple else TextSecondary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.name,
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = teacher.email,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = if (enabled) onClick else null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseTeacherScreenPreview() {
    ChooseTeacherScreen(
        onTeacherAssigned = {}
    )
}