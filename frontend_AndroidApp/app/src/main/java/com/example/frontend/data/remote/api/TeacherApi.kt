package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.TeacherRequest
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class TeacherApi {
    suspend fun register(request: TeacherRequest): Int {
        val response = client.post("http://10.0.2.2:8080/teachers") {
            setBody(request)
        }.body<Map<String, String>>()
        return response["id"]?.toInt() ?: error("id em falta na resposta")
    }
}