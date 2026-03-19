package com.example.frontend.navigation

sealed class Screen(val route: String) {

    object Dashboard : Screen("dashboard")

    object Participants : Screen("participants")

    object Profile : Screen("profile")

    object Availability : Screen("availability")

    object CreateSchedule : Screen("create_schedule")

}