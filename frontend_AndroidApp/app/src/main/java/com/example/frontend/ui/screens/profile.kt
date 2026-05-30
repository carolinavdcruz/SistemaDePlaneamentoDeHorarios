package com.example.frontend.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.frontend.AppModule
import com.example.frontend.navigation.Routes
import com.example.frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel = remember { AppModule.provideProfileViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.onLogoutNavigated()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- AVATAR ---
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = CardBackground,
                    border = BorderStroke(2.dp, AccentPurple)
                ) {
                    Icon(
                        Icons.Default.Person, "",
                        tint = TextSecondary,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Surface(
                    modifier = Modifier.size(32.dp).clickable { },
                    shape = CircleShape,
                    color = AccentPurple,
                    border = BorderStroke(2.dp, Background)
                ) {
                    Icon(
                        Icons.Default.Edit, "",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(uiState.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(uiState.role?.name ?: "", color = AccentPurple, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // --- INFO CARDS ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, InputBorder)
            ) {
                Column {
                    ProfileInfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = uiState.email
                    )
                    HorizontalDivider(
                        color = InputBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ProfileInfoItem(
                        icon = Icons.Default.Person,
                        label = "Cargo",

                        value = when (uiState.role) {
                            com.example.frontend.data.model.OwnerType.STUDENT -> "Aluno"
                            com.example.frontend.data.model.OwnerType.TEACHER -> "Professor"
                            else -> ""
                        }

                    )

                    if (uiState.role == com.example.frontend.data.model.OwnerType.STUDENT) {
                        HorizontalDivider(
                            color = InputBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        ProfileInfoItem(
                            icon = Icons.Default.Face,
                            label = "Professor",
                            value = uiState.teacherName ?: "Sem professor atribuido"
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            // --- LOGOUT ---
            Button(
                onClick = { viewModel.onLogoutClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4D4D).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, "", tint = Color(0xFFFF4D4D))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AccentPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}