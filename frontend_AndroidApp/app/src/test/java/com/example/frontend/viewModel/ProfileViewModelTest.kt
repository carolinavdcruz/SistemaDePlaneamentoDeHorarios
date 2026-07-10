package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import com.example.frontend.ui.viewmodel.profile.ProfileUiState
import com.example.frontend.ui.viewmodel.profile.ProfileViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentRepository = mockk<StudentRepository>()
    private val teacherRepository = mockk<TeacherRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    @Test
    fun `loadProfile de aluno preenche nome email e professor`() = runTest {
        every { sessionManager.getUserId() } returns 10
        every { sessionManager.getUserRole() } returns OwnerType.STUDENT

        coEvery { studentRepository.getById(10) } returns StudentEntity(
            id = 10,
            teacherId = 2,
            name = "Aluno X",
            email = "aluno@isel.pt",
            password = "",
            maxDailySessions = 1
        )

        coEvery { teacherRepository.getById(2) } returns TeacherEntity(
            id = 2,
            name = "Prof Y",
            email = "prof@isel.pt",
            password = ""
        )

        val viewModel = ProfileViewModel(studentRepository, teacherRepository, sessionManager)
        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Aluno X", state.name)
        assertEquals("aluno@isel.pt", state.email)
        assertEquals(OwnerType.STUDENT, state.role)
        assertEquals("Prof Y", state.teacherName)
    }

    @Test
    fun `loadProfile de professor preenche nome e email`() = runTest {
        every { sessionManager.getUserId() } returns 3
        every { sessionManager.getUserRole() } returns OwnerType.TEACHER

        coEvery { teacherRepository.getById(3) } returns TeacherEntity(
            id = 3,
            name = "Prof Z",
            email = "z@isel.pt",
            password = ""
        )

        val viewModel = ProfileViewModel(studentRepository, teacherRepository, sessionManager)
        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Prof Z", state.name)
        assertEquals("z@isel.pt", state.email)
        assertEquals(OwnerType.TEACHER, state.role)
    }

    @Test
    fun `loadProfile sem sessao devolve erro`() = runTest {
        every { sessionManager.getUserId() } returns -1
        every { sessionManager.getUserRole() } returns null

        val viewModel = ProfileViewModel(studentRepository, teacherRepository, sessionManager)
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals(
            "Sessao nao encontrada. Por favor volta a fazer login.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `onLogoutClicked limpa sessao e ativa isLoggedOut`() = runTest {
        val viewModel = ProfileViewModel(studentRepository, teacherRepository, sessionManager)

        viewModel.onLogoutClicked()
        advanceUntilIdle()

        verify { sessionManager.clearSession() }
        assertTrue(viewModel.uiState.value.isLoggedOut)
    }

    @Test
    fun `onErrorDismissed limpa erro`() {
        val viewModel = ProfileViewModel(studentRepository, teacherRepository, sessionManager)

        val current = viewModel.uiState.value.copy(errorMessage = "erro")
        val field = ProfileViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ProfileUiState>
        stateFlow.value = current

        viewModel.onErrorDismissed()

        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onLogoutNavigated repoe isLoggedOut a false`() {
        val viewModel = ProfileViewModel(studentRepository, teacherRepository, sessionManager)

        val current = viewModel.uiState.value.copy(isLoggedOut = true)
        val field = ProfileViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ProfileUiState>
        stateFlow.value = current

        viewModel.onLogoutNavigated()

        assertEquals(false, viewModel.uiState.value.isLoggedOut)
    }
}