package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.components.ModuleName
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.home.ModuleCardData

@Composable
fun ModuleCardDefaultContent(
  module: ModuleCardData,
  maxWidth: Dp,
  maxHeight: Dp
) {

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(
        horizontal = (maxWidth.value * 0.034f).dp,
        vertical = (maxHeight.value * 0.04f).dp
      ),
    verticalArrangement = Arrangement.Bottom
  ) {

    // --------------------------------------------------------
    // MODULE NUMBER
    // --------------------------------------------------------

    Box(
      modifier = Modifier
        .border(
          width = (maxWidth.value * 0.001f).dp,
          color = White.copy(alpha = 0.8f),
          shape = RoundedCornerShape(
            (maxWidth.value * 0.006f).dp
          )
        )
        .padding(
          horizontal = (maxWidth.value * 0.009f).dp,
          vertical = (maxHeight.value * 0.007f).dp
        )
    ) {

      Text(
        text = module.number,
        color = Color.White.copy(alpha = 0.9f),
        style = Typography.labelLarge,
        fontSize = (maxWidth.value * 0.011f).sp,
        fontWeight = FontWeight.Medium
      )
    }


    Spacer(
      modifier = Modifier.height(
        (maxHeight.value * 0.015f).dp
      )
    )


    // --------------------------------------------------------
    // TITLE
    // --------------------------------------------------------

//    Text(
//      text = module.title,
//      color = Color.White,
//      style = Typography.titleLarge,
//      fontSize = (maxWidth.value * 0.017f).sp,
//      fontWeight = FontWeight.Bold,
//      lineHeight = (maxHeight.value * 0.035f).sp
//    )

    ModuleName(
      moduleName = module.title,
      maxWidth = maxWidth
    )


    Spacer(
      modifier = Modifier.height(
        (maxHeight.value * 0.018f).dp
      )
    )


    // --------------------------------------------------------
    // DESCRIPTION
    // --------------------------------------------------------

    Text(
      text = module.description,
      color = Color.White.copy(alpha = 0.72f),
      fontSize = (maxWidth.value * 0.011f).sp,
      lineHeight = (maxHeight.value * 0.026f).sp
    )
  }
}