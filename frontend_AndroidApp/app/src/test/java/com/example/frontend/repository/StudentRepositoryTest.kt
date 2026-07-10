package com.example.frontend.repository

import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.remote.api.StudentApi
import com.example.frontend.data.remote.dto.StudentResponse
import com.example.frontend.data.repository.StudentRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class StudentRepositoryTest {

    private lateinit var dao: StudentDao
    private lateinit var api: StudentApi
    private lateinit var repository: StudentRepository

    @Before
    fun setup() {
        clearAllMocks()
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = StudentRepository(dao, api)
    }

    @Test
    fun `insert envia para api guarda localmente e devolve id`() = runTest {
        val student = StudentEntity(
            id = 0,
            teacherId = 2,
            name = "Aluno A",
            email = "alunoa@isel.pt",
            password = "Student123",
            maxDailySessions = 1
        )

        coEvery { api.register(any()) } returns StudentResponse(
            id = 10,
            name = "Aluno A",
            email = "alunoa@isel.pt",
            teacherId = 2
        )

        val result = repository.insert(student)

        assertEquals(10, result)

        coVerify(exactly = 1) { api.register(any()) }
        coVerify(exactly = 1) {
            dao.insert(
                match {
                    it.id == 10 &&
                            it.teacherId == 2 &&
                            it.name == "Aluno A" &&
                            it.email == "alunoa@isel.pt" &&
                            it.password == "Student123"
                }
            )
        }
    }

    @Test
    fun `getAll devolve dao getAll`() = runTest {

        val local = listOf(
            StudentEntity(1, 2, "Aluno A", "a@isel.pt", "", 1),
            StudentEntity(2, 2, "Aluno B", "b@isel.pt", "", 1)
        )

        coEvery { dao.getAll() } returns local

        val result = repository.getAll()

        assertEquals(2, result.size)
        assertEquals("Aluno A", result[0].name)
    }

    @Test
    fun `getById devolve local quando existe`() = runTest {

        val local = StudentEntity(
            id = 5,
            teacherId = 1,
            name = "Aluno Local",
            email = "local@isel.pt",
            password = "Student123",
            maxDailySessions = 1
        )

        coEvery { dao.getById(5) } returns local

        val result = repository.getById(5)

        val nonNullResult = requireNotNull(result)
        assertEquals(5, nonNullResult.id)
        assertEquals("Aluno Local", nonNullResult.name)

        coVerify(exactly = 0) { api.getAll() }
    }

    @Test
    fun `getById vai a api quando nao existe localmente`() = runTest {

        coEvery { dao.getById(5) } returns null
        coEvery { api.getAll() } returns listOf(
            StudentResponse(
                id = 5,
                name = "Aluno API",
                email = "api@isel.pt",
                teacherId = 3
            )
        )

        val result = repository.getById(5)

        val nonNullResult = requireNotNull(result)
        assertEquals(5, nonNullResult.id)
        assertEquals("Aluno API", nonNullResult.name)
        assertEquals(3, nonNullResult.teacherId)

        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `getById devolve null quando api nao encontra aluno`() = runTest {

        coEvery { dao.getById(99) } returns null
        coEvery { api.getAll() } returns emptyList()

        val result = repository.getById(99)

        assertNull(result)
    }

    @Test
    fun `getByEmail devolve dao getByEmail`() = runTest {

        val local = StudentEntity(
            id = 3,
            teacherId = null,
            name = "Aluno Email",
            email = "email@isel.pt",
            password = "",
            maxDailySessions = 1
        )

        coEvery { dao.getByEmail("email@isel.pt") } returns local

        val result = repository.getByEmail("email@isel.pt")

        val nonNullResult = requireNotNull(result)
        assertEquals(3, nonNullResult.id)
        assertEquals("Aluno Email", nonNullResult.name)
    }

    @Test
    fun `getByTeacherId sincroniza remoto e atualiza cache`() = runTest {

        coEvery { api.getStudentsByTeacher(7) } returns listOf(
            StudentResponse(1, "Aluno A", "a@isel.pt", 7),
            StudentResponse(2, "Aluno B", "b@isel.pt", 7)
        )

        val result = repository.getByTeacherId(7)

        assertEquals(2, result.size)
        assertEquals(7, result[0].teacherId)

        coVerify(exactly = 1) { dao.deleteByTeacherId(7) }
        coVerify(exactly = 2) { dao.insert(any()) }
    }

    /*
    @Test
    fun `getByTeacherId usa fallback local quando api falha`() = runTest {

        val cached = listOf(
            StudentEntity(1, 7, "Aluno Cache", "cache@isel.pt", "", 1)
        )

        coEvery { api.getStudentsByTeacher(7) } throws RuntimeException("Falha API")
        coEvery { dao.getByTeacherId(7) } returns cached

        val result = repository.getByTeacherId(7)

        assertEquals(1, result.size)
        assertEquals("Aluno Cache", result[0].name)
    }
    */

    @Test
    fun `assignTeacherToStudent chama api e dao`() = runTest {

        repository.assignTeacherToStudent(3, 8)

        coVerify(exactly = 1) { api.assignTeacher(3, 8) }
        coVerify(exactly = 1) { dao.assignTeacherToStudent(3, 8) }
    }

    @Test
    fun `unassignTeacherFromStudent chama api e dao`() = runTest {

        repository.unassignTeacherFromStudent(3)

        coVerify(exactly = 1) { api.unassignTeacher(3) }
        coVerify(exactly = 1) { dao.unassignTeacherFromStudent(3) }
    }
}