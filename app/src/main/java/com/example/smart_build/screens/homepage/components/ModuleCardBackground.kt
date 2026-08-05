package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.smart_build.viewmodel.home.ModuleCardData

@Composable
fun ModuleCardBackground(
  module: ModuleCardData
) {

  // ----------------------------------------------------------
  // IMAGE
  // ----------------------------------------------------------

  if (module.image != null) {

    Image(
      painter = painterResource(module.image),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )
  }


  // ----------------------------------------------------------
  // BLUE -> BLACK GRADIENT
  // ----------------------------------------------------------

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


  // ----------------------------------------------------------
  // EXTRA DARK OVERLAY
  // ----------------------------------------------------------

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
}