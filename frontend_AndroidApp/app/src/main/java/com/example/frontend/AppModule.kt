package com.example.frontend

import android.content.Context
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.ui.viewmodel.student.StudentViewModel
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.local.dao.AvailabilityDao
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.ui.viewmodel.availability.AvailabilityViewModel
import com.example.frontend.data.local.database.AppDatabase
import com.example.frontend.data.local.database.DatabaseProvider
import com.example.frontend.data.remote.api.AvailabilityApi

object AppModule {

    private var database: AppDatabase? = null

    fun init(context: Context) {
        database = DatabaseProvider.getDatabase(context)
    }

    private val studentRepository by lazy {
        StudentRepository(database!!.studentDao())
    }

    private val availabilityApi by lazy {
        AvailabilityApi()
    }

    private val availabilityRepository by lazy {
        AvailabilityRepository(database!!.availabilityDao(), availabilityApi)
    }

    fun provideStudentViewModel(): StudentViewModel {
        return StudentViewModel(studentRepository)
    }

    fun provideAvailabilityViewModel(): AvailabilityViewModel {
        return AvailabilityViewModel(availabilityRepository)
    }
}

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
}

class FakeAvailabilityDao : AvailabilityDao {
    private val availabilities = mutableListOf<AvailabilityEntity>()

    override suspend fun insert(availability: AvailabilityEntity) {
        availabilities.add(availability.copy(id = availabilities.size + 1))
    }

    override suspend fun update(availability: AvailabilityEntity) {
        val index = availabilities.indexOfFirst { it.id == availability.id }
        if (index != -1) availabilities[index] = availability
    }

    override suspend fun delete(availability: AvailabilityEntity) {
        availabilities.removeIf { it.id == availability.id }
    }

    override suspend fun getById(id: Int): AvailabilityEntity? = availabilities.find { it.id == id }

    override suspend fun getByOwner(ownerId: Int, ownerType: String): List<AvailabilityEntity> {
        return availabilities.filter { it.ownerId == ownerId && it.ownerType == ownerType }
    }

    override suspend fun getByDay(dayOfWeek: Int): List<AvailabilityEntity> {
        return availabilities.filter { it.dayOfWeek == dayOfWeek }
    }

    override suspend fun deleteByOwner(ownerId: Int, ownerType: String) {
        availabilities.removeIf { it.ownerId == ownerId && it.ownerType == ownerType }
    }
}
