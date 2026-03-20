package com.example.frontend.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // --- 1. HEADER (FOTO E INFO) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = CardBackground,
                        border = BorderStroke(2.dp, AccentPurple)
                    ) {
                        Icon(Icons.Default.Person, "", tint = TextSecondary, modifier = Modifier.padding(20.dp))
                    }
                    Surface(
                        modifier = Modifier.size(32.dp).clickable { /* Abrir galeria */ },
                        shape = CircleShape,
                        color = AccentPurple,
                        border = BorderStroke(2.dp, Background)
                    ) {
                        Icon(Icons.Default.Edit, "", tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("João Silva", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Premium Instructor", color = AccentPurple, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. STATS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = "Classes", value = "24", modifier = Modifier.weight(1f))
                StatCard(label = "Students", value = "128", modifier = Modifier.weight(1f))
                StatCard(label = "Rating", value = "4.9", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. MENU ---
            Text("Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))

            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, InputBorder)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Account Settings",
                        onClick = { navController.navigate("settings_route") } // Exemplo de rota
                    )
                    HorizontalDivider(color = InputBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(icon = Icons.Default.Notifications, title = "Notifications")
                    HorizontalDivider(color = InputBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(icon = Icons.Default.Lock, title = "Privacy & Security")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 4. LOGOUT ---
            Button(
                onClick = { /* Lógica de logout e voltar ao login */ },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.ExitToApp, "", tint = Color(0xFFFF4D4D))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AccentPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.AccountCircle, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
    }
}