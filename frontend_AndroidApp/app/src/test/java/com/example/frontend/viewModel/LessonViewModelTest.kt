package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.remote.api.LessonApi
import com.example.frontend.data.remote.dto.AttendanceSummaryResponse
import com.example.frontend.data.remote.dto.CancelSeriesResponse
import com.example.frontend.data.remote.dto.LessonResponse
import com.example.frontend.data.remote.dto.LessonStudentResponse
import com.example.frontend.ui.viewmodel.schedule.LessonViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonApi = mockk<LessonApi>()

    private fun lesson(id: Int) = LessonResponse(
        id = id,
        teacherId = 1,
        seriesId = null,
        date = "2026-07-13",
        startTime = "09:00",
        endTime = "10:00",
        status = "SCHEDULED",
        students = listOf(LessonStudentResponse(studentId = 10, attended = null, attendedAt = null))
    )

    @Test
    fun `generate atualiza lessons e successMessage`() = runTest {
        coEvery { lessonApi.generate(any()) } returns listOf(lesson(1), lesson(2))

        val viewModel = LessonViewModel(lessonApi)
        var callbackCalled = false

        viewModel.generate(
            teacherId = 1,
            startDate = "2026-07-13",
            recurrence = "WEEKLY",
            occurrences = 2
        ) {
            callbackCalled = true
        }

        advanceUntilIdle()

        assertEquals(2, viewModel.lessons.value.size)
        assertEquals("2 aula(s) gerada(s) com sucesso.", viewModel.successMessage.value)
        assertTrue(callbackCalled)
    }

    @Test
    fun `loadWeek carrega aulas da semana`() = runTest {
        coEvery { lessonApi.getWeek(1, "2026-07-13") } returns listOf(lesson(1))

        val viewModel = LessonViewModel(lessonApi)
        viewModel.loadWeek(1, "2026-07-13")
        advanceUntilIdle()

        assertEquals(1, viewModel.lessons.value.size)
        assertEquals(1, viewModel.lessons.value.first().id)
    }

    @Test
    fun `cancelSeries atualiza successMessage`() = runTest {
        coEvery { lessonApi.cancelSeries("abc") } returns CancelSeriesResponse(cancelledCount = 4)

        val viewModel = LessonViewModel(lessonApi)
        var callbackCalled = false

        viewModel.cancelSeries("abc") {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertEquals("4 aula(s) cancelada(s) na série.", viewModel.successMessage.value)
        assertTrue(callbackCalled)
    }

    @Test
    fun `markAttendance chama api e atualiza mensagem`() = runTest {
        coEvery { lessonApi.markAttendance(1, 10, true) } returns Unit

        val viewModel = LessonViewModel(lessonApi)
        var callbackCalled = false

        viewModel.markAttendance(1, 10, true) {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertEquals("Presença atualizada com sucesso.", viewModel.successMessage.value)
        assertTrue(callbackCalled)
        coVerify { lessonApi.markAttendance(1, 10, true) }
    }

    @Test
    fun `loadAttendanceSummary atualiza estado`() = runTest {
        coEvery { lessonApi.getAttendanceSummary(10) } returns AttendanceSummaryResponse(
            studentId = 10,
            totalLessons = 5,
            attended = 4,
            missed = 1,
            pending = 0,
            attendanceRate = 0.8
        )

        val viewModel = LessonViewModel(lessonApi)
        viewModel.loadAttendanceSummary(10)
        advanceUntilIdle()

        assertEquals(10, viewModel.attendanceSummary.value?.studentId)
        assertEquals(5, viewModel.attendanceSummary.value?.totalLessons)
    }

    @Test
    fun `clearMessages limpa erro e sucesso`() = runTest {
        coEvery { lessonApi.getWeek(1, "2026-07-13") } throws RuntimeException("erro")

        val viewModel = LessonViewModel(lessonApi)
        viewModel.loadWeek(1, "2026-07-13")
        advanceUntilIdle()

        assertTrue(viewModel.errorMessage.value != null)

        viewModel.clearMessages()

        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `clearSelectedLesson limpa selectedLesson`() = runTest {
        coEvery { lessonApi.getById(1) } returns lesson(1)

        val viewModel = LessonViewModel(lessonApi)
        viewModel.loadLessonById(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.selectedLesson.value?.id)

        viewModel.clearSelectedLesson()

        assertNull(viewModel.selectedLesson.value)
    }
}