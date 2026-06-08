package com.example.frontend.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.AppModule
import com.example.frontend.data.model.OwnerType
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.theme.lightOrange
import com.example.frontend.ui.viewmodel.availability.AvailabilityViewModel
import com.example.frontend.ui.viewmodel.availability.TimeRangeInput

@Composable
fun AvailabilitySelector(
    ownerId: Int,
    ownerType: OwnerType,
    viewModel: AvailabilityViewModel = remember { AppModule.provideAvailabilityViewModel() }
) {

    val context = LocalContext.current

    LaunchedEffect(ownerId, ownerType) {
        viewModel.load(ownerId, ownerType)
    }

    val dayAvailabilities by viewModel.dayAvailabilities.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Weekly Availability",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "Add one or more time blocks for each day",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dayAvailabilities.forEach { dayAvailability ->
                DayAvailabilityCard(
                    day = dayAvailability.day,
                    ranges = dayAvailability.ranges,
                    onAddRange = { viewModel.addRange(dayAvailability.day) },
                    onRemoveRange = { rangeId ->
                        viewModel.removeRange(dayAvailability.day, rangeId)
                    },
                    onStartTimeClick = { rangeId, currentTime ->
                        showTimePicker(context, currentTime) { newTime ->
                            viewModel.setStartTime(dayAvailability.day, rangeId, newTime)
                        }
                    },
                    onEndTimeClick = { rangeId, currentTime ->
                        showTimePicker(context, currentTime) { newTime ->
                            viewModel.setEndTime(dayAvailability.day, rangeId, newTime)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = { viewModel.saveAvailability(ownerId, ownerType) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Text(
                "Guardar Disponibilidade",
                color = White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.clear(ownerId, ownerType) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, InputBorder)
        ) {
            Text(
                "Limpar",
                color = White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DayAvailabilityCard(
    day: String,
    ranges: List<TimeRangeInput>,
    onAddRange: () -> Unit,
    onRemoveRange: (String) -> Unit,
    onStartTimeClick: (String, String) -> Unit,
    onEndTimeClick: (String, String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AccentPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = day,
                        color = AccentPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onAddRange,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple.copy(alpha = 0.14f),
                        contentColor = AccentPurple
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Add interval", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (ranges.isEmpty()) {
                Surface(
                    color = Background.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, InputBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No time blocks yet",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ranges.forEach { range ->
                        TimeRangeCard(
                            startTime = range.startTime,
                            endTime = range.endTime,
                            onRemove = { onRemoveRange(range.id) },
                            onStartTimeClick = { onStartTimeClick(range.id, range.startTime) },
                            onEndTimeClick = { onEndTimeClick(range.id, range.endTime) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeCard(
    startTime: String,
    endTime: String,
    onRemove: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Background.copy(alpha = 0.72f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time block",
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove interval",
                        tint = lightOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeBox(
                    label = "Start",
                    time = startTime,
                    modifier = Modifier.weight(1f),
                    onClick = onStartTimeClick
                )

                Text(
                    text = "→",
                    color = TextSecondary,
                    fontSize = 16.sp
                )

                TimeBox(
                    label = "End",
                    time = endTime,
                    modifier = Modifier.weight(1f),
                    onClick = onEndTimeClick
                )
            }
        }
    }
}

@Composable
fun TimeBox(
    label: String,
    time: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, InputBorder),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun showTimePicker(
    context: Context,
    currentTime: String,
    onTimeSelected: (String) -> Unit
) {
    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
        },
        hour,
        minute,
        true
    ).show()
}
