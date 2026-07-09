package com.example.frontend.ui.screens.students

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.data.remote.dto.LessonResponse
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Red
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.White

@Composable
fun StudentLessonRow(lesson: LessonResponse) {
    Surface(
        color = AccentPurple.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${lesson.date} | ${lesson.startTime} - ${lesson.endTime}",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (lesson.status == "CANCELLED") "Cancelada" else "Confirmada",
                color = if (lesson.status == "CANCELLED") Red else StatusActive,
                fontSize = 12.sp
            )
        }
    }
}
