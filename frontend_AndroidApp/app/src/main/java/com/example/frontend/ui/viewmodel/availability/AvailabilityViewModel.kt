package com.example.frontend.ui.viewmodel.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.data.repository.StudentRestrictionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class TimeRangeInput(
    val id: String,
    val startTime: String = "09:00",
    val endTime: String = "17:00"
)

data class DayAvailabilityInput(
    val day: String,
    val ranges: List<TimeRangeInput> = emptyList()
)

class AvailabilityViewModel(
    private val availabilityRepository: AvailabilityRepository,
    private val studentRestrictionsRepository: StudentRestrictionsRepository
) : ViewModel() {

    private val defaultDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private val _dayAvailabilities = MutableStateFlow(
        defaultDays.map { day -> DayAvailabilityInput(day) }
    )
    val dayAvailabilities: StateFlow<List<DayAvailabilityInput>> = _dayAvailabilities

    private val _availabilityList = MutableStateFlow<List<AvailabilityEntity>>(emptyList())
    val availabilityList: StateFlow<List<AvailabilityEntity>> = _availabilityList

    private val _weeklyHours = MutableStateFlow("3")
    val weeklyHours: StateFlow<String> = _weeklyHours

    private val daysMap = mapOf(
        "Mon" to 1,
        "Tue" to 2,
        "Wed" to 3,
        "Thu" to 4,
        "Fri" to 5,
        "Sat" to 6,
        "Sun" to 7
    )

    fun setWeeklyHours(value: String) {
        if (value.all { it.isDigit() } || value.isEmpty()) {
            _weeklyHours.value = value
        }
    }

    fun addRange(day: String) {
        _dayAvailabilities.value = _dayAvailabilities.value.map { dayInput ->
            if (dayInput.day == day) {
                dayInput.copy(
                    ranges = dayInput.ranges + TimeRangeInput(
                        id = UUID.randomUUID().toString()
                    )
                )
            } else {
                dayInput
            }
        }
    }

    fun removeRange(day: String, rangeId: String) {
        _dayAvailabilities.value = _dayAvailabilities.value.map { dayInput ->
            if (dayInput.day == day) {
                dayInput.copy(
                    ranges = dayInput.ranges.filterNot { it.id == rangeId }
                )
            } else {
                dayInput
            }
        }
    }

    fun setStartTime(day: String, rangeId: String, time: String) {
        _dayAvailabilities.value = _dayAvailabilities.value.map { dayInput ->
            if (dayInput.day == day) {
                dayInput.copy(
                    ranges = dayInput.ranges.map { range ->
                        if (range.id == rangeId) range.copy(startTime = time) else range
                    }
                )
            } else {
                dayInput
            }
        }
    }

    fun setEndTime(day: String, rangeId: String, time: String) {
        _dayAvailabilities.value = _dayAvailabilities.value.map { dayInput ->
            if (dayInput.day == day) {
                dayInput.copy(
                    ranges = dayInput.ranges.map { range ->
                        if (range.id == rangeId) range.copy(endTime = time) else range
                    }
                )
            } else {
                dayInput
            }
        }
    }

    fun load(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {
            val data = availabilityRepository.getByOwner(ownerId, ownerType)
            _availabilityList.value = data

            val groupedByDay = data.groupBy { it.dayOfWeek }

            if (ownerType == OwnerType.STUDENT) {
                val restrictions = studentRestrictionsRepository.getByStudentId(ownerId)
                _weeklyHours.value = restrictions?.weeklyHours?.toString() ?: "3"
            } else {
                _weeklyHours.value = "3"
            }

            _dayAvailabilities.value = defaultDays.map { day ->
                val dayNumber = daysMap[day] ?: 1
                val savedRanges = groupedByDay[dayNumber].orEmpty().map { availability ->
                    TimeRangeInput(
                        id = availability.id.toString(),
                        startTime = availability.startTime,
                        endTime = availability.endTime
                    )
                }

                DayAvailabilityInput(
                    day = day,
                    ranges = savedRanges
                )
            }
        }
    }

    fun saveAvailability(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {
            availabilityRepository.deleteByOwner(ownerId, ownerType)

            if (ownerType == OwnerType.STUDENT) {
                val weeklyHoursInt = _weeklyHours.value.toIntOrNull()?.takeIf { it > 0 } ?: 3
                studentRestrictionsRepository.save(ownerId, weeklyHoursInt)
            }

            val selectedAvailabilities = _dayAvailabilities.value.flatMap { dayInput ->
                dayInput.ranges.map { range ->
                    AvailabilityEntity(
                        ownerId = ownerId,
                        ownerType = ownerType,
                        dayOfWeek = daysMap[dayInput.day] ?: 1,
                        startTime = range.startTime,
                        endTime = range.endTime
                    )
                }
            }

            selectedAvailabilities.forEach { availability ->
                availabilityRepository.insert(availability)
            }

            load(ownerId, ownerType)
        }
    }

    fun clear(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {

            availabilityRepository.deleteByOwner(ownerId, ownerType)
            _availabilityList.value = emptyList()
            _dayAvailabilities.value = defaultDays.map { day ->
                DayAvailabilityInput(day)
            }
        }
    }
}
