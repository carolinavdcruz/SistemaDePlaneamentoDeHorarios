package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.LoginRequest
import com.example.frontend.data.remote.dto.LoginResponse
import com.example.frontend.data.remote.dto.TeacherRequest
import com.example.frontend.data.remote.dto.TeacherResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class TeacherApi {
    suspend fun register(request: TeacherRequest) : TeacherResponse {
        return client.post("http://10.0.2.2:8080/teachers") {
            setBody(request)
        }.body()
    }

    suspend fun getAll(): List<TeacherResponse> {
        return client.get("http://10.0.2.2:8080/teachers").body()
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        return client.post("http://10.0.2.2:8080/login") {
            setBody(request)
        }.body()
    }


}