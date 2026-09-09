package com.example.smart_build.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.max

/** Floor relative type so auth/home/search copy stays readable on phones. */
fun readableSp(maxWidth: Dp, fraction: Float, minSp: Float = 14f): TextUnit =
  max(minSp, maxWidth.value * fraction).sp
