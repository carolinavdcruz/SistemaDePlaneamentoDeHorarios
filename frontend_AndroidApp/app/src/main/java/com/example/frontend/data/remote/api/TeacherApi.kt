package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class TeacherRequest(
    val name: String,
    val email: String,
    val sessionDurationMinutes: Int = 60,
    val maxParticipantsPerSession: Int = 5
)

class TeacherApi {
    suspend fun register(request: TeacherRequest) {
        client.post("http://10.0.2.2:8080/teachers") {
            setBody(request)
        }
    }
}