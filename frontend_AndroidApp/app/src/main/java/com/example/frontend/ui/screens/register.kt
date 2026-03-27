package com.example.frontend.ui.screens

import com.example.frontend.AppModule
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.frontend.ui.theme.AccentPurple
import com.example.frontend.ui.theme.Background
import com.example.frontend.ui.theme.CardBackground
import com.example.frontend.ui.theme.InputBorder
import com.example.frontend.ui.theme.TextSecondary

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onCreateAccountClick: (String, String, String) -> Unit
) {
    val viewModel = remember { AppModule.provideRegisterViewModel() }
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // --- LOGO (Mesmo da anterior) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Surface(
                    color = AccentPurple,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.DateRange, "", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Text(" SPH", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(" FOR YOU", color = AccentPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // --- CARD DE REGISTRO ---
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, InputBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text("Join SPH", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Create your account to start optimizing your time", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

                    Text("I am a...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp))

                    // --- ROLE SELECTOR (Student / Teacher) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RoleCard(
                            label = "Student",
                            icon = Icons.Default.Person,
                            isSelected = selectedRole == "Student",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSelectedRole("Student") }
                        )
                        RoleCard(
                            label = "Teacher",
                            icon = Icons.Default.AccountCircle,
                            isSelected = selectedRole == "Teacher",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSelectedRole("Teacher") }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reutilizar o componente de Input da tela anterior
                    LoginInputField(
                        label = "Email",
                        value = email,
                        onValueChange = viewModel::setEmail,
                        placeholder = "m@example.com",
                        leadingIcon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LoginInputField(
                        label = "Password",
                        value = password,
                        onValueChange = viewModel::setPassword,
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onPasswordVisibleChange = viewModel::togglePasswordVisibility
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            if (viewModel.validateRegister()) {
                                onCreateAccountClick(email, password, selectedRole)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Already have an account? ", color = TextSecondary)
                        Text("Login", color = AccentPurple, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onLoginClick() })
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentPurple else InputBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onLoginClick = {},
        onCreateAccountClick = { nome, email, password -> }
    )
}
