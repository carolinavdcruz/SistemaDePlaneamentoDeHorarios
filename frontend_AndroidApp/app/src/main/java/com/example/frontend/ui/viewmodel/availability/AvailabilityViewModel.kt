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

class AvailabilityViewModel(
    private val availabilityRepository: AvailabilityRepository,
    private val timeSlotRepository: TimeSlotRepository
) : ViewModel() {

    // UI STATE
    private val _selectedDays = MutableStateFlow<Set<String>>(emptySet())
    val selectedDays: StateFlow<Set<String>> = _selectedDays

    private val _startTime = MutableStateFlow("09:00")
    val startTime: StateFlow<String> = _startTime

    private val _endTime = MutableStateFlow("17:00")
    val endTime: StateFlow<String> = _endTime

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
        _selectedDays.value =
            if (_selectedDays.value.contains(day))
                _selectedDays.value - day
            else
                _selectedDays.value + day
    }

    fun setStartTime(time: String) {
        _startTime.value = time
    }

    fun setEndTime(time: String) {
        _endTime.value = time
    }

    fun load(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {
            val data = availabilityRepository.getByOwner(ownerId, ownerType)
            _availabilityList.value = data

            // Preenche a UI com dados guardados
            if (data.isNotEmpty()) {

                _selectedDays.value = data.mapNotNull { availability ->
                    reverseDaysMap[availability.dayOfWeek]
                }.toSet()

                _startTime.value = data.first().startTime
                _endTime.value = data.first().endTime

            } else {

                _selectedDays.value = emptySet()
                _startTime.value = "09:00"
                _endTime.value = "17:00"
            }
        }
    }

    fun saveAvailability(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {

            availabilityRepository.deleteByOwner(ownerId,ownerType)

            val newEntities = _selectedDays.value.map { day ->
                AvailabilityEntity(
                    ownerId   = ownerId,
                    ownerType = ownerType,
                    dayOfWeek = daysMap[day] ?: 1, // TODO: rever este se for null
                    startTime = _startTime.value,
                    endTime   = _endTime.value
                )
            }

            newEntities.forEach { availabilityRepository.insert(it) }

            // criar TimeSlots de 1h
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val triples = newEntities.map { Triple(it.dayOfWeek, it.startTime, it.endTime) }

                val slots = TimeSlotProcessor.processAll(triples, slotDurationMinutes = 60L)

                val slotEntities = slots.mapIndexed { index, slot ->
                    TimeSlotEntity(
                        id = index,           // IDs sequenciais; clearAll() apaga os antigos
                        dayOfWeek = slot.dayOfWeek,
                        startTime = slot.startTime.toString(),
                        endTime = slot.endTime.toString()
                    )
                }

                timeSlotRepository.replaceAll(slotEntities)
            }
            load(ownerId, ownerType)
        }
    }

    fun clear(ownerId: Int, ownerType: OwnerType) {
        viewModelScope.launch {
            availabilityRepository.deleteByOwner(ownerId, ownerType)
            _availabilityList.value = emptyList()
            _selectedDays.value = emptySet()
            _startTime.value = "09:00"
            _endTime.value = "17:00"
        }
    }
}