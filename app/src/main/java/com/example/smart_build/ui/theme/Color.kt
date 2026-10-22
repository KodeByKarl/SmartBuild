package com.example.smart_build.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF17A6DF)
val Black = Color(0xFF011723)
val White = Color(0xFFF3F6FA)

/** Light-mode page background (white with a blue tint). */
val LightBackground = Color(0xFFEAF4FC)

/** Screen background that follows the app-wide Light / Dark choice. Read inside composition. */
val ScreenBg: Color get() = if (ThemePrefs.lightMode) LightBackground else Black

/** Text/icon colour that reads on [ScreenBg]. */
val ScreenFg: Color get() = if (ThemePrefs.lightMode) Black else White
