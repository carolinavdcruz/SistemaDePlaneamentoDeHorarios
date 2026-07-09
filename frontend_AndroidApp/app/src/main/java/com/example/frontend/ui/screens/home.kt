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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.ui.theme.Black
import com.example.frontend.ui.theme.White
import com.example.frontend.ui.theme.darkPurple
import com.example.frontend.ui.theme.deepPurple
import com.example.frontend.ui.theme.grey
import com.example.frontend.ui.theme.lightPurple
import kotlin.math.roundToInt

@Composable
fun HomeScreen(onStartClick: () -> Unit) {


    // Estado para o gesto de puxar para cima
    var offsetY by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        if (offsetY + delta <= 0) {
            offsetY += delta
        }
    }

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
                    colors = listOf(deepPurple, darkPurple)
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

        // Placeholder
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier.size(200.dp)
            ) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.descarregar)
                        .build(),
                    contentDescription = "Robô SPH",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(align = Alignment.CenterStart, unbounded = true)
                        .size(700.dp)  // força o GIF a ocupar 500dp, fazendo crop extremo
                        .offset(x = (-45).dp, y = (-30).dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            }

            // Balão de fala
            Surface(
                color = grey.copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (5).dp)
            ) {
                Text(
                    text = "Need our help\nnow?",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Botão "Get Started"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        if (offsetY >= -400f) offsetY = 0f
                    }
                )
        ) {
            Surface(
                color = Black.copy(alpha = 0.5f),
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
                    Surface(
                        color = lightPurple,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Swipe up",
                            tint = White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Text(
                        text = "Slide up to Start",
                        color = White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )

                    Surface(
                        color = lightPurple,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Swipe up",
                            tint = White,
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
