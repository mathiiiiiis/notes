@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package de.mathiiis.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.mathiiis.notes.R

private val RobotoSlab = FontFamily(Font(R.font.roboto_slab))

private val base = Typography()

val NotesTypography =
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = RobotoSlab),
        displayMedium = base.displayMedium.copy(fontFamily = RobotoSlab),
        displaySmall = base.displaySmall.copy(fontFamily = RobotoSlab),
        headlineLarge = base.headlineLarge.copy(fontFamily = RobotoSlab),
        headlineMedium = base.headlineMedium.copy(fontFamily = RobotoSlab),
        headlineSmall = base.headlineSmall.copy(fontFamily = RobotoSlab),
        titleLarge = base.titleLarge.copy(fontFamily = RobotoSlab),
        titleMedium = base.titleMedium.copy(fontFamily = RobotoSlab, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = RobotoSlab),
        displayLargeEmphasized = base.displayLargeEmphasized.copy(fontFamily = RobotoSlab),
        displayMediumEmphasized = base.displayMediumEmphasized.copy(fontFamily = RobotoSlab),
        displaySmallEmphasized = base.displaySmallEmphasized.copy(fontFamily = RobotoSlab),
        headlineLargeEmphasized = base.headlineLargeEmphasized.copy(fontFamily = RobotoSlab),
        headlineMediumEmphasized = base.headlineMediumEmphasized.copy(fontFamily = RobotoSlab),
        headlineSmallEmphasized = base.headlineSmallEmphasized.copy(fontFamily = RobotoSlab),
        titleLargeEmphasized = base.titleLargeEmphasized.copy(fontFamily = RobotoSlab),
        titleMediumEmphasized = base.titleMediumEmphasized.copy(fontFamily = RobotoSlab),
        titleSmallEmphasized = base.titleSmallEmphasized.copy(fontFamily = RobotoSlab),
        bodyLarge = base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
        bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
    )
