package de.mathiiis.notes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF6366F1)
private val IndigoDark = Color(0xFF818CF8)

val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E1FF),
    onPrimaryContainer = Color(0xFF1B1B5C),
    secondary = Color(0xFF5C5D72),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFBFAFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFBFAFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF47464F),
    outlineVariant = Color(0xFFC8C5D0),
)

val DarkColors = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color(0xFF1B1B5C),
    primaryContainer = Color(0xFF3D3D85),
    onPrimaryContainer = Color(0xFFE0E1FF),
    secondary = Color(0xFFC5C4DD),
    onSecondary = Color(0xFF2E2F42),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC8C5D0),
    outlineVariant = Color(0xFF47464F),
)
