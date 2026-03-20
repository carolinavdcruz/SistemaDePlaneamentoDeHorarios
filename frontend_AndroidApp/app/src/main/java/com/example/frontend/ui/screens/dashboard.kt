package com.example.frontend.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.frontend.navigation.Routes
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextMain
import com.example.frontend.ui.theme.TextSecondary


// --- CLASSE DE DADOS ---
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun DashboardScreen(navController: NavController, onSignOutClick: () -> Unit) {
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val bottomNavItems = listOf(
        BottomNavItem("Dashboard", Icons.Filled.DateRange, Icons.Outlined.DateRange, Routes.DASHBOARD),
        BottomNavItem("Parameters", Icons.Filled.Settings, Icons.Outlined.Settings, Routes.PARAMETERS),
        BottomNavItem("Integrations", Icons.Filled.Add, Icons.Outlined.Add, Routes.INTEGRATIONS),
        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, Routes.PROFILE)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, InputBorder, RoundedCornerShape(50.dp))
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        label = { Text(item.title, color = if (selectedItemIndex == index) Color.White else TextSecondary) },
                        icon = {
                            Icon(
                                imageVector = if (selectedItemIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (selectedItemIndex == index) AccentPurple else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentPurple.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItemIndex) {
                0 -> MainDashboardContent(onSignOutClick)
                1 -> ParametersCardMobile()
                2 -> IntegrationsContent()
                3 -> ProfileScreen(navController)
            }
        }
    }
}

@Composable
fun MainDashboardContent(onSignOutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DashboardHeader()
        Spacer(modifier = Modifier.height(24.dp))
        ProposedScheduleCardMobile()
        Spacer(modifier = Modifier.height(30.dp))
        SignOutButton(onSignOutClick)
    }
}

@Composable
fun IntegrationsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Integrations Screen", color = Color.White)
    }
}

// --- COMPONENTES AUXILIARES (Header, etc) ---
@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Teacher Workspace", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Overview of your schedule", color = TextSecondary, fontSize = 12.sp)
        }
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, StatusActive)
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, "", tint = StatusActive, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Google", color = StatusActive, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProposedScheduleCardMobile() {
    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, InputBorder),
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(20.dp)) {
            // Texto do Header
            Text("Proposed Weekly Schedule", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(30.dp))

            // Estado vazio (Simulado)
            Icon(Icons.Default.DateRange, "", tint = TextSecondary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No active proposal", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Generate to see blocks", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun MobileParameterInput(label: String, value: String, placeholder: String) {
    Column {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
        // BasicTextField customizado como no login
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            color = Background,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, InputBorder)
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                if (value.isEmpty()) Text(placeholder, color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp)
                Text(value, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SignOutButton(onSignOutClick: () -> Unit) {
    OutlinedButton(
        onClick = { onSignOutClick() },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        border = BorderStroke(1.dp, InputBorder),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
    ) {
        Icon(Icons.Default.ExitToApp, "", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}