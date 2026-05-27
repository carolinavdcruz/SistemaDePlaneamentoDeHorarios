package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.client
import com.example.frontend.data.remote.dto.AssignTeacherRequest
import com.example.frontend.data.remote.dto.StudentRequest
import com.example.frontend.data.remote.dto.StudentResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class StudentApi {

    suspend fun register(request: StudentRequest) : StudentResponse {
        return client.post("http://10.0.2.2:8080/students") {
            setBody(request)
        }.body()
    }

    suspend fun assignTeacher(studentId: Int, teacherId: Int) {
        client.post("http://10.0.2.2:8080/students/assign-teacher") {
            setBody(AssignTeacherRequest(studentId, teacherId))
        }
    }

    suspend fun unassignTeacher(studentId: Int) {
        client.post("http://10.0.2.2:8080/students/unassign-teacher/$studentId")
    }

    suspend fun getStudentsByTeacher(teacherId: Int): List<StudentResponse> {
        return client.get("http://10.0.2.2:8080/students/by-teacher/$teacherId").body()
    }

}