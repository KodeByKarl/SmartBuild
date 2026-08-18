package com.example.smart_build.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

// TODO:
//  Applicable for Module Cards,
//  Module Title in Module Expanded Component,
//  Module Loading Page,
//  and Module Lesson and Simulation Page.

// There will be 2 or 3 variants. First is just simple text
// in one row and pure white with smart wrap, that is for the
// Module Card. Next is bigger ModuleName and there will be
// highlighted or different font color in the name, that is
// for Module Expanded Component and Module Loading Page.
// The last is maybe just the same to the first but my plan,
// but not yet final, is there will be highlighted or different
// font color just like the second variant, like mix of variant 1 and 2.

@Composable
fun ModuleName(moduleName: String, maxWidth: Dp, variant: Int = 1) {
  val var1FontSize = (maxWidth.value * 0.017f).sp
  val var1LineHeight = (var1FontSize.value * 1.27f).sp

  val var2FontSize = (maxWidth.value * 0.034f).sp
  val var2LetterSpacing = (var2FontSize.value * 0.08f).sp
  val var2LineHeight = (var2FontSize.value * 1.2f).sp

  val var3FontSize = (maxWidth.value * 0.045f).sp
  val var3LetterSpacing = (var3FontSize.value * 0.08f).sp
  val var3LineHeight = (var3FontSize.value * 1.2f).sp

  val mn1: String = moduleName.substringBefore(" Computer")
  val mn2: String = "Computer" + moduleName.substringAfter(" Computer")

  when(variant) {
    1 -> Text(
        text = moduleName,
        color = Color.White,
        style = Typography.titleLarge,
        fontSize = var1FontSize,
        fontWeight = FontWeight.Medium,
        lineHeight = var1LineHeight
      )

    2 -> {
      Column {
        Text(
          text = mn1.uppercase(),
          color = White.copy(alpha = 0.75f),
          style = Typography.displayMedium.copy(fontFamily = GSCode),
          fontSize = var2FontSize,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = var2LetterSpacing,
          lineHeight = var2LineHeight
        )
        Text(
          text = mn2.uppercase(),
          color = Primary,
          style = Typography.displayMedium.copy(fontFamily = GSCode),
          fontSize = var2FontSize,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = var2LetterSpacing,
          lineHeight = var2LineHeight
        )
      }
    }

    3 -> {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = mn1.uppercase(),
          color = White.copy(alpha = 0.75f),
          style = Typography.displayMedium.copy(fontFamily = GSCode),
          fontSize = var3FontSize,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = var3LetterSpacing,
          lineHeight = var3LineHeight,
          softWrap = false
        )
        Text(
          text = mn2.uppercase(),
          color = Primary,
          style = Typography.displayMedium.copy(fontFamily = GSCode),
          fontSize = var3FontSize,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = var3LetterSpacing,
          lineHeight = var3LineHeight,
          softWrap = false
        )
      }
    }
  }
}