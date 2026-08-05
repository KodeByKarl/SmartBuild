package com.example.smart_build.screens.homepage.components

import android.text.Layout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

@Composable
fun BenefitItem(
  text: String,
  maxWidth: Dp
) {

  Row(
    verticalAlignment = Alignment.CenterVertically
  ) {

    Text(
      text = "•",
      style = Typography.headlineMedium.copy(fontFamily = GSFlex),
      color = White.copy(alpha = 0.65f),
      fontSize = (maxWidth.value * 0.017f).sp
    )

    Spacer(
      modifier = Modifier.width((maxWidth.value * 0.009f).dp)
    )

    Text(
      text = text,
      style = Typography.headlineMedium.copy(fontFamily = GSFlex),
      color = White.copy(alpha = 0.70f),
      fontSize = (maxWidth.value * 0.017f).sp
    )
  }
}