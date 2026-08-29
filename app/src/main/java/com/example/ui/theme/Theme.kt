package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = OverlayCyan,
    onPrimary = CameraBlack,
    primaryContainer = CameraSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AccentAmber,
    onSecondary = CameraBlack,
    error = RecordRed,
    onError = Color.White,
    background = CameraBlack,
    onBackground = TextPrimary,
    surface = CameraSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CameraSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CameraBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

