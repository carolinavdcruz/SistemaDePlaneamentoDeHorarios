package com.example.frontend.data.repository

import com.example.frontend.data.local.dao.TimeSlotDao
import com.example.frontend.data.local.entity.TimeSlotEntity

class TimeSlotRepository(private val dao: TimeSlotDao) {

    suspend fun replaceAll(slots: List<TimeSlotEntity>) {
        dao.clearAll()
        dao.insertSlots(slots)
    }
}