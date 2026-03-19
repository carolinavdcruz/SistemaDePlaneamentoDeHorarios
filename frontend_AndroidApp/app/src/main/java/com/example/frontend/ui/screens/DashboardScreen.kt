package com.example.frontend.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {

    Column {
        Text("Dashboard")

        Button(onClick = {
            navController.navigate("profile")
        }) {
            Text("Ir para Perfil")
        }

    }
}