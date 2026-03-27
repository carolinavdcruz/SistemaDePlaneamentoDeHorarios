package com.example.frontend.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary

@Composable
fun ListScreen() {
    val context = LocalContext.current
    val viewModel = remember { AppModule.provideStudentViewModel(context) }
    val students by viewModel.students.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadStudents()
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
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp) // Espaço para não cobrir o form
        ) {
            item {
                Text(
                    text = "My Students",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Manage your student database and contact info",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // LISTA DE ALUNOS
            items(students) { student ->
                StudentCard(
                    name = student.name,
                    email = student.email,
                    onDelete = { viewModel.deleteStudent(student) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // FORMULÁRIO DE ADIÇÃO (Dentro do Scroll ou fixo, aqui está no scroll)
            item {
                Surface(
                    color = CardBackground,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, InputBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, "", tint = AccentPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New Student", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reutilizando seu padrão de input estilizado
                        LoginInputField(
                            label = "Student Name",
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "John Doe",
                            leadingIcon = Icons.Default.Person
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LoginInputField(
                            label = "Student Email",
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "john@example.com",
                            leadingIcon = Icons.Default.Email
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (name.isNotBlank() && email.isNotBlank()) {
                                    viewModel.addStudent(name, email)
                                    name = ""
                                    email = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Register Student", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(name: String, email: String, onDelete: () -> Unit) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth()
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
                    Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(email, color = TextSecondary, fontSize = 13.sp)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF5350) // Vermelho suave para deletar
                )
            }
        }
    }
}