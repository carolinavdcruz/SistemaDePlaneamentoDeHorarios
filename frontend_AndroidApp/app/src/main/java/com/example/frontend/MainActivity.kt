package com.example.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.frontend.navigation.AppNavHost
import com.example.frontend.ui.theme.Frontend_AndroidAppTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContext.context = applicationContext

        // INICIALIZAR DATABASE
        AppModule.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            Frontend_AndroidAppTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {

    val navController = rememberNavController()

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        AppNavHost(navController = navController)
    }
}