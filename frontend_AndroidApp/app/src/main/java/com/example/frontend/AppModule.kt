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


object AppModule {

    private var database: AppDatabase? = null

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

    private val studentRepository by lazy {
        StudentRepository(db().studentDao())
    }

    private val teacherRepository by lazy {
        TeacherRepository(db().teacherDao())
    }

    private val availabilityRepository by lazy {
        AvailabilityRepository(db().availabilityDao())
    }

    private val restrictionsRepository by lazy {
        RestrictionsRepository(db().restrictionsDao())
    }

    private val timeSlotRepository by lazy {
        TimeSlotRepository(db().timeSlotDao())
    }

    /*
    private val availabilityApi by lazy {
        AvailabilityApi()
    }
    */


    fun provideStudentViewModel(): StudentViewModel {
        return StudentViewModel(studentRepository)
    }

    fun provideTeacherViewModel(): TeacherViewModel {
        return TeacherViewModel(teacherRepository)
    }

    fun provideAvailabilityViewModel(): AvailabilityViewModel {
        return AvailabilityViewModel(
            availabilityRepository,
            //timeSlotRepository
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
            studentRepository = studentRepository
        )
    }
}





