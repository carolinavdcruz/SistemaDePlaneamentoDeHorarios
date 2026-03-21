package com.example.frontend.data.remote.repository

import com.example.frontend.data.model.Teacher
import com.example.frontend.data.remote.ApiClient
import io.ktor.client.request.get
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TeacherRepository {

    private val client = ApiClient.client
    private val baseUrl = "http://10.0.2.2:8080" // IDKKKKK

    suspend fun createTeacher(teacher: Teacher): Teacher {
        return client.post("$baseUrl/teachers") {
            contentType(ContentType.Application.Json)
            setBody(teacher)
        }.body()
    }

    suspend fun getTeacher(): Teacher {
        return client.get("$baseUrl/teachers/1").body()
    }
}