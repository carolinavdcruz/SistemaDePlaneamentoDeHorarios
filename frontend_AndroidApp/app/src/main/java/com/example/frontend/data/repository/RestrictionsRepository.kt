package com.example.frontend.data.repository

import com.example.frontend.data.local.dao.RestrictionsDao
import com.example.frontend.data.local.entity.RestrictionsEntity
import com.example.frontend.data.remote.api.RestrictionsApi
import com.example.frontend.data.remote.dto.RestrictionsRequest

class RestrictionsRepository(
    private val dao: RestrictionsDao,
    private val api: RestrictionsApi
) {
    suspend fun insert(restrictions: RestrictionsEntity) {
        dao.insert(restrictions)
        api.saveRestrictions(
            RestrictionsRequest(
                teacherId = restrictions.teacherId,
                maxDailyHours = restrictions.maxDailyHours,
                sessionDurationMinutes = restrictions.sessionDurationMinutes,
                maxParticipantsPerSession = restrictions.maxParticipantsPerSession,
                maxSessionsPerStudentPerDay = restrictions.maxSessionsPerStudentPerDay
            )
        )
    }

    suspend fun update(restrictions: RestrictionsEntity) {
        dao.update(restrictions)
        api.saveRestrictions(
            RestrictionsRequest(
                teacherId = restrictions.teacherId,
                maxDailyHours = restrictions.maxDailyHours,
                sessionDurationMinutes = restrictions.sessionDurationMinutes,
                maxParticipantsPerSession = restrictions.maxParticipantsPerSession,
                maxSessionsPerStudentPerDay = restrictions.maxSessionsPerStudentPerDay
            )
        )
    }

    suspend fun getByTeacherId(teacherId: Int): RestrictionsEntity? {
        return try {
            val remote = api.getRestrictions(teacherId)
            val restriction = RestrictionsEntity(
                teacherId = remote.teacherId,
                maxDailyHours = remote.maxDailyHours,
                sessionDurationMinutes = remote.sessionDurationMinutes,
                maxParticipantsPerSession = remote.maxParticipantsPerSession,
                maxSessionsPerStudentPerDay = remote.maxSessionsPerStudentPerDay
            )
            dao.insert(restriction)
            restriction
        } catch (e: Exception) {
            dao.getByTeacherId(teacherId)
        }
    }

    suspend fun delete(restrictions: RestrictionsEntity) {
        dao.delete(restrictions)
    }
}

