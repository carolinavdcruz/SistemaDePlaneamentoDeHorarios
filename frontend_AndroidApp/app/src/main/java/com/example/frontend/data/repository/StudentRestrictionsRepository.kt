package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.remote.api.StudentRestrictionsApi
import com.example.frontend.data.remote.dto.StudentRestrictionsRequest
import com.example.frontend.data.remote.dto.StudentRestrictionsResponse

class StudentRestrictionsRepository(
    private val api: StudentRestrictionsApi
) {

    private val tag = "StudentRestrictionsRepository"

    suspend fun getByStudentId(studentId: Int): StudentRestrictionsResponse? {
        return try {
            api.getStudentRestrictions(studentId)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao obter restrições do aluno: ${e.message}")
            null
        }
    }

    suspend fun save(studentId: Int, weeklyHours: Int) {
        try {
            api.saveStudentRestrictions(
                StudentRestrictionsRequest(
                    studentId = studentId,
                    weeklyHours = weeklyHours
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Erro ao guardar restrições do aluno: ${e.message}")
            throw e
        }
    }
}