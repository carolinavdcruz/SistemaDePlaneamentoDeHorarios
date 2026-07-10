package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import com.example.frontend.ui.viewmodel.login.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teacherRepository = mockk<TeacherRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    @Test
    fun `validateLogin falha quando email esta vazio`() {
        val viewModel = LoginViewModel(teacherRepository, sessionManager)

        viewModel.setEmail("")
        viewModel.setPassword("1234")

        val result = viewModel.validateLogin()

        assertFalse(result)
        assertEquals("O email é obrigatório.", viewModel.errorMessage.value)
    }

    @Test
    fun `validateLogin falha quando password esta vazia`() {
        val viewModel = LoginViewModel(teacherRepository, sessionManager)

        viewModel.setEmail("teste@isel.pt")
        viewModel.setPassword("")

        val result = viewModel.validateLogin()

        assertFalse(result)
        assertEquals("A password é obrigatória.", viewModel.errorMessage.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `login com sucesso guarda sessao e ativa loginSuccess`() = runTest {
        coEvery {
            teacherRepository.login("teste@isel.pt", "Teacher123")
        } returns (7 to OwnerType.TEACHER)

        val viewModel = LoginViewModel(teacherRepository, sessionManager)
        viewModel.setEmail("teste@isel.pt")
        viewModel.setPassword("Teacher123")

        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.loginSuccess.value)
        assertEquals(null, viewModel.errorMessage.value)
        verify { sessionManager.saveSession(7, OwnerType.TEACHER) }
    }

    @Test
    fun `login invalido mostra mensagem de erro`() = runTest {
        coEvery {
            teacherRepository.login("teste@isel.pt", "errada")
        } returns null

        val viewModel = LoginViewModel(teacherRepository, sessionManager)
        viewModel.setEmail("teste@isel.pt")
        viewModel.setPassword("errada")

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.loginSuccess.value)
        assertEquals("Email ou password incorretos.", viewModel.errorMessage.value)
    }

    @Test
    fun `togglePasswordVisibility alterna estado`() {
        val viewModel = LoginViewModel(teacherRepository, sessionManager)

        assertFalse(viewModel.isPasswordVisible.value)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.isPasswordVisible.value)
        viewModel.togglePasswordVisibility()
        assertFalse(viewModel.isPasswordVisible.value)
    }
}