package com.example.smart_build.screens.authenticationpage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smart_build.components.AppLogo
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.auth.AuthStatusState

@Composable
fun AppLogoWithLoading(
  modifier: Modifier,
  maxWidthScreen: Dp,
  gap: Float,
  uiState: AuthStatusState,
  loadingSize: Float,
  loadingStroke: Float,
  appLogoScale: Float = 1f,
  xOffSet: Dp = 0.dp
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
//      .border(1.dp, Color.Green)
      .animateContentSize()
//      .fillMaxWidth()
//      .absoluteOffset(x = xOffSet)
  ) {
    AppLogo(scale = appLogoScale, maxWidth = maxWidthScreen.value)
    AnimatedVisibility(
      visible = uiState is AuthStatusState.Loading,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column {
        Spacer(modifier = Modifier.height(gap.dp))
        CircularProgressIndicator(
          modifier = Modifier.size(loadingSize.dp),
          color = Primary,
          trackColor = White,
          strokeWidth = loadingStroke.dp
        )
      }
    }
  }
}