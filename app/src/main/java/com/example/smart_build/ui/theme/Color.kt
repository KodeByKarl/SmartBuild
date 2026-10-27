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

/** Shared module/lab surfaces. These are getters so Compose observes ThemePrefs. */
val ModuleTopBg: Color get() = if (ThemePrefs.lightMode) Color(0xFFDCECF7) else Color(0xFF022A3A)
val ModuleBottomBg: Color get() = if (ThemePrefs.lightMode) LightBackground else Color(0xFF011018)
val ModuleSurface: Color get() = if (ThemePrefs.lightMode) Color(0xFFFFFFFF) else Color(0xFF071A22)
val ModuleSubtleSurface: Color get() = if (ThemePrefs.lightMode) Color(0xFFD9ECF7) else Color(0xFF0A3A4A)
val ModuleMutedFg: Color get() = ScreenFg.copy(alpha = if (ThemePrefs.lightMode) 0.68f else 0.75f)
