package com.example.frontend.data.remote.repository

import com.example.frontend.data.model.Student
import com.example.frontend.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class StudentRepository {

    private val client = ApiClient.client
    private val baseUrl = "http://10.0.2.2:8080" //IDKKKK

    suspend fun getStudents(): List<Student> {
        return client.get("$baseUrl/students").body()
    }

    suspend fun createStudent(student: Student): Student {
        return client.post("$baseUrl/students") {
            contentType(ContentType.Application.Json)
            setBody(student)
        }.body()
    }

    suspend fun updateStudent(id: Long, student: Student): Student {
        return client.put("$baseUrl/students/$id") {
            contentType(ContentType.Application.Json)
            setBody(student)
        }.body()
    }

    suspend fun deleteStudent(id: Long) {
        client.delete("$baseUrl/students/$id")
    }
}