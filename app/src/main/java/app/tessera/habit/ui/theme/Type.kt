@file:OptIn(ExperimentalTextApi::class)

package app.tessera.habit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.tessera.habit.R

/** Fraunces: display face, used only for the two largest sizes. Bundled variable font (OFL). */
val Fraunces = FontFamily(
    Font(
        R.font.fraunces,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.Setting("SOFT", 50f)
        )
    )
)

/** Libre Franklin: everything else. Bundled variable font (OFL). */
val LibreFranklin = FontFamily(
    Font(
        R.font.libre_franklin,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.libre_franklin,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    )
)

/**
 * The whole type scale: six sizes (12, 14, 16, 18, 26, 44) and two sans weights.
 * Nothing the user has to read is smaller than 12sp. No all-caps headings.
 */
object TesseraType {
    /** 44sp Fraunces. One per screen at most: the streak number, a hero stat. */
    val Display = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    )

    /** 26sp Fraunces. Screen titles and sheet titles. */
    val Headline = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    )

    /** 18sp semibold. Habit names, card titles. */
    val Title = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )

    /** 16sp. Body text, list rows, text field content. */
    val Body = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    /** 16sp semibold. Emphasised body: row values, primary button text. */
    val BodyStrong = Body.copy(fontWeight = FontWeight.SemiBold)

    /** 14sp. Supporting text under a title, descriptions, helper text. */
    val Supporting = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    /** 14sp semibold. Section headings (sentence case), chips, secondary buttons. */
    val Label = Supporting.copy(fontWeight = FontWeight.SemiBold)

    /** 12sp semibold. Heatmap axes, legends, tab labels. The smallest size allowed. */
    val Caption = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    /** 14sp monospace. Only for literal code or commands the user copies. */
    val Code = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

val TesseraTypography = Typography(
    displayLarge = TesseraType.Display,
    displayMedium = TesseraType.Display,
    displaySmall = TesseraType.Display,
    headlineLarge = TesseraType.Headline,
    headlineMedium = TesseraType.Headline,
    headlineSmall = TesseraType.Headline,
    titleLarge = TesseraType.Title,
    titleMedium = TesseraType.BodyStrong,
    titleSmall = TesseraType.Label,
    bodyLarge = TesseraType.Body,
    bodyMedium = TesseraType.Supporting,
    bodySmall = TesseraType.Caption,
    labelLarge = TesseraType.Label,
    labelMedium = TesseraType.Caption,
    labelSmall = TesseraType.Caption
)
