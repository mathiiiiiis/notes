package de.mathiiis.notes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF6366F1)
private val IndigoDark = Color(0xFF818CF8)

val LightColors =
    lightColorScheme(
        primary = Indigo,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0E1FF),
        onPrimaryContainer = Color(0xFF1B1B5C),
        secondary = Color(0xFF5C5D72),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1E0F9),
        onSecondaryContainer = Color(0xFF191A2C),
        tertiary = Color(0xFF785189),
        onTertiary = Color(0xFFFFFFFF),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFBFAFF),
        onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFFBFAFF),
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFE4E1EC),
        onSurfaceVariant = Color(0xFF47464F),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F3FA),
        surfaceContainer = Color(0xFFEFEDF4),
        surfaceContainerHigh = Color(0xFFE9E7EF),
        surfaceContainerHighest = Color(0xFFE3E1E9),
        outline = Color(0xFF787680),
        outlineVariant = Color(0xFFC8C5D0),
    )

val DarkColors =
    darkColorScheme(
        primary = IndigoDark,
        onPrimary = Color(0xFF1B1B5C),
        primaryContainer = Color(0xFF3D3D85),
        onPrimaryContainer = Color(0xFFE0E1FF),
        secondary = Color(0xFFC5C4DD),
        onSecondary = Color(0xFF2E2F42),
        secondaryContainer = Color(0xFF444559),
        onSecondaryContainer = Color(0xFFE1E0F9),
        tertiary = Color(0xFFE8B9FB),
        onTertiary = Color(0xFF46225A),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF121316),
        onBackground = Color(0xFFE4E1E9),
        surface = Color(0xFF121316),
        onSurface = Color(0xFFE4E1E9),
        surfaceVariant = Color(0xFF47464F),
        onSurfaceVariant = Color(0xFFC8C5D0),
        surfaceContainerLowest = Color(0xFF0D0E11),
        surfaceContainerLow = Color(0xFF1A1B1F),
        surfaceContainer = Color(0xFF1E1F23),
        surfaceContainerHigh = Color(0xFF292A2E),
        surfaceContainerHighest = Color(0xFF343539),
        outline = Color(0xFF918F9A),
        outlineVariant = Color(0xFF47464F),
    )
