package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SafeGramLightColorScheme = lightColorScheme(
    primary = SafetyEmerald,
    onPrimary = Color.White,
    primaryContainer = SafetyEmeraldContainer,
    onPrimaryContainer = SafetyEmeraldText,
    secondary = SlateDark,
    onSecondary = Color.White,
    secondaryContainer = SlateSubtle,
    onSecondaryContainer = SlateDark,
    tertiary = AlertAmber,
    onTertiary = Color.White,
    tertiaryContainer = AlertAmberContainer,
    onTertiaryContainer = AlertAmberText,
    error = HazardRed,
    onError = Color.White,
    errorContainer = HazardRedContainer,
    onErrorContainer = HazardRedText,
    background = CanvasLight,
    onBackground = SlateInk,
    surface = SurfaceWhite,
    onSurface = SlateInk,
    surfaceVariant = SlateSubtle,
    onSurfaceVariant = SlateSecondary,
    outline = BorderInput,
    outlineVariant = BorderHairline,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = CanvasLight,
    surfaceContainer = SlateSubtle,
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
)

private val SafeGramDarkColorScheme = darkColorScheme(
    primary = DarkEmerald,
    onPrimary = Color(0xFF002113),
    primaryContainer = SafetyEmeraldDeep,
    onPrimaryContainer = Color(0xFFA6FFD0),
    secondary = Color(0xFFCBD5E1),
    onSecondary = SlateDark,
    secondaryContainer = DarkSurfaceHigh,
    onSecondaryContainer = DarkOnSurface,
    tertiary = AlertAmber,
    onTertiary = Color(0xFF2A1700),
    tertiaryContainer = Color(0xFF452B00),
    onTertiaryContainer = Color(0xFFFFDDB8),
    error = HazardRed,
    onError = Color.White,
    errorContainer = Color(0xFF5F1414),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkCanvas,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    surfaceContainerLowest = DarkCanvas,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceHigh,
)

val SafeGramShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun SafeGramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SafeGramDarkColorScheme else SafeGramLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SafeGramTypography,
        shapes = SafeGramShapes,
        content = content,
    )
}
