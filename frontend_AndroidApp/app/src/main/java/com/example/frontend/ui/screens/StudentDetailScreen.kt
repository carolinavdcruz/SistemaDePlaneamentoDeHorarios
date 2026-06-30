package com.example.frontend.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White

/** Ecrã de detalhe de um aluno, visto pelo professor (clicando na lista de "My Students"). */
@Composable
fun StudentDetailScreen(
    studentName: String,
    studentEmail: String,
    studentId: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Detalhe do aluno",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    color = AccentPurple.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentPurple)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(studentName, color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(studentEmail, color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AttendanceStatsCard(studentId = studentId)
        }
    }
}
