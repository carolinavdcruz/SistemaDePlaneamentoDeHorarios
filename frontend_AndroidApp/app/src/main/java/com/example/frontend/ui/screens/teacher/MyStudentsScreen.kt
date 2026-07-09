package com.example.frontend.ui.screens.teacher

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.ui.screens.StudentDetailScreen
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.theme.lightRed

@Composable
fun MyStudentsScreen(
    teacherId: Int
) {

    val viewModel = remember { AppModule.provideStudentViewModel() }
    val students by viewModel.students.collectAsState()

    // Navegação simples dentro do próprio ecrã (sem rota no NavHost),
    // seguindo o mesmo padrão usado no resto da app (ex: ChooseTeacherScreen).
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }

    LaunchedEffect(teacherId) {
        viewModel.loadStudentsByTeacherId(teacherId)
    }

    if (selectedStudent != null) {
        StudentDetailScreen(
            studentName = selectedStudent!!.name,
            studentEmail = selectedStudent!!.email,
            studentId = selectedStudent!!.id,
            onBack = { selectedStudent = null }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "My Students",
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Students assigned to you",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            if (students.isEmpty()) {
                item {
                    Surface(
                        color = CardBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, InputBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "No students yet",
                                color = White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "When students choose you as their teacher, they will appear here.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(students) { student ->
                    StudentCard(
                        name = student.name,
                        email = student.email,
                        onUnassign = { viewModel.unassignTeacherFromStudent(student.id, teacherId) },
                        onClick = { selectedStudent = student }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun StudentCard(name: String, email: String, onUnassign: () -> Unit, onClick: () -> Unit = {}) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circular simples
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = AccentPurple.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(),
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(name, color = White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(email, color = TextSecondary, fontSize = 13.sp)
                }
            }

            IconButton(onClick = onUnassign) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove My Student",
                    tint = lightRed
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyStudentsScreenPreview() {
    MyStudentsScreen(teacherId = 1)
}