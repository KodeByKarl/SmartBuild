package com.example.smart_build.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smart_build.R

val GSFlex = FontFamily(
    Font(R.font.gsflex_black, FontWeight.Black),
    Font(R.font.gsflex_extrabold, FontWeight.ExtraBold),
    Font(R.font.gsflex_bold, FontWeight.Bold),
    Font(R.font.gsflex_semibold, FontWeight.SemiBold),
    Font(R.font.gsflex_medium, FontWeight.Medium),
    Font(R.font.gsflex_regular, FontWeight.Normal),
    Font(R.font.gsflex_light, FontWeight.Light),
    Font(R.font.gsflex_extralight, FontWeight.ExtraLight),
    Font(R.font.gsflex_thin, FontWeight.Thin)
)

val GSCode = FontFamily(
    Font(R.font.gscode_extrabold, FontWeight.ExtraBold),
    Font(R.font.gscode_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),

    Font(R.font.gscode_bold, FontWeight.Bold),
    Font(R.font.gscode_bolditalic, FontWeight.Bold, FontStyle.Italic),

    Font(R.font.gscode_semibold, FontWeight.SemiBold),
    Font(R.font.gscode_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),

    Font(R.font.gscode_medium, FontWeight.Medium),
    Font(R.font.gscode_mediumitalic, FontWeight.Medium, FontStyle.Italic),

    Font(R.font.gscode_regular, FontWeight.Normal),
    Font(R.font.gscode_italic, FontWeight.Normal, FontStyle.Italic),

    Font(R.font.gscode_light, FontWeight.Light),
    Font(R.font.gscode_lightitalic, FontWeight.Light, FontStyle.Italic)
)

val Typography = Typography (
    displayLarge = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GSCode,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    labelLarge = TextStyle(
        fontFamily = GSFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GSFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GSFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = GSFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GSFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GSFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
