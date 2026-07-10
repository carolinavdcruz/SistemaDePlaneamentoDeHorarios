package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.remote.dto.StudentRestrictionsResponse
import com.example.frontend.data.repository.StudentRestrictionsRepository
import com.example.frontend.ui.viewmodel.student.StudentRestrictionsViewModel
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
class StudentRestrictionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<StudentRestrictionsRepository>()

    @Test
    fun `loadRestrictions preenche horas semanais quando existem`() = runTest {
        coEvery { repository.getByStudentId(1) } returns StudentRestrictionsResponse(
            studentId = 1,
            weeklyHours = 4
        )

        val viewModel = StudentRestrictionsViewModel(repository)
        viewModel.loadRestrictions(1)
        advanceUntilIdle()

        assertEquals("4", viewModel.weeklyHours.value)
    }

    @Test
    fun `loadRestrictions usa valor por defeito quando nao existem`() = runTest {
        coEvery { repository.getByStudentId(1) } returns null

        val viewModel = StudentRestrictionsViewModel(repository)
        viewModel.loadRestrictions(1)
        advanceUntilIdle()

        assertEquals("3", viewModel.weeklyHours.value)
    }

    @Test
    fun `setWeeklyHours aceita apenas digitos`() {
        val viewModel = StudentRestrictionsViewModel(repository)

        viewModel.setWeeklyHours("5")
        assertEquals("5", viewModel.weeklyHours.value)

        viewModel.setWeeklyHours("abc")
        assertEquals("5", viewModel.weeklyHours.value)
    }

    @Test
    fun `saveRestrictions com valor valido ativa isSaved`() = runTest {
        coEvery { repository.save(1, 6) } returns Unit

        val viewModel = StudentRestrictionsViewModel(repository)
        viewModel.setWeeklyHours("6")

        viewModel.saveRestrictions(1)
        advanceUntilIdle()

        assertTrue(viewModel.isSaved.value)
        coVerify { repository.save(1, 6) }
    }

    @Test
    fun `saveRestrictions com valor invalido devolve erro`() = runTest {
        val viewModel = StudentRestrictionsViewModel(repository)
        viewModel.setWeeklyHours("0")

        viewModel.saveRestrictions(1)
        advanceUntilIdle()

        assertEquals("Desired weekly hours invalid.", viewModel.errorMessage.value)
    }
}