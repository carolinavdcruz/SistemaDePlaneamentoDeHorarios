package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.AvailabilityDao
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.remote.api.AvailabilityApi
import com.example.frontend.data.remote.dto.AvailabilityRequest

class AvailabilityRepository(
    private val dao: AvailabilityDao,
    private val api: AvailabilityApi
) {
    private val tag = "AvailabilityRepository"


    suspend fun insert(availability: AvailabilityEntity) {
        try {
            val remote = api.createAvailability(
                AvailabilityRequest(
                    ownerId = availability.ownerId,
                    ownerType = availability.ownerType.name,
                    dayOfWeek = availability.dayOfWeek,
                    startTime = availability.startTime,
                    endTime = availability.endTime
                )
            )

            dao.insert(
                availability.copy(id = remote.id)
            )
        } catch (e: Exception) {
            Log.e(tag, "Error sending availability to API: ${e.message}")
            throw e
        }
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

    suspend fun getByOwner(ownerId: Int, ownerType: OwnerType): List<AvailabilityEntity> {
        // sincroniza com API em background (não bloqueia)
        try {
            val remote = api.getAvailability(ownerId, ownerType.name)
            val entities = remote.map {
                AvailabilityEntity(
                    id        = it.id,
                    ownerId   = it.ownerId,
                    ownerType = OwnerType.valueOf(it.ownerType),
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime   = it.endTime
                )
            }
            dao.deleteByOwner(ownerId, ownerType)
            entities.forEach { dao.insert(it) }
        } catch (e: Exception) {
            Log.e(tag, "Sincronização com API falhou, usando cache: ${e.message}")
        }
        return dao.getByOwner(ownerId, ownerType)
    }

    suspend fun getByDay(dayOfWeek: Int): List<AvailabilityEntity> {
        return dao.getByDay(dayOfWeek)
    }

    suspend fun deleteByOwner(ownerId: Int, ownerType: OwnerType) {
        try {
            api.deleteAvailability(ownerId, ownerType.name)
            dao.deleteByOwner(ownerId, ownerType)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao apagar disponibilidade na API: ${e.message}")
            throw e
        }
    }

}
