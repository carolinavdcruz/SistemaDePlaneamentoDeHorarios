package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.RestrictionsRequest
import com.example.frontend.data.remote.dto.RestrictionsResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class RestrictionsApi {

    suspend fun getRestrictions(teacherId: Int): RestrictionsResponse {
        return client.get("http://10.0.2.2:8080/restrictions/$teacherId").body()
    }

    suspend fun saveRestrictions(request: RestrictionsRequest) {
        client.put("http://10.0.2.2:8080/restrictions/${request.teacherId}") {
            setBody(request)
        }
    }

}