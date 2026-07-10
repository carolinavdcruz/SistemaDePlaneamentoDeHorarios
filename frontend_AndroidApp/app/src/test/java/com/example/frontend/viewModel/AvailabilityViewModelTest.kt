package com.example.frontend.viewModel

import com.example.frontend.MainDispatcherRule
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.ui.viewmodel.availability.AvailabilityViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvailabilityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AvailabilityRepository>()

    @Test
    fun `addRange adiciona um intervalo ao dia certo`() {
        val viewModel = AvailabilityViewModel(repository)

        viewModel.addRange("Mon")

        val monday = viewModel.dayAvailabilities.value.first { it.day == "Mon" }
        assertEquals(1, monday.ranges.size)
    }

    @Test
    fun `removeRange remove o intervalo correto`() {
        val viewModel = AvailabilityViewModel(repository)

        viewModel.addRange("Mon")
        val rangeId = viewModel.dayAvailabilities.value.first { it.day == "Mon" }.ranges.first().id

        viewModel.removeRange("Mon", rangeId)

        val monday = viewModel.dayAvailabilities.value.first { it.day == "Mon" }
        assertTrue(monday.ranges.isEmpty())
    }

    @Test
    fun `setStartTime e setEndTime atualizam intervalo`() {
        val viewModel = AvailabilityViewModel(repository)

        viewModel.addRange("Tue")
        val rangeId = viewModel.dayAvailabilities.value.first { it.day == "Tue" }.ranges.first().id

        viewModel.setStartTime("Tue", rangeId, "10:00")
        viewModel.setEndTime("Tue", rangeId, "12:00")

        val range = viewModel.dayAvailabilities.value.first { it.day == "Tue" }.ranges.first()
        assertEquals("10:00", range.startTime)
        assertEquals("12:00", range.endTime)
    }

    @Test
    fun `load agrupa disponibilidades por dia`() = runTest {
        coEvery { repository.getByOwner(1, OwnerType.STUDENT) } returns listOf(
            AvailabilityEntity(ownerId = 1, ownerType = OwnerType.STUDENT, dayOfWeek = 1, startTime = "09:00", endTime = "10:00"),
            AvailabilityEntity(ownerId = 1, ownerType = OwnerType.STUDENT, dayOfWeek = 1, startTime = "14:00", endTime = "15:00"),
            AvailabilityEntity(ownerId = 1, ownerType = OwnerType.STUDENT, dayOfWeek = 3, startTime = "11:00", endTime = "12:00")
        )

        val viewModel = AvailabilityViewModel(repository)
        viewModel.load(1, OwnerType.STUDENT)
        advanceUntilIdle()

        val monday = viewModel.dayAvailabilities.value.first { it.day == "Mon" }
        val wednesday = viewModel.dayAvailabilities.value.first { it.day == "Wed" }

        assertEquals(2, monday.ranges.size)
        assertEquals(1, wednesday.ranges.size)
    }

    @Test
    fun `saveAvailability apaga antigas e grava novas`() = runTest {
        coEvery { repository.deleteByOwner(1, OwnerType.TEACHER) } returns Unit
        coEvery { repository.insert(any()) } returns Unit
        coEvery { repository.getByOwner(1, OwnerType.TEACHER) } returns emptyList()

        val viewModel = AvailabilityViewModel(repository)
        viewModel.addRange("Mon")
        val rangeId = viewModel.dayAvailabilities.value.first { it.day == "Mon" }.ranges.first().id
        viewModel.setStartTime("Mon", rangeId, "09:00")
        viewModel.setEndTime("Mon", rangeId, "11:00")

        viewModel.saveAvailability(1, OwnerType.TEACHER)
        advanceUntilIdle()

        coVerify { repository.deleteByOwner(1, OwnerType.TEACHER) }
        coVerify { repository.insert(any()) }
    }

    @Test
    fun `clear limpa repositorio e estado local`() = runTest {
        coEvery { repository.deleteByOwner(2, OwnerType.STUDENT) } returns Unit

        val viewModel = AvailabilityViewModel(repository)
        viewModel.addRange("Fri")

        viewModel.clear(2, OwnerType.STUDENT)
        advanceUntilIdle()

        coVerify { repository.deleteByOwner(2, OwnerType.STUDENT) }

        val friday = viewModel.dayAvailabilities.value.first { it.day == "Fri" }
        assertTrue(friday.ranges.isEmpty())
    }
}