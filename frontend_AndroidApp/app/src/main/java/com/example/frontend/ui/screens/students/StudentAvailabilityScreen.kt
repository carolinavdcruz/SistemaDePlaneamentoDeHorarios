package com.example.frontend.ui.screens.students

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.model.OwnerType
import com.example.frontend.ui.screens.AvailabilitySelector
import com.example.frontend.ui.screens.MobileParameterInput
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.viewmodel.availability.AvailabilityViewModel
import com.example.frontend.ui.viewmodel.student.StudentRestrictionsViewModel

@Composable
fun StudentAvailabilityScreen(
    studentId: Int,
    restrictionsViewModel: StudentRestrictionsViewModel = remember {
        AppModule.provideStudentRestrictionsViewModel()
    },
    availabilityViewModel: AvailabilityViewModel = remember {
        AppModule.provideAvailabilityViewModel()
    }
) {
    val weeklyHours by restrictionsViewModel.weeklyHours.collectAsState()
    val isLoading by restrictionsViewModel.isLoading.collectAsState()
    val isSaved by restrictionsViewModel.isSaved.collectAsState()
    val errorMessage by restrictionsViewModel.errorMessage.collectAsState()

    LaunchedEffect(studentId) {
        restrictionsViewModel.loadRestrictions(studentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Availability & Preferences",
            color = White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Define your weekly availability and your preferred weekly workload",
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
                    text = "Student Preference",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                MobileParameterInput(
                    label = "Desired Hours per Week",
                    value = weeklyHours,
                    onValueChange = restrictionsViewModel::setWeeklyHours,
                    placeholder = "3"
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
                        text = "Preference saved successfully.",
                        color = White,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { restrictionsViewModel.saveRestrictions(studentId) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLoading) "Saving..." else "Save Preference"
                    )
                }
            }
        }

        HorizontalDivider(
            color = InputBorder,
            thickness = 0.5.dp
        )

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
                    ownerId = studentId,
                    ownerType = OwnerType.STUDENT,
                    viewModel = availabilityViewModel
                )
            }
        }
    }
}