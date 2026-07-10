package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import com.example.frontend.ui.viewmodel.register.RegisterViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentRepository = mockk<StudentRepository>()
    private val teacherRepository = mockk<TeacherRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    @Test
    fun `register aluno com sucesso guarda sessao`() = runTest {
        coEvery { studentRepository.insert(any()) } returns 11

        val viewModel = RegisterViewModel(studentRepository, teacherRepository, sessionManager)
        viewModel.setName("Maria")
        viewModel.setEmail("maria@isel.pt")
        viewModel.setPassword("Student123")
        viewModel.setSelectedRole(OwnerType.STUDENT)

        viewModel.register()
        advanceUntilIdle()

        assertTrue(viewModel.registerSuccess.value)
        verify { sessionManager.saveSession(11, OwnerType.STUDENT) }
    }

    @Test
    fun `register professor com sucesso guarda sessao`() = runTest {
        coEvery { teacherRepository.insert(any()) } returns 5

        val viewModel = RegisterViewModel(studentRepository, teacherRepository, sessionManager)
        viewModel.setName("Prof")
        viewModel.setEmail("prof@isel.pt")
        viewModel.setPassword("Teacher123")
        viewModel.setSelectedRole(OwnerType.TEACHER)

        viewModel.register()
        advanceUntilIdle()

        assertTrue(viewModel.registerSuccess.value)
        verify { sessionManager.saveSession(5, OwnerType.TEACHER) }
    }

    @Test
    fun `register invalido nao chama repositories`() = runTest {
        val viewModel = RegisterViewModel(studentRepository, teacherRepository, sessionManager)
        viewModel.setName("")
        viewModel.setEmail("")
        viewModel.setPassword("")

        viewModel.register()
        advanceUntilIdle()

        assertFalse(viewModel.registerSuccess.value)
        assertTrue(viewModel.errorMessage.value != null)
    }

    @Test
    fun `togglePasswordVisibility alterna estado`() {
        val viewModel = RegisterViewModel(studentRepository, teacherRepository, sessionManager)

        assertFalse(viewModel.isPasswordVisible.value)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.isPasswordVisible.value)
    }
}