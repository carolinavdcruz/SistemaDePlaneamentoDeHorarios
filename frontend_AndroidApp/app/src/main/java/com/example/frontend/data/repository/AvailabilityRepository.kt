package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.AvailabilityDao
import com.example.frontend.data.local.entity.AvailabilityEntity

class AvailabilityRepository(private val dao: AvailabilityDao) {

    suspend fun insert(availability: AvailabilityEntity) {
        println("ROOM INSERT: $availability")
        dao.insert(availability)
    }

    suspend fun update(availability: AvailabilityEntity) {
        dao.update(availability)
    }

    suspend fun delete(availability: AvailabilityEntity) {
        dao.delete(availability)
    }

    suspend fun getById(id: Int): AvailabilityEntity? {
        return dao.getById(id)
    }

    suspend fun getByOwner(ownerId: Int, ownerType: String): List<AvailabilityEntity> {
        return dao.getByOwner(ownerId, ownerType)
    }

    suspend fun getByDay(dayOfWeek: Int): List<AvailabilityEntity> {
        return dao.getByDay(dayOfWeek)
    }

    suspend fun deleteByOwner(ownerId: Int, ownerType: String) {
        dao.deleteByOwner(ownerId, ownerType)
    }
}
