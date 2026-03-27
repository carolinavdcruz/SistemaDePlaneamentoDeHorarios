package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.AvailabilityDao
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.remote.api.AvailabilityApi
import com.example.frontend.data.remote.dto.AvailabilityRequest

class AvailabilityRepository(private val dao: AvailabilityDao, private val api: AvailabilityApi) {

    private val TAG = "AvailabilityRepository"

    suspend fun insert(availability: AvailabilityEntity) {
        println("ROOM INSERT: $availability")
        
        try {
            // envia para API
            api.createAvailability(
                AvailabilityRequest(
                    ownerId = availability.ownerId,
                    ownerType = availability.ownerType,
                    dayOfWeek = availability.dayOfWeek,
                    startTime = availability.startTime,
                    endTime = availability.endTime
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sending availability to API: ${e.message}")
        }

        // guarda local sempre, mesmo que a API falhe para permitir modo offline
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
        try {
            // Tenta API
            val remote = api.getAvailability(ownerId, ownerType)

            // Se a API responder, atualiza o cache local
            val entities = remote.map {
                AvailabilityEntity(
                    id = it.id,
                    ownerId = it.ownerId,
                    ownerType = it.ownerType,
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime = it.endTime
                )
            }
            dao.deleteByOwner(ownerId, ownerType)
            entities.forEach { dao.insert(it) }
            
            return entities
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching availability from API: ${e.message}")
            // Fallback para o banco de dados local em caso de erro de rede
            return dao.getByOwner(ownerId, ownerType)
        }
    }


    suspend fun getByDay(dayOfWeek: Int): List<AvailabilityEntity> {
        return dao.getByDay(dayOfWeek)
    }

    suspend fun deleteByOwner(ownerId: Int, ownerType: String) {
        dao.deleteByOwner(ownerId, ownerType)
    }
}
