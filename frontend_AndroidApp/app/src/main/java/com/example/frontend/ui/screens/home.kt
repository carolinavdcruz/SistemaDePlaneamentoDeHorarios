package com.example.frontend.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun HomeScreen(onStartClick: () -> Unit) {
    // Cores baseadas na imagem
    val deepPurple = Color(0xFF1A0B3F)
    val lightPurple = Color(0xFF9D88FF)
    val botBody = Color(0xFFF3F3F3)

    // Estado para o gesto de puxar para cima
    var offsetY by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        // Só permite arrastar para cima (valores negativos)
        if (offsetY + delta <= 0) {
            offsetY += delta
        }
    }

    // Gatilho para navegação quando atingir um limite (ex: -300px)
    LaunchedEffect(offsetY) {
        if (offsetY < -400f) {
            onStartClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(deepPurple, Color(0xFF311B92))
                )
            )
            .padding(24.dp)
    ) {
        // Texto de Boas-vindas
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Meet the",
                color = Color.White,
                fontSize = 32.sp,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "SPH!",
                color = lightPurple,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Placeholder para o Robô (Aqui você usaria Image ou um Canvas)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            // Representação simplificada do Robô
            Surface(
                color = botBody,
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier.size(180.dp, 150.dp)
            ) {
                // Adicione olhos e detalhes aqui
            }

            // Balão de fala
            Surface(
                color = Color(0xFF424242).copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-20).dp)
            ) {
                Text(
                    text = "Need our help\nnow?",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Botão "Get Started" com gesto de Swipe Up
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        // Reseta a posição se não arrastar o suficiente
                        if (offsetY >= -400f) offsetY = 0f
                    }
                )
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, Color.Gray),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Círculo roxo com a seta
                    Surface(
                        color = lightPurple,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Swipe up",
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Text(
                        text = "Slide up to Start",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )

                    // Círculo roxo com a seta
                    Surface(
                        color = lightPurple,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Swipe up",
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onStartClick = {}
    )
}
