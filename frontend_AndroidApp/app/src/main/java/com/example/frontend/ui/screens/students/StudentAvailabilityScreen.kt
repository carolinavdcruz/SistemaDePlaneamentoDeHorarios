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
    availabilityViewModel: AvailabilityViewModel = remember {
        AppModule.provideAvailabilityViewModel()
    }
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Disponibilidade",
            color = White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Define a tua disponibilidade semanal",
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
                    text = "Disponibilidade Semanal",
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