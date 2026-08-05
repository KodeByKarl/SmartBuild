package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.viewmodel.home.ModuleCardData

@Composable
fun ModuleCard(
  module: ModuleCardData,
  isCurrentPage: Boolean,
  maxWidth: Dp,
  maxHeight: Dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape((maxWidth.value * 0.022f).dp)

  Box(
    modifier = modifier
      .clickable {
        onClick()
      }
//      .fillMaxWidth()
      .width((maxWidth.value * 0.781f).dp)
//      .height(550.dp)
      .height((maxHeight.value * 0.693f).dp)
      .clip(shape)
      .border(
        width = (maxWidth.value * 0.002f).dp,
//        color = Color(0xFF008FC5),
        color = Primary,
        shape = shape
      )
  ) {

    // ----------------------------------------------------
    // BACKGROUND IMAGE
    // ----------------------------------------------------

    if (module.image != null) {

      Image(
        painter = painterResource(module.image),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    }


    // ----------------------------------------------------
    // BLUE -> TRANSPARENT -> BLACK GRADIENT
    //
    // This makes the image gradually disappear into black
    // at the bottom, similar to your screenshot.
    // ----------------------------------------------------

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF14A9E0).copy(alpha = 0.90f),
              Color(0xFF087AA4).copy(alpha = 0.75f),
              Color(0xFF00354A).copy(alpha = 0.82f),
              Color(0xFF001A27).copy(alpha = 0.97f),
              Color(0xFF001923)
            )
          )
        )
    )


    // ----------------------------------------------------
    // EXTRA DARK OVERLAY
    //
    // Makes the lower portion of the image darker.
    // ----------------------------------------------------

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              Color.Transparent,
              Color(0xFF001923).copy(alpha = 0.35f),
              Color(0xFF001923).copy(alpha = 0.90f)
            )
          )
        )
    )


    // ----------------------------------------------------
    // CARD CONTENT
    // ----------------------------------------------------

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          horizontal = (maxWidth.value * 0.034f).dp,
          vertical = (maxHeight.value * 0.04f).dp
        ),
      verticalArrangement = Arrangement.Bottom
    ) {

      // Module number
      Box(
        modifier = Modifier
          .border(
            width = (maxWidth.value * 0.001f).dp,
            color = Color.White.copy(alpha = 0.8f),
            shape = RoundedCornerShape((maxWidth.value * 0.006f).dp)
          )
          .padding(
            horizontal = (maxWidth.value * 0.009f).dp,
            vertical = (maxHeight.value * 0.008f).dp
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
        modifier = Modifier.height((maxHeight.value * 0.015f).dp)
      )

      // Title
      Text(
        text = module.title,
        color = Color.White,
        style = Typography.titleLarge,
        fontSize = (maxWidth.value * 0.017f).sp,
        fontWeight = FontWeight.Bold,
        lineHeight = (maxHeight.value * 0.035f).sp
      )

      Spacer(
        modifier = Modifier.height((maxHeight.value * 0.018f).dp)
      )

      // Description
      Text(
        text = module.description,
        color = Color.White.copy(alpha = 0.72f),
        fontSize = (maxWidth.value * 0.011f).sp,
        lineHeight = (maxHeight.value * 0.026f).sp
      )
    }
  }
}