package de.mathiiis.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.mathiiis.notes.R

/**
 * Typography.
 *
 * > display, headline and title styles use Roboto Slab
 * > body and label styles stay on the system default which is Roboto
 * > the slab file is a variable font, weight is synthesized where the platform needs it
 */
private val RobotoSlab = FontFamily(Font(R.font.roboto_slab))

private val base = Typography()

val NotesTypography = base.copy(
    // ==== headings on roboto slab ====
    displayLarge = base.displayLarge.copy(fontFamily = RobotoSlab),
    displayMedium = base.displayMedium.copy(fontFamily = RobotoSlab),
    displaySmall = base.displaySmall.copy(fontFamily = RobotoSlab),
    headlineLarge = base.headlineLarge.copy(fontFamily = RobotoSlab),
    headlineMedium = base.headlineMedium.copy(fontFamily = RobotoSlab),
    headlineSmall = base.headlineSmall.copy(fontFamily = RobotoSlab),
    titleLarge = base.titleLarge.copy(fontFamily = RobotoSlab),
    titleMedium = base.titleMedium.copy(fontFamily = RobotoSlab, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = RobotoSlab),

    // ==== body on roboto, loosened a touch for long notes ====
    bodyLarge = base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
)
