package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

@Composable
fun ProgressCard(
  progress: Float,
  maxWidth: Dp,
  maxHeight: Dp,
  onGS: () -> Unit,
  onAS: () -> Unit
) {

  Column(
    modifier = Modifier
      .width(
        (maxWidth.value * 0.28f).dp
      )
      .border(
        width = (maxWidth.value * 0.002f).dp,
        color = White.copy(alpha = 0.35f),
        shape = RoundedCornerShape((maxWidth.value * 0.009f).dp)
      )
      .padding((maxWidth.value * 0.013f).dp)
  ) {

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {

      Text(
        text = "Your Progress",
        style = Typography.headlineSmall.copy(fontFamily = GSFlex),
        color = White.copy(alpha = 0.75f),
        fontSize = (maxWidth.value * 0.019f).sp
      )

      Box(
        contentAlignment = Alignment.Center
      ) {

        CircularProgressIndicator(
          progress = { progress },
          modifier = Modifier.size((maxWidth.value * 0.033f).dp),
          strokeWidth = (maxWidth.value * 0.003f).dp
        )

        Text(
          text = "${(progress * 100).toInt()}",
          style = Typography.bodyLarge.copy(fontFamily = GSFlex),
          color = White.copy(alpha = 0.75f),
          fontSize = (maxWidth.value * 0.009f).sp
        )
      }
    }


    Spacer(
      modifier = Modifier.height((maxHeight.value * 0.025f).dp)
    )


    // Guided Simulation
    Box(
      modifier = Modifier
        .fillMaxWidth()
//        .height(56.dp)
        .height((maxHeight.value * 0.07f).dp)
        .clip(RoundedCornerShape((maxWidth.value * 0.011f).dp))
        .background(Color(0xFF1591C2))
        .clickable(onClick = onGS),
      contentAlignment = Alignment.Center
    ) {

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {

        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size((maxWidth.value * 0.019f).dp)
        )

        Spacer(
          modifier = Modifier.width((maxWidth.value * 0.006f).dp)
        )

        Text(
          text = "Guided Simulation",
          style = Typography.titleMedium,
          color = Color.White,
          fontSize = (maxWidth.value * 0.013f).sp
        )
      }
    }


    Spacer(
      modifier = Modifier.height((maxHeight.value * 0.015f).dp)
    )


    // Assessment

    Box(
      modifier = Modifier
        .fillMaxWidth()
//        .height(56.dp)
        .height((maxHeight.value * 0.07f).dp)
        .clip(RoundedCornerShape((maxWidth.value * 0.011f).dp))
        .background(Color(0xFF003247))
        .clickable(onClick = onAS),
      contentAlignment = Alignment.Center
    ) {

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {

        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = White.copy(alpha = 0.15f),
          modifier = Modifier.size((maxWidth.value * 0.019f).dp)
        )

        Spacer(
          modifier = Modifier.width((maxWidth.value * 0.006f).dp)
        )

        Text(
          text = "Assessment Simulation",
          style = Typography.titleMedium,
          color = White.copy(alpha = 0.15f),
          fontSize = (maxWidth.value * 0.013f).sp
        )
      }
    }
  }
}