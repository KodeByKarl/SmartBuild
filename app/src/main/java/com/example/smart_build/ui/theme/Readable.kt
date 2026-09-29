package com.example.smart_build.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/** Floor relative type so auth/home/search/labs stay readable on phones (panelist: text too small). */
fun readableSp(maxWidth: Dp, fraction: Float, minSp: Float = 17f): TextUnit =
  max(minSp, maxWidth.value * fraction).sp

/** Line height from the font size, so landscape height cannot crush lines together. */
fun TextUnit.lineGap(factor: Float = 1.45f): TextUnit = (value * factor).sp

/**
 * Lab chrome type — keeps a phone floor but **caps** size so landscape / tablets
 * do not bury the simulation under giant DO THIS / header text (M2 panelist).
 */
fun labSp(
  maxWidth: Dp,
  fraction: Float,
  minSp: Float = 12f,
  maxSp: Float = 15f,
): TextUnit = min(maxSp, max(minSp, maxWidth.value * fraction)).sp
