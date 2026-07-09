package com.example.frontend.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.frontend.data.model.OwnerType
import com.example.frontend.ui.screens.students.ChooseTeacherScreen
import com.example.frontend.ui.screens.students.StudentAvailabilityScreen
import com.example.frontend.ui.screens.students.StudentLessonsScreen
import com.example.frontend.ui.screens.teacher.MyStudentsScreen
import com.example.frontend.ui.screens.teacher.TeacherAvailabilityScreen
import com.example.frontend.ui.screens.teacher.TeacherLessonsScreen
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.StatusActive
import com.example.frontend.ui.theme.TextSecondary
import com.example.frontend.ui.theme.White

// CLASSE DE DADOS
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigationScreen(
    navController: NavController,
    userRole: OwnerType,
    userId: Int,
    onSignOutClick: () -> Unit
) {
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val bottomNavItems = when (userRole) {
        OwnerType.TEACHER -> listOf(
            BottomNavItem("Horário", Icons.Filled.DateRange, Icons.Outlined.DateRange),
            BottomNavItem("Aulas", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
            BottomNavItem("Disponibilidade", Icons.Filled.Settings, Icons.Outlined.Settings),
            BottomNavItem("Alunos", Icons.Filled.Face, Icons.Outlined.Face),
            BottomNavItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
        )

        OwnerType.STUDENT -> listOf(
            BottomNavItem("Aulas", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
            BottomNavItem("Disponibilidade", Icons.Filled.DateRange, Icons.Outlined.DateRange),
            BottomNavItem("Professores", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
            BottomNavItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
        )
    }
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
                        label = { Text(item.title, color = if (selectedItemIndex == index) White else TextSecondary) },
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
            when (userRole) {
                OwnerType.TEACHER ->
                    when (selectedItemIndex) {
                        0 -> TeacherScheduleScreen(teacherId = userId, onSignOutClick = onSignOutClick)
                        1 -> TeacherLessonsScreen(teacherId = userId)
                        2 -> TeacherAvailabilityScreen(teacherId = userId)
                        3 -> MyStudentsScreen(teacherId = userId)
                        4 -> ProfileScreen(navController)
                    }
                OwnerType.STUDENT ->
                    when (selectedItemIndex) {
                        0 -> StudentLessonsScreen(studentId = userId)
                        1 -> StudentAvailabilityScreen(studentId = userId)
                        2 -> ChooseTeacherScreen(onTeacherAssigned = { selectedItemIndex = 0 })
                        3 -> ProfileScreen(navController)
                    }
            }
        }
    }
}


// DASHBOARD PRINCIPAL
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeacherScheduleScreen(teacherId: Int, onSignOutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScheduleHeader()
        Spacer(modifier = Modifier.height(24.dp))
        GenerateScheduleButton(teacherId = teacherId)
        Spacer(modifier = Modifier.height(30.dp))
        SignOutButton(onSignOutClick)
    }
}

// COMPONENTES AUXILIARES (Header, etc)
@Composable
fun ScheduleHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Horário", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Cria e confirma o horário semanal", color = TextSecondary, fontSize = 12.sp)
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
        Text("Terminar Sessão", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainNavigationScreenPreview() {
    MainNavigationScreen(
        navController = rememberNavController(),
        userRole = OwnerType.TEACHER,
        userId = 1,
        onSignOutClick = {},
    )
}