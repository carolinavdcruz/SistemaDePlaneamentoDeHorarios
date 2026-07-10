package com.example.frontend.repository

import com.example.frontend.data.local.dao.RestrictionsDao
import com.example.frontend.data.local.entity.RestrictionsEntity
import com.example.frontend.data.remote.api.RestrictionsApi
import com.example.frontend.data.remote.dto.RestrictionsResponse
import com.example.frontend.data.repository.RestrictionsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class RestrictionsRepositoryTest {

    private val dao: RestrictionsDao = mockk(relaxed = true)
    private val api: RestrictionsApi = mockk(relaxed = true)

    @Test
    fun `insert guarda localmente e envia para api`() = runTest {
        val repository = RestrictionsRepository(dao, api)

        val entity = RestrictionsEntity(
            teacherId = 1,
            maxDailyHours = 8,
            sessionDurationMinutes = 60,
            maxParticipantsPerSession = 3,
            maxSessionsPerStudentPerDay = 1
        )

        repository.insert(entity)

        coVerify(exactly = 1) { dao.insert(entity) }
        coVerify(exactly = 1) { api.saveRestrictions(any()) }
    }

    @Test
    fun `update atualiza localmente e envia para api`() = runTest {
        val repository = RestrictionsRepository(dao, api)

        val entity = RestrictionsEntity(
            teacherId = 1,
            maxDailyHours = 6,
            sessionDurationMinutes = 45,
            maxParticipantsPerSession = 2,
            maxSessionsPerStudentPerDay = 2
        )

        repository.update(entity)

        coVerify(exactly = 1) { dao.update(entity) }
        coVerify(exactly = 1) { api.saveRestrictions(any()) }
    }

    @Test
    fun `getByTeacherId devolve remoto e atualiza cache`() = runTest {
        val repository = RestrictionsRepository(dao, api)

        coEvery { api.getRestrictions(1) } returns RestrictionsResponse(
            teacherId = 1,
            maxDailyHours = 8,
            sessionDurationMinutes = 60,
            maxParticipantsPerSession = 3,
            maxSessionsPerStudentPerDay = 1
        )

        val result = repository.getByTeacherId(1)

        requireNotNull(result)
        assertEquals(1, result.teacherId)
        assertEquals(8, result.maxDailyHours)

        coVerify(exactly = 1) { dao.insert(any()) }
    }
}