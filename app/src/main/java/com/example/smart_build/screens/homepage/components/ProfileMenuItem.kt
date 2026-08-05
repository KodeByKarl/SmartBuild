package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.Black

@Composable
fun ProfileMenuItem(
  icon: ImageVector,
  text: String,
  maxWidth: Dp,
  maxHeight: Dp,
  onClick: () -> Unit
) {

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        onClick()
      }
      .padding(
        horizontal = (maxWidth.value * 0.013f).dp,
        vertical = (maxHeight.value * 0.018f).dp
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {

    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Black.copy(alpha = 0.75f),
      modifier = Modifier.size((maxWidth.value * 0.017f).dp)
    )

    Spacer(
      modifier = Modifier.width((maxWidth.value * 0.011f).dp)
    )

    Text(
      text = text,
      color = Black.copy(alpha = 0.8f),
      fontSize = (maxWidth.value * 0.013f).sp,
      fontWeight = FontWeight.Medium
    )
  }
}