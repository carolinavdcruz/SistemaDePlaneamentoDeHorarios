package com.example.frontend.repository

import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.remote.api.TeacherApi
import com.example.frontend.data.remote.dto.LoginResponse
import com.example.frontend.data.remote.dto.TeacherResponse
import com.example.frontend.data.repository.TeacherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class TeacherRepositoryTest {

    private val dao: TeacherDao = mockk(relaxed = true)
    private val api: TeacherApi = mockk(relaxed = true)

    @Test
    fun `insert envia para api guarda localmente e devolve id`() = runTest {
        val repository = TeacherRepository(dao, api)

        val teacher = TeacherEntity(
            id = 0,
            name = "Prof. Ana",
            email = "ana@isel.pt",
            password = "Teacher123"
        )

        coEvery {
            api.register(any())
        } returns TeacherResponse(
            id = 7,
            name = "Prof. Ana",
            email = "ana@isel.pt"
        )

        val result = repository.insert(teacher)

        assertEquals(7, result)

        coVerify(exactly = 1) { api.register(any()) }
        coVerify(exactly = 1) {
            dao.insert(
                match {
                    it.id == 7 &&
                            it.name == "Prof. Ana" &&
                            it.email == "ana@isel.pt" &&
                            it.password == "Teacher123"
                }
            )
        }
    }

    @Test
    fun `getAll devolve professores remotos e atualiza cache local`() = runTest {
        val repository = TeacherRepository(dao, api)

        coEvery { api.getAll() } returns listOf(
            TeacherResponse(1, "Prof. Ana", "ana@isel.pt"),
            TeacherResponse(2, "Prof. Pedro", "pedro@isel.pt")
        )

        val result = repository.getAll()

        assertEquals(2, result.size)
        assertEquals("Prof. Ana", result[0].name)
        assertEquals("", result[0].password)

        coVerify(exactly = 1) { dao.deleteAll() }
        coVerify(exactly = 2) { dao.insert(any()) }
    }

    @Test
    fun `getById devolve local quando existe`() = runTest {
        val repository = TeacherRepository(dao, api)

        val localTeacher = TeacherEntity(
            id = 5,
            name = "Prof. Local",
            email = "local@isel.pt",
            password = "Teacher123"
        )

        coEvery { dao.getById(5) } returns localTeacher

        val result = repository.getById(5)

        requireNotNull(result)
        assertEquals(5, result.id)
        coVerify(exactly = 0) { api.getAll() }
    }

    @Test
    fun `getById vai a api quando nao existe localmente`() = runTest {
        val repository = TeacherRepository(dao, api)

        coEvery { dao.getById(5) } returns null
        coEvery { api.getAll() } returns listOf(
            TeacherResponse(5, "Prof. API", "api@isel.pt")
        )

        val result = repository.getById(5)

        requireNotNull(result)
        assertEquals("Prof. API", result.name)
        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `login devolve userId e ownerType`() = runTest {
        val repository = TeacherRepository(dao, api)

        coEvery {
            api.login(any())
        } returns LoginResponse(
            userId = 3,
            ownerType = "TEACHER"
        )

        val result = repository.login("ana@isel.pt", "Teacher123")

        requireNotNull(result)
        assertEquals(3, result.first)
        assertEquals(OwnerType.TEACHER, result.second)
    }
}