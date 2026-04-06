package com.example.frontend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.local.entity.TimeSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {
    @Query("SELECT * FROM timeslots WHERE dayOfWeek = :day")
    fun getSlotsByDay(day: Int): Flow<List<TimeSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimeSlotEntity>)

    @Query("DELETE FROM timeslots")
    suspend fun clearAll()
}