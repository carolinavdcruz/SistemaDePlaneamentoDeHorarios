package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.ScheduleCreateRequest
import com.example.frontend.data.remote.dto.ScheduleSessionResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody


class ScheduleApi {

    suspend fun generateSchedule(teacherId: Int): List<ScheduleSessionResponse> {
        return client.post("http://10.0.2.2:8080/schedule/create") {
            setBody(ScheduleCreateRequest(teacherId))
        }.body()
    }

}