package com.example.smart_build.screens.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.data.CssPartsCatalog
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

@Composable
fun CategoryChipRow(
  selected: String,
  onSelect: (String) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CssPartsCatalog.categories.forEach { category ->
      val active = category == selected
      Text(
        text = category,
        color = if (active) White else White.copy(alpha = 0.7f),
        fontSize = 12.sp,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(
            if (active) Primary.copy(alpha = 0.35f)
            else Color(0xFF0A2438)
          )
          .border(
            BorderStroke(
              1.dp,
              if (active) Primary else Primary.copy(alpha = 0.35f)
            ),
            RoundedCornerShape(20.dp)
          )
          .clickable { onSelect(category) }
          .padding(horizontal = 12.dp, vertical = 8.dp)
      )
    }
  }
}
