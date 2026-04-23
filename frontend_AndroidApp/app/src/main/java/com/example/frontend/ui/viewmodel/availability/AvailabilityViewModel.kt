package com.example.frontend.ui.viewmodel.availability

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.local.entity.TimeSlotEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.model.scheduling.TimeSlotProcessor
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.data.repository.TimeSlotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DayAvailabilityInput(
    val day: String,
    val isSelected: Boolean = false,
    val startTime: String = "09:00",
    val endTime: String = "17:00"
)

class AvailabilityViewModel(
    private val availabilityRepository: AvailabilityRepository,
    //private val timeSlotRepository: TimeSlotRepository
) : ViewModel() {

    // UI STATE
    private val _dayAvailabilities = MutableStateFlow(
        listOf(
            DayAvailabilityInput("Mon"),
            DayAvailabilityInput("Tue"),
            DayAvailabilityInput("Wed"),
            DayAvailabilityInput("Thu"),
            DayAvailabilityInput("Fri"),
            DayAvailabilityInput("Sat"),
            DayAvailabilityInput("Sun")
        )
    )
    val dayAvailabilities: StateFlow<List<DayAvailabilityInput>> = _dayAvailabilities

    // DB STATE
    private val _availabilityList = MutableStateFlow<List<AvailabilityEntity>>(emptyList())
    val availabilityList: StateFlow<List<AvailabilityEntity>> = _availabilityList

    // MAPA PARA CONVERTER DIAS
    private val daysMap = mapOf(
        "Mon" to 1,
        "Tue" to 2,
        "Wed" to 3,
        "Thu" to 4,
        "Fri" to 5,
        "Sat" to 6,
        "Sun" to 7
    )

    private val reverseDaysMap = daysMap.entries.associate { (k, v) -> v to k }

    fun toggleDay(day: String) {
        _dayAvailabilities.value =
            _dayAvailabilities.value.map {
                if (it.day == day) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
    }

    fun setStartTime(day: String, time: String) {
        _dayAvailabilities.value = _dayAvailabilities.value.map {
            if (it.day == day) {
                it.copy(startTime = time)
            } else {
                it
            }
        }
    }

    fun setEndTime(day: String, time: String) {
        _dayAvailabilities.value = _dayAvailabilities.value.map {
            if (it.day == day) {
                it.copy(endTime = time)
            } else {
                it
            }
        }
    }

    fun load(ownerId: Int, ownerType: OwnerType) {

        viewModelScope.launch {
            val data = availabilityRepository.getByOwner(ownerId, ownerType)
            _availabilityList.value = data

            val defaultDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            // Preenche a UI com dados guardados
            _dayAvailabilities.value = defaultDays.map { day ->
                val dayNumber = daysMap[day]
                val savedAvailability = data.find { it.dayOfWeek == dayNumber }

                if (savedAvailability != null) {
                    DayAvailabilityInput(
                        day = day,
                        isSelected = true,
                        startTime = savedAvailability.startTime,
                        endTime = savedAvailability.endTime
                    )
                } else {
                    DayAvailabilityInput(day = day)
                }
            }
        }
    }

    fun saveAvailability(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {

            availabilityRepository.deleteByOwner(ownerId,ownerType)

            val selectedAvailabilities = _dayAvailabilities.value
                .filter { it.isSelected }
                .map { dayInput ->
                    AvailabilityEntity(
                        ownerId = ownerId,
                        ownerType = ownerType,
                        dayOfWeek = daysMap[dayInput.day] ?: 1,
                        startTime = dayInput.startTime,
                        endTime = dayInput.endTime
                    )
                }

            selectedAvailabilities.forEach {
                availabilityRepository.insert(it)
            }

            load(ownerId, ownerType)
        }
    }

    fun clear(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {
            availabilityRepository.deleteByOwner(ownerId, ownerType)
            _availabilityList.value = emptyList()

            _dayAvailabilities.value = listOf(
                DayAvailabilityInput("Mon"),
                DayAvailabilityInput("Tue"),
                DayAvailabilityInput("Wed"),
                DayAvailabilityInput("Thu"),
                DayAvailabilityInput("Fri"),
                DayAvailabilityInput("Sat"),
                DayAvailabilityInput("Sun")
            )
        }
    }
}