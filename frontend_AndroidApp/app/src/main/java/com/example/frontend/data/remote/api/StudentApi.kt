package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class StudentRequest(
    val name: String,
    val email: String
)

class StudentApi {
    suspend fun register(request: StudentRequest) {
        client.post("http://10.0.2.2:8080/students") {
            setBody(request)
        }
    }
}