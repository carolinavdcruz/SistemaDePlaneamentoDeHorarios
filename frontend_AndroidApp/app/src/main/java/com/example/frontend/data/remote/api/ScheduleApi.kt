package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.SaveScheduleRequest
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody


class ScheduleApi {
    suspend fun saveSchedule(request: SaveScheduleRequest): Int {
        val response = client.post("http://10.0.2.2:8080/schedule/save") {
            setBody(request)
        }.body<Map<String, String>>()
        return response["scheduleId"]?.toInt() ?: error("scheduleId em falta")
    }

    suspend fun acceptSchedule(scheduleId: Int) {
        client.put("http://10.0.2.2:8080/schedule/$scheduleId/accept")
    }

    suspend fun rejectSchedule(scheduleId: Int) {
        client.put("http://10.0.2.2:8080/schedule/$scheduleId/reject")
    }
}