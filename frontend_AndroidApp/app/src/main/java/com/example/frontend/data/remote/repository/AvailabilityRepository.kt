package com.example.frontend.data.remote.repository

import com.example.frontend.data.model.Availability
import com.example.frontend.data.remote.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AvailabilityRepository {

    private val client = ApiClient.client
    private val baseUrl = "http://10.0.2.2:8080" //IDK

    suspend fun addAvailability(studentId: Long, availability: Availability) {
        client.post("$baseUrl/availability/student/$studentId") {
            contentType(ContentType.Application.Json)
            setBody(availability)
        }
    }

    suspend fun getAvailability(studentId: Long): List<Availability> {
        return client.get("$baseUrl/availability/student/$studentId").body()
    }
}