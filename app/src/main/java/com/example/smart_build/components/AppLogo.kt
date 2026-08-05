package com.example.smart_build.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

@Composable
fun AppLogo(scale: Float = 1f, xSet: Dp = 0.dp, maxWidth: Float) {
  val appIconSize: Float = (maxWidth * 0.22f) * scale // max width * 22%

  val spacerSize: Float = (maxWidth * 0.03f) * scale // max width * 3%

  val appTitleLetterSpacing: Float = (maxWidth * 0.007f) * scale // max width * 0.7%
  val appTitleLineHeight: Float = (maxWidth * 0.041f) * scale // max width * 4.1%
  val appTitleFontSize: Float = (maxWidth * 0.034f) * scale // max width * 3.4%

  val appHeadlineFontSize: Float = (maxWidth * 0.013f) * scale // max width * 1.3%
  val appHeadlineLetterSpacing: Float = (maxWidth * 0.004f) * scale // max width * 0.4%

  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .offset(xSet)
  ) {
    AppIcon(modifier = Modifier.width(appIconSize.dp)) // AppIcon
//    AppIcon() // AppIcon
    Spacer(modifier = Modifier.size(spacerSize.dp)) // Spacer
    Row {
      Text(
        "SMART",
        color = White,
        letterSpacing = appTitleLetterSpacing.sp,
        lineHeight = appTitleLineHeight.sp,
        style = Typography.displaySmall.copy(
          fontSize = appTitleFontSize.sp,
          fontWeight = FontWeight.Black
        )
      ) // Text
      Spacer(modifier = Modifier.size(appTitleLetterSpacing.dp))
      Text(
        "BUILD",
        color = Primary,
        letterSpacing = appTitleLetterSpacing.sp,
        lineHeight = appTitleLineHeight.sp,
        style = Typography.displaySmall.copy(
          fontSize = appTitleFontSize.sp,
          fontWeight = FontWeight.Black
        )
      ) // Text
    } // Row
    Text(
      "VIRTUAL COMPUTER SYSTEMS SERVICING",
      style = Typography.headlineSmall.copy(
        fontSize = appHeadlineFontSize.sp,
        fontWeight = FontWeight.Normal
      ),
      color = White.copy(alpha = 0.75f),
      letterSpacing = appHeadlineLetterSpacing.sp
    ) // Text
  }
}