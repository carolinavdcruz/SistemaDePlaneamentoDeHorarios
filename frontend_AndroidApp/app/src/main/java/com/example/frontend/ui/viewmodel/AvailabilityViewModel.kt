package com.example.frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.local.entity.AvailabilityEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AvailabilityViewModel : ViewModel() {

    private val repository = AppModule.availabilityRepository

    private val _availability = MutableStateFlow<List<AvailabilityEntity>>(emptyList())
    val availability: StateFlow<List<AvailabilityEntity>> = _availability

    fun load(ownerId: Int, ownerType: String) {
        viewModelScope.launch {
            _availability.value = repository.getByOwner(ownerId, ownerType)
        }
    }

    fun addAvailability(
        ownerId: Int,
        ownerType: String,
        day: Int,
        start: String,
        end: String
    ) {
        viewModelScope.launch {
            val availability = AvailabilityEntity(
                ownerId = ownerId,
                ownerType = ownerType,
                dayOfWeek = day,
                startTime = start,
                endTime = end
            )

            repository.insert(availability)
            load(ownerId, ownerType)
        }
    }

    fun deleteAvailability(availability: AvailabilityEntity) {
        viewModelScope.launch {
            repository.delete(availability)
            load(availability.ownerId, availability.ownerType)
        }
    }

    fun clearAll(ownerId: Int, ownerType: String) {
        viewModelScope.launch {
            repository.deleteByOwner(ownerId, ownerType)
            load(ownerId, ownerType)
        }
    }
}