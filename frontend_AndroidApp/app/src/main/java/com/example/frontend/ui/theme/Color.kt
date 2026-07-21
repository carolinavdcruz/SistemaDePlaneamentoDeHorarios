package com.example.frontend.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Background = Color(0xFF201B2E) // Roxo muito escuro, quase preto
val CardBackground = Color(0xFF16121F).copy(alpha = 0.8f) // Card ainda mais escuro
val AccentPurple = Color(0xFFA166E1) // O roxo vibrante do botão e logo
val TextMain = Color.White
val TextSecondary = Color(0xFFADADAD)
val InputBackground = Color(0xFF120E19) // Fundo do campo de texto
val InputBorder = Color(0xFF352F46) // Borda do campo de texto

val StatusActive = Color(0xFF4CAF50) // Verde para "Connected"
val BrainPurple = Color(0xFFD4B9FE) // Cor mais suave para o assistente

//colors for home
val deepPurple = Color(0xFF1A0B3F)
val lightPurple = Color(0xFF9D88FF)
val botBody = Color(0xFFF3F3F3)

val darkPurple = Color(0xFF311B92)
val grey = Color(0xFF424242)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

//profile
val RedOut = Color(0xFFFF4D4D)

//register
val Transparent = Color(0x00000000)

//availability
val lightOrange = Color(0xFFFF6B6B)

//Students Screen
val lightRed = Color(0xFFEF5350)

//Schedulescreen
val Red = Color(0xFFFF0000)

//Theme
val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Cores fixas para os campos de texto (OutlinedTextField) usados em toda a app.
 *
 * Os cartões e ecrãs usam um fundo escuro fixo (Background/CardBackground), mas o
 * Material3 por omissão tira as cores do texto/cursor/contorno do color scheme do
 * sistema (inclui "cor dinâmica" no Android 12+). Se esse scheme calhar a ser claro,
 * o texto escrito fica escuro sobre fundo escuro e quase invisível.
 */
@Composable
fun appTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = White,
    unfocusedTextColor = White,
    cursorColor = AccentPurple,
    focusedBorderColor = AccentPurple,
    unfocusedBorderColor = InputBorder,
    focusedLabelColor = AccentPurple,
    unfocusedLabelColor = TextSecondary,
    focusedPlaceholderColor = TextSecondary,
    unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f)
)