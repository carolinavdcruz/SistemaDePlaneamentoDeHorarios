package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.ui.viewmodel.student.StudentViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: StudentRepository = mockk(relaxed = true)

    @Test
    fun `loadStudents atualiza lista`() = runTest {
        val students = listOf(
            StudentEntity(
                id = 1,
                teacherId = 10,
                name = "Aluno A",
                email = "alunoa@isel.pt",
                password = "Student123",
                maxDailySessions = 1
            ),
            StudentEntity(
                id = 2,
                teacherId = 10,
                name = "Aluno B",
                email = "alunob@isel.pt",
                password = "Student123",
                maxDailySessions = 1
            )
        )

        coEvery { repository.getAll() } returns students

        val viewModel = StudentViewModel(repository)
        viewModel.loadStudents()
        advanceUntilIdle()

        assertEquals(2, viewModel.students.value.size)
        assertEquals("Aluno A", viewModel.students.value[0].name)
    }

    @Test
    fun `addStudent com nome vazio devolve erro`() = runTest {
        val viewModel = StudentViewModel(repository)

        viewModel.setEmail("aluno@isel.pt")
        viewModel.setPassword("Student123")
        viewModel.addStudent()

        assertEquals("O nome do aluno é obrigatorio.", viewModel.errorMessage.value)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `addStudent com email vazio devolve erro`() = runTest {
        val viewModel = StudentViewModel(repository)

        viewModel.setName("Aluno A")
        viewModel.setPassword("Student123")
        viewModel.addStudent()

        assertEquals("O email do aluno é obrigatorio.", viewModel.errorMessage.value)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `addStudent com password vazia devolve erro`() = runTest {
        val viewModel = StudentViewModel(repository)

        viewModel.setName("Aluno A")
        viewModel.setEmail("aluno@isel.pt")
        viewModel.addStudent()

        assertEquals("A password do aluno é obrigatória.", viewModel.errorMessage.value)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `addStudent valido insere aluno limpa form e recarrega lista`() = runTest {
        coEvery { repository.insert(any()) } returns 1
        coEvery { repository.getAll() } returns emptyList()

        val viewModel = StudentViewModel(repository)
        viewModel.setName("Aluno A")
        viewModel.setEmail("alunoa@isel.pt")
        viewModel.setPassword("Student123")

        viewModel.addStudent()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insert(
                match {
                    it.name == "Aluno A" &&
                            it.email == "alunoa@isel.pt" &&
                            it.password == "Student123" &&
                            it.maxDailySessions == 1
                }
            )
        }

        coVerify(atLeast = 1) { repository.getAll() }

        assertEquals("", viewModel.name.value)
        assertEquals("", viewModel.email.value)
        assertEquals("", viewModel.password.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `updateStudent chama repository e recarrega lista`() = runTest {
        val student = StudentEntity(
            id = 1,
            teacherId = 10,
            name = "Aluno A",
            email = "alunoa@isel.pt",
            password = "Student123",
            maxDailySessions = 1
        )

        coEvery { repository.getAll() } returns listOf(student)

        val viewModel = StudentViewModel(repository)
        viewModel.updateStudent(student)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.update(student) }
        coVerify(atLeast = 1) { repository.getAll() }
    }

    @Test
    fun `deleteStudent chama repository e recarrega lista`() = runTest {
        val student = StudentEntity(
            id = 1,
            teacherId = 10,
            name = "Aluno A",
            email = "alunoa@isel.pt",
            password = "Student123",
            maxDailySessions = 1
        )

        coEvery { repository.getAll() } returns emptyList()

        val viewModel = StudentViewModel(repository)
        viewModel.deleteStudent(student)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.delete(student) }
        coVerify(atLeast = 1) { repository.getAll() }
    }

    @Test
    fun `loadStudentsByTeacherId atualiza lista filtrada`() = runTest {
        val students = listOf(
            StudentEntity(
                id = 1,
                teacherId = 7,
                name = "Aluno A",
                email = "alunoa@isel.pt",
                password = "",
                maxDailySessions = 1
            )
        )

        coEvery { repository.getByTeacherId(7) } returns students

        val viewModel = StudentViewModel(repository)
        viewModel.loadStudentsByTeacherId(7)
        advanceUntilIdle()

        assertEquals(1, viewModel.students.value.size)
        assertEquals(7, viewModel.students.value.first().teacherId)
    }

    @Test
    fun `unassignTeacherFromStudent chama repository e recarrega alunos do professor`() = runTest {
        coEvery { repository.getByTeacherId(7) } returns emptyList()

        val viewModel = StudentViewModel(repository)
        viewModel.unassignTeacherFromStudent(studentId = 3, teacherId = 7)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.unassignTeacherFromStudent(3) }
        coVerify(exactly = 1) { repository.getByTeacherId(7) }
    }
}