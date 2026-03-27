package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.AvailabilityRequest
import com.example.frontend.data.remote.dto.AvailabilityResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AvailabilityApi {

    suspend fun getAvailability(ownerId: Int, ownerType: String): List<AvailabilityResponse> {
        return client.get("http://10.0.2.2:8080/availability") {
            parameter("ownerId", ownerId)
            parameter("ownerType", ownerType)
        }.body()
    }

    suspend fun createAvailability(request: AvailabilityRequest) {
        client.post("http://10.0.2.2:8080/availability") {
            setBody(request)
        }
    }
}
