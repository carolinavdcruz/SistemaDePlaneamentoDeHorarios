package com.example.frontend.repository

import com.example.frontend.data.remote.api.StudentRestrictionsApi
import com.example.frontend.data.remote.dto.StudentRestrictionsResponse
import com.example.frontend.data.repository.StudentRestrictionsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class StudentRestrictionsRepositoryTest {

    private val api: StudentRestrictionsApi = mockk(relaxed = true)

    @Test
    fun `getByStudentId devolve restricoes do aluno`() = runTest {
        val repository = StudentRestrictionsRepository(api)

        coEvery { api.getStudentRestrictions(3) } returns StudentRestrictionsResponse(
            studentId = 3,
            weeklyHours = 4
        )

        val result = repository.getByStudentId(3)

        requireNotNull(result)
        assertEquals(3, result.studentId)
        assertEquals(4, result.weeklyHours)
    }

    @Test
    fun `save envia weeklyHours para api`() = runTest {
        val repository = StudentRestrictionsRepository(api)

        repository.save(studentId = 3, weeklyHours = 5)

        coVerify(exactly = 1) {
            api.saveStudentRestrictions(
                match {
                    it.studentId == 3 && it.weeklyHours == 5
                }
            )
        }
    }
}