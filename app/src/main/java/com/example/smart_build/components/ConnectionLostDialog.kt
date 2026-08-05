package com.example.smart_build.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.Typography

@Composable
fun ConnectionLostDialog(maxWidth: Dp, maxHeight: Dp) {
  AlertDialog(
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      dismissOnBackPress = false,
      dismissOnClickOutside = false
    ),
    onDismissRequest = {},
    icon = {
      Icon(
        imageVector = Icons.Default.LinkOff,
        contentDescription = null,
        tint = Black.copy(alpha = 0.6f),
        modifier = Modifier.size((maxWidth.value * 0.019f).dp)
      )
    },
    title = {
      Text(
        "Connection Lost",
        style = Typography.headlineSmall.copy(
          color = Black,
//          fontSize = (maxWidth.value * 0.019f).sp,
          fontWeight = FontWeight.Normal
        )
      )
    },
    text = {
      Text(
        "Unable to connect to the internet. Please check your connection and try again.",
        modifier = Modifier.fillMaxWidth(),
        style = Typography.bodyMedium.copy(
          color = Black.copy(alpha = 0.7f),
//          fontSize = (maxWidth.value * 0.011f).sp,
          textAlign = TextAlign.Center
        )
      )
    },
    confirmButton = {},
    containerColor = Color(0xFFD7ECFF),
    modifier = Modifier
//      .widthIn(min = 312.dp, max = 560.dp)
//      .width((maxWidth.value * 0.4f).dp)
//      .padding(horizontal = (maxWidth.value * 0.019f).dp, vertical = (maxHeight.value * 0.03f).dp)
//      .width(100.dp)
      .width(IntrinsicSize.Min)
//      .height(IntrinsicSize.Min)
  )
}