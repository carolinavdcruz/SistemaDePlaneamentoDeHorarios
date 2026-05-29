package com.example.frontend

import android.content.Context
import com.example.frontend.data.repository.StudentRepository
import com.example.frontend.data.remote.api.ScheduleApi
import com.example.frontend.ui.viewmodel.student.StudentViewModel
import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.ui.viewmodel.availability.AvailabilityViewModel
import com.example.frontend.data.local.database.AppDatabase
import com.example.frontend.data.local.database.DatabaseProvider
import com.example.frontend.data.remote.api.AvailabilityApi
import com.example.frontend.data.remote.api.RestrictionsApi
import com.example.frontend.data.repository.RestrictionsRepository
import com.example.frontend.data.session.SessionManager
import com.example.frontend.ui.viewmodel.login.LoginViewModel
import com.example.frontend.ui.viewmodel.profile.ProfileViewModel
import com.example.frontend.ui.viewmodel.teacher.TeacherViewModel
import com.example.frontend.data.repository.TeacherRepository
import com.example.frontend.data.repository.TimeSlotRepository
import com.example.frontend.ui.viewmodel.register.RegisterViewModel
import com.example.frontend.ui.viewmodel.schedule.ScheduleViewModel
import com.example.frontend.ui.viewmodel.student.ChooseTeacherViewModel
import com.example.frontend.ui.viewmodel.teacher.RestrictionsViewModel
import com.example.frontend.data.remote.api.StudentApi
import com.example.frontend.data.remote.api.TeacherApi


object AppModule {

    private var database: AppDatabase? = null
    private val teacherApi by lazy {
        TeacherApi()
    }
    private val studentApi by lazy {
        StudentApi()
    }

    private val restrictionsApi by lazy {
        RestrictionsApi()
    }

    fun init(context: Context) {
        database = DatabaseProvider.getDatabase(context)
    }

    private fun db(): AppDatabase {
        return requireNotNull(database) {
            "Database has not been initialized"
        }
    }

    private val sessionManager by lazy {
        SessionManager(AppContext.context)
    }

    private val teacherRepository by lazy {
        TeacherRepository(db().teacherDao(), teacherApi)
    }

    private val studentRepository by lazy {
        StudentRepository(db().studentDao(), studentApi)
    }

    private val availabilityRepository by lazy {
        AvailabilityRepository(db().availabilityDao(), availabilityApi)
    }

    private val restrictionsRepository by lazy {
        RestrictionsRepository(db().restrictionsDao(), restrictionsApi)
    }

    private val timeSlotRepository by lazy {
        TimeSlotRepository(db().timeSlotDao())
    }

    private val availabilityApi by lazy {
        AvailabilityApi()
    }

    private val scheduleApi by lazy {
        ScheduleApi()
    }

    fun provideStudentViewModel(): StudentViewModel {
        return StudentViewModel(studentRepository)
    }

    fun provideTeacherViewModel(): TeacherViewModel {
        return TeacherViewModel(teacherRepository)
    }

    fun provideAvailabilityViewModel(): AvailabilityViewModel {
        return AvailabilityViewModel(
            availabilityRepository
        )

    }

    fun provideLoginViewModel(): LoginViewModel {
        return LoginViewModel(studentRepository, teacherRepository, sessionManager)
    }

    fun provideRegisterViewModel(): RegisterViewModel {
        return RegisterViewModel(studentRepository, teacherRepository, sessionManager)
    }

    fun provideProfileViewModel(): ProfileViewModel {
        return ProfileViewModel(studentRepository, teacherRepository, sessionManager)
    }

    fun provideChooseTeacherViewModel(): ChooseTeacherViewModel {
        return ChooseTeacherViewModel(teacherRepository, studentRepository, sessionManager)
    }

    fun provideRestrictionsViewModel(): RestrictionsViewModel {
        return RestrictionsViewModel(restrictionsRepository)
    }

    fun provideScheduleViewModel(): ScheduleViewModel {
        return ScheduleViewModel(
            availabilityRepository = availabilityRepository,
            restrictionsRepository = restrictionsRepository,
            studentRepository = studentRepository,
            scheduleApi = scheduleApi
        )
    }
}
