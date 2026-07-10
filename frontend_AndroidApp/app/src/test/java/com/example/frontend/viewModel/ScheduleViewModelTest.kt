package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.remote.api.ScheduleApi
import com.example.frontend.data.remote.dto.ScheduleSessionResponse
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.ui.viewmodel.schedule.ScheduleUiState
import com.example.frontend.ui.viewmodel.schedule.ScheduleViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduleApi = mockk<ScheduleApi>()
    private val studentRepository = mockk<StudentRepository>()

    @Test
    fun `generateSchedule com sessoes devolve Success`() = runTest {
        coEvery { scheduleApi.generateSchedule(1) } returns listOf(
            ScheduleSessionResponse(
                dayOfWeek = 1,
                startTime = "09:00",
                endTime = "10:00",
                studentIds = listOf(10, 11)
            )
        )

        coEvery { studentRepository.getByTeacherId(1) } returns listOf(
            StudentEntity(id = 10, teacherId = 1, name = "A", email = "a@isel.pt", password = "", maxDailySessions = 1),
            StudentEntity(id = 11, teacherId = 1, name = "B", email = "b@isel.pt", password = "", maxDailySessions = 1)
        )

        val viewModel = ScheduleViewModel(scheduleApi, studentRepository)
        viewModel.generateSchedule(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScheduleUiState.Success
        assertEquals(1, state.sessions.size)
        assertEquals("A", state.studentName[10])
        assertEquals("B", state.studentName[11])
    }

    @Test
    fun `generateSchedule sem sessoes devolve Empty`() = runTest {
        coEvery { scheduleApi.generateSchedule(1) } returns emptyList()
        coEvery { studentRepository.getByTeacherId(1) } returns emptyList()

        val viewModel = ScheduleViewModel(scheduleApi, studentRepository)
        viewModel.generateSchedule(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScheduleUiState.Empty
        assertEquals("Sem sobreposições de disponibilidade encontradas.", state.reason)
    }

    @Test
    fun `generateSchedule com erro devolve Error`() = runTest {
        coEvery { scheduleApi.generateSchedule(1) } throws RuntimeException("Falha API")

        val viewModel = ScheduleViewModel(scheduleApi, studentRepository)
        viewModel.generateSchedule(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScheduleUiState.Error
        assertEquals("Falha API", state.message)
    }

    @Test
    fun `markAccepted muda estado para Accepted`() {
        val viewModel = ScheduleViewModel(scheduleApi, studentRepository)

        viewModel.markAccepted()

        assertTrue(viewModel.uiState.value is ScheduleUiState.Accepted)
    }

    @Test
    fun `setUiError muda estado para Error`() {
        val viewModel = ScheduleViewModel(scheduleApi, studentRepository)

        viewModel.setUiError("Erro manual")

        val state = viewModel.uiState.value as ScheduleUiState.Error
        assertEquals("Erro manual", state.message)
    }
}