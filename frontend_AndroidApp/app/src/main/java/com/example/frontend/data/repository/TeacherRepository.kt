package com.example.frontend.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.dao.TimeSlotDao
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.local.entity.TimeSlotEntity
import com.example.frontend.data.model.TimeSlot
import com.example.frontend.data.remote.api.AvailabilityApi
import com.example.frontend.data.remote.dto.AvailabilityRequest
import com.example.frontend.data.remote.dto.AvailabilityResponse
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class TeacherRepository(
    private val dao: TeacherDao,
    private val timeSlotDao: TimeSlotDao,
    private val api: AvailabilityApi
) {

    suspend fun insert(teacher: TeacherEntity) {
        dao.insert(teacher)
    }

    suspend fun update(teacher: TeacherEntity) {
        dao.update(teacher)
    }

    suspend fun delete(teacher: TeacherEntity) {
        dao.delete(teacher)
    }

    suspend fun getAll(): List<TeacherEntity> {
        return dao.getAll()
    }

    suspend fun getById(id: Int): TeacherEntity? {
        return dao.getById(id)
    }

    // Converte de Entity (DB) para Model (UI)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSlotsForDay(day: Int): Flow<List<TimeSlot>> {
        return timeSlotDao.getSlotsByDay(day).map { entities ->
            entities.map {
                TimeSlot(
                    id = it.id,
                    dayOfWeek = it.dayOfWeek,
                    startTime = LocalTime.parse(it.startTime),
                    endTime = LocalTime.parse(it.endTime)
                )
            }
        }
    }

    suspend fun syncAvailability(teacherId: Int, day: Int, start: String, end: String) {
        val request = AvailabilityRequest(
            ownerId = teacherId,
            ownerType = "TEACHER",
            dayOfWeek = day,
            startTime = start,
            endTime = end
        )
        val response = api.setupAvailability(request)

        if (response.status.isSuccess()) {
            val entities = response.body<List<AvailabilityResponse>>().map {
                TimeSlotEntity(
                    id = it.id,
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime = it.endTime
                )
            }
            timeSlotDao.insertSlots(entities)
        }
    }

    suspend fun updateTeacherAvailability(
        teacherId: Int,
        day: Int,
        start: String,
        end: String
    ): Boolean {
        val request = AvailabilityRequest(
            ownerId = teacherId,
            ownerType = "TEACHER",
            dayOfWeek = day,
            startTime = start,
            endTime = end
        )
        val response = api.setupAvailability(request)

        return if (response.status.isSuccess()) {
            timeSlotDao.clearAll()
            val newList = response.body<List<AvailabilityResponse>>()
            val entities = newList.map {
                TimeSlotEntity(
                    id = it.id,
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime = it.endTime
                )
            }
            timeSlotDao.insertSlots(entities)
            true
        } else {
            false
        }
    }
}
