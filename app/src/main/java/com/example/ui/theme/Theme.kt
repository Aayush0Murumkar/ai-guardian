package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = BrandPrimary,
  onPrimary = Color.White,
  primaryContainer = BrandBlueLight,
  onPrimaryContainer = BrandAccentBlue,
  secondary = BrandNavy,
  onSecondary = Color.White,
  secondaryContainer = BrandBlueSelectBg,
  onSecondaryContainer = BrandNavy,
  background = BrandBg,
  onBackground = BrandText,
  surface = Color.White,
  onSurface = BrandDark,
  surfaceVariant = BrandBg,
  onSurfaceVariant = BrandMuted,
  outline = BrandBorder,
  outlineVariant = BrandBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent enterprise dashboard colors per specification
  content: @Composable () -> Unit,
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = BrandBg.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
      }
    }
  }

  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
