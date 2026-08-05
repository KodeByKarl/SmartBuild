package com.example.smart_build.screens.homepage.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.viewmodel.home.ModuleCardData


@Composable
fun ModuleCard1(
  module: ModuleCardData,
  isCurrentPage: Boolean,
  maxWidth: Dp,
  maxHeight: Dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {

  var expanded by remember {
    mutableStateOf(false)
  }


  // ==========================================================
  // DEFAULT CARD
  // ==========================================================

  Box(
    modifier = modifier
      .clickable {

        expanded = true

        // Keep your existing callback if you need it.
        onClick()
      }
      .width((maxWidth.value * 0.781f).dp)
      .height((maxHeight.value * 0.693f).dp)
      .clip(
        RoundedCornerShape(
          (maxWidth.value * 0.022f).dp
        )
      )
      .border(
        width = (maxWidth.value * 0.002f).dp,
        color = Primary,
        shape = RoundedCornerShape(
          (maxWidth.value * 0.022f).dp
        )
      )
  ) {

    ModuleCardBackground(
      module = module
    )

    ModuleCardDefaultContent(
      module = module,
      maxWidth = maxWidth,
      maxHeight = maxHeight
    )
  }


  // ==========================================================
  // EXPANDED STATE
  // ==========================================================

  if (expanded) {

    Dialog(
      onDismissRequest = {
        expanded = false
      },
      properties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
      )
    ) {

      // Android back button
      BackHandler {
        expanded = false
      }


      ModuleCardExpanded(
        module = module,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        onBack = {
          expanded = false
        }
      )
    }
  }
}