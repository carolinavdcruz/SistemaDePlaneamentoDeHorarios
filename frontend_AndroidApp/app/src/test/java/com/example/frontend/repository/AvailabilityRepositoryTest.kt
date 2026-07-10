package com.example.frontend.repository

import com.example.frontend.data.local.dao.AvailabilityDao
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.remote.api.AvailabilityApi
import com.example.frontend.data.remote.dto.AvailabilityResponse
import com.example.frontend.data.repository.AvailabilityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class AvailabilityRepositoryTest {

    private val dao: AvailabilityDao = mockk(relaxed = true)
    private val api: AvailabilityApi = mockk(relaxed = true)

    @Test
    fun `insert envia para api e guarda com id remoto`() = runTest {
        val repository = AvailabilityRepository(dao, api)

        val entity = AvailabilityEntity(
            id = 0,
            ownerId = 1,
            ownerType = OwnerType.TEACHER,
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:00"
        )

        coEvery { api.createAvailability(any()) } returns AvailabilityResponse(
            id = 99,
            ownerId = 1,
            ownerType = "TEACHER",
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:00"
        )

        repository.insert(entity)

        coVerify(exactly = 1) {
            dao.insert(
                match {
                    it.id == 99 &&
                            it.ownerId == 1 &&
                            it.ownerType == OwnerType.TEACHER
                }
            )
        }
    }

    @Test
    fun `getByOwner sincroniza remoto e devolve cache`() = runTest {
        val repository = AvailabilityRepository(dao, api)

        coEvery { api.getAvailability(1, "TEACHER") } returns listOf(
            AvailabilityResponse(
                id = 10,
                ownerId = 1,
                ownerType = "TEACHER",
                dayOfWeek = 1,
                startTime = "09:00",
                endTime = "10:00"
            )
        )

        coEvery { dao.getByOwner(1, OwnerType.TEACHER) } returns listOf(
            AvailabilityEntity(
                id = 10,
                ownerId = 1,
                ownerType = OwnerType.TEACHER,
                dayOfWeek = 1,
                startTime = "09:00",
                endTime = "10:00"
            )
        )

        val result = repository.getByOwner(1, OwnerType.TEACHER)

        assertEquals(1, result.size)
        assertEquals("09:00", result.first().startTime)

        coVerify(exactly = 1) { dao.deleteByOwner(1, OwnerType.TEACHER) }
        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `deleteByOwner apaga na api e localmente`() = runTest {
        val repository = AvailabilityRepository(dao, api)

        repository.deleteByOwner(1, OwnerType.STUDENT)

        coVerify(exactly = 1) { api.deleteAvailability(1, "STUDENT") }
        coVerify(exactly = 1) { dao.deleteByOwner(1, OwnerType.STUDENT) }
    }
}