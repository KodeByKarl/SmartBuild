package com.example.smart_build.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.smart_build.R

@Composable
fun AppIcon(modifier : Modifier) {
//fun AppIcon() {
  Box(
    modifier = modifier
      .aspectRatio(1f)
  ) {
    Image(
      painter = painterResource(R.drawable.app_icon),
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
    ) // Image
  } // Box
}