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
import com.example.frontend.ui.viewmodel.login.LoginViewModel
import com.example.frontend.ui.viewmodel.teacher.TeacherViewModel
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.session.SessionManager
import com.example.frontend.ui.viewmodel.profile.ProfileViewModel
import com.example.frontend.ui.viewmodel.register.RegisterViewModel

object AppModule {

    private var database: AppDatabase? = null
    private var sessionManager: SessionManager? = null

    fun init(context: Context) {
        database = DatabaseProvider.getDatabase(context)
        sessionManager = SessionManager(context)
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

    private val teacherRepository by lazy {
        TeacherRepository(database!!.teacherDao())
    }

    fun provideStudentViewModel(): StudentViewModel {
        return StudentViewModel(studentRepository)
    }

    fun provideAvailabilityViewModel(): AvailabilityViewModel {
        return AvailabilityViewModel(availabilityRepository)
    }

    fun provideLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            studentDao     = database!!.studentDao(),
            teacherDao     = database!!.teacherDao(),
            sessionManager = sessionManager!!
        )
    }

    fun provideRegisterViewModel(): RegisterViewModel {
        return RegisterViewModel()
    }

    fun provideTeacherViewModel(): TeacherViewModel {
        return TeacherViewModel(teacherRepository)
    }

    fun provideProfileViewModel(): ProfileViewModel {
        return ProfileViewModel(
            studentDao     = database!!.studentDao(),
            teacherDao     = database!!.teacherDao(),
            sessionManager = sessionManager!!
        )
    }
}