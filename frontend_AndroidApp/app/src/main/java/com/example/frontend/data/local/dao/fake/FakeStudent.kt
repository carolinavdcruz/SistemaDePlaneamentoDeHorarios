package com.example.frontend.data.local.dao.fake

/*
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.entity.StudentEntity

// Implementação em memória (Não precisa do Room para funcionar)
class FakeStudentDao : StudentDao {
    private val students = mutableListOf<StudentEntity>()

    override suspend fun insert(student: StudentEntity) {
        students.add(student.copy(id = students.size + 1))
    }

    override suspend fun update(student: StudentEntity) {
        val index = students.indexOfFirst { it.id == student.id }
        if (index != -1) students[index] = student
    }

    override suspend fun delete(student: StudentEntity) {
        students.removeIf { it.id == student.id }
    }

    override suspend fun getAll(): List<StudentEntity> = students.toList()

    override suspend fun getById(id: Int): StudentEntity? = students.find { it.id == id }
    override suspend fun getByEmail(email: String): StudentEntity? = students.find { it.email == email }
}

 */