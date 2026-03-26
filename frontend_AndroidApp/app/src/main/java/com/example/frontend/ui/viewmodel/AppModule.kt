package com.example.frontend.ui.viewmodel

import com.example.frontend.data.repository.AvailabilityRepository
import com.example.frontend.data.repository.StudentRepository

object AppModule {

    lateinit var availabilityRepository: AvailabilityRepository
    lateinit var studentRepository: StudentRepository

}