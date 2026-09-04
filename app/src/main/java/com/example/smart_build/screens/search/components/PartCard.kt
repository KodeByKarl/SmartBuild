package com.example.smart_build.screens.search.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.data.CssPart
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

@Composable
fun PartCard(
  part: CssPart,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, Primary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
      .background(Color(0xFF041C2B))
      .clickable(onClick = onClick)
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(110.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Image(
        painter = painterResource(part.imageRes),
        contentDescription = part.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .width(118.dp)
          .fillMaxHeight()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0A2A3C))
      )
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(10.dp))
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF0E4F73), Color(0xFF08344F))
            )
          )
          .padding(10.dp),
        contentAlignment = Alignment.TopStart,
      ) {
        Text(
          text = part.summary,
          color = White.copy(alpha = 0.92f),
          fontSize = 11.sp,
          lineHeight = 14.sp,
          maxLines = 6,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    Text(
      text = part.title.uppercase(),
      color = White,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
    )
  }
}
