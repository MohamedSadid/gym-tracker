package com.bool.gymtracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val GymBlack = Color(0xFF0B0D10)
val GymSurface = Color(0xFF15181E)
val GymSurfaceHigh = Color(0xFF1C2028)
val GymLime = Color(0xFFC8F542)
val GymLimeDim = Color(0xFF8FB82E)
val GymText = Color(0xFFF4F6F8)
val GymMuted = Color(0xFF9AA3B2)
val GymDanger = Color(0xFFFF6B6B)
val GymAmber = Color(0xFFFFB020)

private val GymColors: ColorScheme = darkColorScheme(
    primary = GymLime,
    onPrimary = GymBlack,
    secondary = GymLimeDim,
    background = GymBlack,
    surface = GymSurface,
    surfaceVariant = GymSurfaceHigh,
    onBackground = GymText,
    onSurface = GymText,
    onSurfaceVariant = GymMuted,
    error = GymDanger,
    onError = GymBlack,
    outline = Color(0xFF2A303A),
)

private val GymTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = GymText),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = GymText),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = GymText),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, color = GymText),
    bodyLarge = TextStyle(fontSize = 16.sp, color = GymText),
    bodyMedium = TextStyle(fontSize = 14.sp, color = GymMuted),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GymBlack),
)

@Composable
fun GymTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GymColors,
        typography = GymTypography,
        content = content,
    )
}
