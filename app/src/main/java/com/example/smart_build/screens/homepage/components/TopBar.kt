package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.R
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

@Composable
fun TopBar(maxWidth: Dp, maxHeight: Dp, searchBarOnClick: () -> Unit, iconButtonOnClick: () -> Unit) {

  val appTitleLetterSpacing: Float = (maxWidth.value * 0.002f) // max width * 0.7%
  val appTitleLineHeight: Float = (maxWidth.value * 0.045f) // max width * 4.1%
  val appTitleFontSize: Float = (maxWidth.value * 0.022f) // max width * 3.4%

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
//      .height((maxHeight.value * 0.06f).dp)
      .height((maxHeight.value * 0.11f).dp)
      .padding(horizontal = (maxWidth.value * 0.019f).dp, vertical = (maxHeight.value * 0.025f).dp)
  ) {
    Row {
      Image(
        painter = painterResource(R.drawable.app_icon),
        contentDescription = "App Logo",
        modifier = Modifier
          .size((maxWidth.value * 0.038f).dp)
      )
      Spacer(modifier = Modifier.size((maxWidth.value * 0.009f).dp))
      Row {
        Text(
          "SMART",
          color = White,
          letterSpacing = appTitleLetterSpacing.sp,
          lineHeight = appTitleLineHeight.sp,
          style = Typography.displaySmall.copy(
            fontSize = appTitleFontSize.sp,
            fontWeight = FontWeight.Black
          )
        ) // Text
        Spacer(modifier = Modifier.size(appTitleLetterSpacing.dp))
        Text(
          "BUILD",
          color = Primary,
          letterSpacing = appTitleLetterSpacing.sp,
          lineHeight = appTitleLineHeight.sp,
          style = Typography.displaySmall.copy(
            fontSize = appTitleFontSize.sp,
            fontWeight = FontWeight.Black
          )
        ) // Text
      } // Row
    } // Row
    Row() {
      Button(
        onClick = searchBarOnClick,
        border = BorderStroke(width = (maxHeight.value * 0.003f).dp, color = White.copy(alpha = 0.32f)),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = (maxWidth.value * 0.006f).dp, vertical = (maxHeight.value * 0.01f).dp),
        modifier = Modifier
          .height(IntrinsicSize.Min)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(horizontal = (maxWidth.value * 0.009f).dp, vertical = (maxHeight.value * 0.005f).dp)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = White.copy(alpha = 0.32f),
            modifier = Modifier.size((maxWidth.value * 0.019f).dp)
          )
          Spacer(Modifier.size((maxWidth.value * 0.006f).dp))
          Text(
            "Start exploring components",
            style = Typography.titleMedium.copy(
              color = White.copy(alpha = 0.32f),
              fontSize = (maxWidth.value * 0.013f).sp,
              fontWeight = FontWeight.Medium
            )
          )
        } // Row
      } // Button
      Spacer(Modifier.size((maxWidth.value * 0.009f).dp))
      IconButton(
        onClick = iconButtonOnClick,
        shape = CircleShape,
        colors = IconButtonDefaults.iconButtonColors(
          containerColor = Color(0xFFD7ECFF)
        ),
        modifier = Modifier.size((maxWidth.value * 0.038f).dp)
      ) {
        Icon(
          imageVector = Icons.Default.AccountCircle,
          contentDescription = null,
          tint = Black.copy(alpha = 0.8f),
          modifier = Modifier.size((maxWidth.value * 0.031f).dp)
        )
      } // IconButton
    } // Row
  }
}