package de.mathiiis.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Typography overrides
 *
 * > only body and editor relevant styles are tuned, rest stay Material defaults
 * > line height is loosened a little so long note bodies stay readable
 */
val NotesTypography = Typography(
    bodyLarge = TextStyle(
        fontSize = 17.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
)
