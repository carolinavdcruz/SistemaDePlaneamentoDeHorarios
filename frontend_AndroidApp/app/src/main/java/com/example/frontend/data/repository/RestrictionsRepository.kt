package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.RestrictionsDao
import com.example.frontend.data.local.entity.RestrictionsEntity
import com.example.frontend.data.remote.api.RestrictionsApi
import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.RestrictionsRequest
import com.example.frontend.data.remote.dto.RestrictionsResponse
import io.ktor.client.call.body
import io.ktor.client.request.get

class RestrictionsRepository(
    private val dao: RestrictionsDao,
    private val api: RestrictionsApi
) {
    private val tag = "RestrictionsRepository"

    suspend fun insert(restrictions: RestrictionsEntity) {
        dao.insert(restrictions)
        try {
            api.saveRestrictions(
                RestrictionsRequest(
                    teacherId                   = restrictions.teacherId,
                    sessionDurationMinutes      = restrictions.sessionDurationMinutes,
                    maxDailyHours               = restrictions.maxDailyHours,
                    maxParticipantsPerSession   = restrictions.maxParticipantsPerSession,
                    maxSessionsPerStudentPerDay = restrictions.maxSessionsPerStudentPerDay
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Erro ao enviar restrições: ${e.message}")
        }
    }

    suspend fun update(restrictions: RestrictionsEntity) {
        dao.update(restrictions)
        try {
            api.saveRestrictions(
                RestrictionsRequest(
                    teacherId                   = restrictions.teacherId,
                    sessionDurationMinutes      = restrictions.sessionDurationMinutes,
                    maxDailyHours               = restrictions.maxDailyHours,
                    maxParticipantsPerSession   = restrictions.maxParticipantsPerSession,
                    maxSessionsPerStudentPerDay = restrictions.maxSessionsPerStudentPerDay
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Erro ao atualizar restrições: ${e.message}")
        }
    }

    suspend fun delete(restrictions: RestrictionsEntity) {
        try {
            api.deleteRestrictions(
                RestrictionsRequest(
                    teacherId                   = restrictions.teacherId,
                    sessionDurationMinutes      = restrictions.sessionDurationMinutes,
                    maxDailyHours               = restrictions.maxDailyHours,
                    maxParticipantsPerSession   = restrictions.maxParticipantsPerSession,
                    maxSessionsPerStudentPerDay = restrictions.maxSessionsPerStudentPerDay
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Erro ao apagar restrições: ${e.message}")
            dao.delete(restrictions)
        }
    }

    suspend fun getByTeacherId(teacherId: Int): RestrictionsEntity? {
        return try {
            val response = client.get("http://10.0.2.2:8080/restrictions/$teacherId")
            if (response.status.value == 404) {
                return dao.getByTeacherId(teacherId)
            }
            val remote = response.body<RestrictionsResponse>()
            RestrictionsEntity(
                teacherId                   = remote.teacherId,
                sessionDurationMinutes      = remote.sessionDurationMinutes,
                maxDailyHours               = remote.maxDailyHours,
                maxParticipantsPerSession   = remote.maxParticipantsPerSession,
                maxSessionsPerStudentPerDay = remote.maxSessionsPerStudentPerDay
            )
        } catch (e: Exception) {
            Log.e(tag, "Erro ao buscar restrições da API, usando cache local: ${e.message}")
            dao.getByTeacherId(teacherId)
        }
    }
}
