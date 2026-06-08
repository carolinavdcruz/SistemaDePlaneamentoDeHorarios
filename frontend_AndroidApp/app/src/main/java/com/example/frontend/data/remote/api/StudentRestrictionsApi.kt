package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.StudentRestrictionsRequest
import com.example.frontend.data.remote.dto.StudentRestrictionsResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class StudentRestrictionsApi {

    suspend fun getStudentRestrictions(studentId: Int): StudentRestrictionsResponse {
        return client.get("http://10.0.2.2:8080/studentRestrictions/$studentId").body()
    }

    suspend fun saveStudentRestrictions(request: StudentRestrictionsRequest) {
        client.put("http://10.0.2.2:8080/studentRestrictions/${request.studentId}") {
            setBody(request)
        }
    }
}