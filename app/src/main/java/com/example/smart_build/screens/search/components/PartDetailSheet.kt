package com.example.smart_build.screens.search.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.data.CssPart
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

@Composable
fun PartDetailSheet(
  part: CssPart,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
      .border(1.dp, Primary.copy(alpha = 0.45f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
      .background(Color(0xFF062233))
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(part.title, color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(part.category, color = Primary, fontSize = 13.sp)
      }
      TextButton(onClick = onClose) {
        Text("Close", color = Primary)
      }
    }

    Image(
      painter = painterResource(part.imageRes),
      contentDescription = part.title,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFF0A2A3C))
    )

    DetailSection("Overview", part.overview)
    DetailSection("How it works", part.howItWorks)
    DetailSection("CSS workplace use", part.cssUse)
    DetailSection("Common issues", part.commonIssues)
    DetailSection("Technician tips", part.technicianTips)
    DetailSection(
      "Related modules",
      part.relatedModules.joinToString(", ") { "Module $it" }
    )
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun DetailSection(title: String, body: String) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    Text(body, color = White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp)
  }
}
