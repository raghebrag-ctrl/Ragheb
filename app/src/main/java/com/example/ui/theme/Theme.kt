package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val HebrDarkColorScheme = darkColorScheme(
  primary = HebrInkDarkPrimary,
  onPrimary = HebrOnInkDarkPrimary,
  primaryContainer = HebrInkDarkPrimaryContainer,
  onPrimaryContainer = HebrOnInkDarkPrimaryContainer,
  secondary = HebrTealDarkSecondary,
  onSecondary = HebrOnTealDarkSecondary,
  secondaryContainer = HebrTealDarkSecondaryContainer,
  onSecondaryContainer = HebrOnTealDarkSecondaryContainer,
  tertiary = HebrAmberDarkTertiary,
  onTertiary = HebrOnAmberDarkTertiary,
  tertiaryContainer = HebrAmberDarkTertiaryContainer,
  onTertiaryContainer = HebrOnAmberDarkTertiaryContainer,
  background = HebrDarkBackground,
  onBackground = HebrOnDarkBackground,
  surface = HebrDarkSurface,
  onSurface = HebrOnDarkBackground,
  surfaceVariant = HebrDarkSurfaceVariant,
  onSurfaceVariant = HebrDarkOnSurfaceVariant,
  outline = HebrDarkOutline,
  outlineVariant = HebrDarkSurfaceVariant,
  error = HebrError,
  errorContainer = HebrErrorContainer,
  onErrorContainer = HebrOnErrorContainer,
)

private val HebrLightColorScheme = lightColorScheme(
  primary = HebrInkPrimary,
  onPrimary = HebrOnInkPrimary,
  primaryContainer = HebrInkPrimaryContainer,
  onPrimaryContainer = HebrOnInkPrimaryContainer,
  secondary = HebrTealSecondary,
  onSecondary = HebrOnTealSecondary,
  secondaryContainer = HebrTealSecondaryContainer,
  onSecondaryContainer = HebrOnTealSecondaryContainer,
  tertiary = HebrAmberTertiary,
  onTertiary = HebrOnAmberTertiary,
  tertiaryContainer = HebrAmberTertiaryContainer,
  onTertiaryContainer = HebrOnAmberTertiaryContainer,
  background = HebrParchmentBackground,
  onBackground = HebrOnParchment,
  surface = HebrSurface,
  onSurface = HebrOnParchment,
  surfaceVariant = HebrSurfaceVariant,
  onSurfaceVariant = HebrOnSurfaceVariant,
  outline = HebrOutline,
  outlineVariant = HebrOutlineVariant,
  error = HebrError,
  errorContainer = HebrErrorContainer,
  onErrorContainer = HebrOnErrorContainer,
)

val HebrShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HebrAssociationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) HebrDarkColorScheme else HebrLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = HebrTypography,
    shapes = HebrShapes,
    content = content,
  )
}
