package com.example.frontend.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.frontend.data.local.entity.AvailabilityEntity

@Dao
interface AvailabilityDao {

    @Insert
    suspend fun insert(availability: AvailabilityEntity)

    @Update
    suspend fun update(availability: AvailabilityEntity)

    @Delete
    suspend fun delete(availability: AvailabilityEntity)

    @Query("SELECT * FROM availability WHERE id = :id")
    suspend fun getById(id: Int): AvailabilityEntity?

    @Query("SELECT * FROM availability WHERE ownerId = :ownerId AND ownerType = :ownerType")
    suspend fun getByOwner(ownerId: Int, ownerType: String): List<AvailabilityEntity>

    @Query("SELECT * FROM availability WHERE dayOfWeek = :dayOfWeek")
    suspend fun getByDay(dayOfWeek: Int): List<AvailabilityEntity>

    @Query("DELETE FROM availability WHERE ownerId = :ownerId AND ownerType = :ownerType")
    suspend fun deleteByOwner(ownerId: Int, ownerType: String)
}