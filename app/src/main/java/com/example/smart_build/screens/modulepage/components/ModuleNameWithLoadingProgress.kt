package com.example.smart_build.screens.modulepage.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.components.ModuleName
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

@Composable
fun ModuleNameWithLoadingProgress(moduleName: String, maxWidth: Dp, maxHeight: Dp) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .border(1.dp, Color.Blue)
  ) {
    ModuleName(
      moduleName = moduleName,
      maxWidth = maxWidth,
      variant = 3
    )

    Spacer(modifier = Modifier.size((maxHeight.value * 0.3f).dp))

    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        "SETTING UP YOUR ENVIRONMENT...",
        style = Typography.headlineSmall.copy(fontFamily = GSFlex, fontWeight = FontWeight.Normal),
        color = White,
        fontSize = (maxWidth.value * 0.019f).sp,
        letterSpacing = ((maxWidth.value * 0.019f) * 0.12f).sp,
        lineHeight = ((maxWidth.value * 0.019f) * 1.2f).sp
      )
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
    }
  }
}