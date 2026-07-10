package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import com.example.frontend.ui.viewmodel.student.ChooseTeacherViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChooseTeacherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teacherRepository = mockk<TeacherRepository>()
    private val studentRepository = mockk<StudentRepository>()
    private val sessionManager = mockk<SessionManager>()

    @Test
    fun `loadTeachers carrega professores e professor atual do aluno`() = runTest {
        val teachers = listOf(
            TeacherEntity(1, "Prof A", "a@isel.pt", ""),
            TeacherEntity(2, "Prof B", "b@isel.pt", "")
        )

        coEvery { teacherRepository.getAll() } returns teachers
        every { sessionManager.getUserId() } returns 10
        every { sessionManager.getUserRole() } returns OwnerType.STUDENT
        coEvery { studentRepository.getById(10) } returns StudentEntity(
            id = 10,
            name = "Aluno",
            email = "aluno@isel.pt",
            password = "",
            teacherId = 2,
            maxDailySessions = 1
        )

        val viewModel = ChooseTeacherViewModel(teacherRepository, studentRepository, sessionManager)
        viewModel.loadTeachers()
        advanceUntilIdle()

        assertEquals(2, viewModel.teachers.value.size)
        assertEquals(2, viewModel.currentTeacherId.value)
        assertEquals("Prof B", viewModel.currentTeacherName.value)
    }

    @Test
    fun `assignTeacherToStudent associa professor e ativa sucesso`() = runTest {
        every { sessionManager.getUserId() } returns 10
        every { sessionManager.getUserRole() } returns OwnerType.STUDENT
        coEvery { studentRepository.assignTeacherToStudent(10, 1) } returns Unit

        val viewModel = ChooseTeacherViewModel(teacherRepository, studentRepository, sessionManager)
        viewModel.onTeacherSelected(1)
        viewModel.assignTeacherToStudent()
        advanceUntilIdle()

        assertTrue(viewModel.assignSuccess.value)
        assertEquals(1, viewModel.currentTeacherId.value)
        coVerify { studentRepository.assignTeacherToStudent(10, 1) }
    }

    @Test
    fun `assignTeacherToStudent falha sem professor selecionado`() = runTest {
        every { sessionManager.getUserId() } returns 10
        every { sessionManager.getUserRole() } returns OwnerType.STUDENT

        val viewModel = ChooseTeacherViewModel(teacherRepository, studentRepository, sessionManager)
        viewModel.assignTeacherToStudent()
        advanceUntilIdle()

        assertEquals("Por favor selecione um professor.", viewModel.errorMessage.value)
    }
}