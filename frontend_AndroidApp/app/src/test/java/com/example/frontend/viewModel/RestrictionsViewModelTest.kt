package com.example.frontend.viewModel


import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.local.entity.RestrictionsEntity
import com.example.frontend.data.repository.RestrictionsRepository
import com.example.frontend.ui.viewmodel.teacher.RestrictionsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestrictionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<RestrictionsRepository>()

    @Test
    fun `loadRestrictions preenche valores quando existem`() = runTest {
        coEvery { repository.getByTeacherId(1) } returns RestrictionsEntity(
            teacherId = 1,
            maxDailyHours = 8,
            sessionDurationMinutes = 60,
            maxParticipantsPerSession = 3,
            maxSessionsPerStudentPerDay = 1
        )

        val viewModel = RestrictionsViewModel(repository)
        viewModel.loadRestrictions(1)
        advanceUntilIdle()

        assertEquals("8", viewModel.maxDailyHours.value)
        assertEquals("60", viewModel.sessionDurationMinutes.value)
        assertEquals("3", viewModel.maxParticipantsPerSession.value)
        assertEquals("1", viewModel.maxSessionsPerStudentPerDay.value)
    }

    @Test
    fun `saveRestrictions com valores validos ativa isSaved`() = runTest {
        coEvery { repository.getByTeacherId(1) } returns null
        coEvery { repository.insert(any()) } returns Unit

        val viewModel = RestrictionsViewModel(repository)
        viewModel.setMaxDailyHours("8")
        viewModel.setSessionDurationMinutes("60")
        viewModel.setMaxParticipantsPerSession("3")
        viewModel.setMaxSessionsPerStudentPerDay("1")

        viewModel.saveRestrictions(1)
        advanceUntilIdle()

        assertTrue(viewModel.isSaved.value)
        coVerify { repository.insert(any()) }
    }

    @Test
    fun `saveRestrictions com maxDailyHours invalido devolve erro`() = runTest {
        val viewModel = RestrictionsViewModel(repository)
        viewModel.setMaxDailyHours("")
        viewModel.setSessionDurationMinutes("60")
        viewModel.setMaxParticipantsPerSession("3")
        viewModel.setMaxSessionsPerStudentPerDay("1")

        viewModel.saveRestrictions(1)
        advanceUntilIdle()

        assertEquals("Max daily hours invalid.", viewModel.errorMessage.value)
    }
}