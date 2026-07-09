package com.example.frontend.ui.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.frontend.AppModule
import com.example.frontend.data.model.OwnerType
import com.example.frontend.ui.screens.AvailabilitySelector
import com.example.frontend.ui.screens.MobileParameterInput
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.viewmodel.teacher.RestrictionsViewModel

@Composable
fun TeacherAvailabilityScreen(
    teacherId: Int,
    restrictionsViewModel: RestrictionsViewModel = remember {
        AppModule.provideRestrictionsViewModel()
    }
){

    val maxDailyHours by restrictionsViewModel.maxDailyHours.collectAsState()
    val sessionDurationMinutes by restrictionsViewModel.sessionDurationMinutes.collectAsState()
    val maxParticipantsPerSession by restrictionsViewModel.maxParticipantsPerSession.collectAsState()
    val maxSessionsPerStudentPerDay by restrictionsViewModel.maxSessionsPerStudentPerDay.collectAsState()
    val isLoading by restrictionsViewModel.isLoading.collectAsState()
    val isSaved by restrictionsViewModel.isSaved.collectAsState()
    val errorMessage by restrictionsViewModel.errorMessage.collectAsState()


    LaunchedEffect(teacherId) {
        restrictionsViewModel.loadRestrictions(teacherId)
    }

    /*
    LaunchedEffect(isSaved) {
        if (isSaved) {
            restrictionsViewModel.onSaveHandled()
        }
    }
     */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Availability & Restrictions",
            color = White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Define your weekly availability and scheduling rules",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, InputBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Scheduling Rules",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                MobileParameterInput(
                    label = "Max Daily Hours",
                    value = maxDailyHours,
                    onValueChange = restrictionsViewModel::setMaxDailyHours,
                    placeholder = "8"
                )

                Spacer(modifier = Modifier.height(12.dp))

                MobileParameterInput(
                    label = "Session Duration (min)",
                    value = sessionDurationMinutes,
                    onValueChange = restrictionsViewModel::setSessionDurationMinutes,
                    placeholder = "60"
                )

                Spacer(modifier = Modifier.height(12.dp))

                MobileParameterInput(
                    label = "Max Students per Session",
                    value = maxParticipantsPerSession,
                    onValueChange = restrictionsViewModel::setMaxParticipantsPerSession,
                    placeholder = "3"
                )

                Spacer(modifier = Modifier.height(12.dp))

                MobileParameterInput(
                    label = "Max Sessions per Student / Day",
                    value = maxSessionsPerStudentPerDay,
                    onValueChange = restrictionsViewModel::setMaxSessionsPerStudentPerDay,
                    placeholder = "1"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                if (isSaved) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Parameters saved successfully.",
                        color = AccentPurple,
                        fontSize = 13.sp
                    )
                }
            }
        }

        HorizontalDivider(color = InputBorder, thickness = 0.5.dp)

        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, InputBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Your Availability",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                AvailabilitySelector(
                    ownerId = teacherId,
                    ownerType = OwnerType.TEACHER
                )
            }
        }

        Button(
            onClick = { restrictionsViewModel.saveRestrictions(teacherId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            Icon(Icons.Default.Done, contentDescription = null)
            Spacer(modifier = Modifier.height(0.dp))
            Text(
                text = if (isLoading) "Saving..." else "Save Parameters",
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
    }

}