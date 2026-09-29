package com.example.smart_build.screens.composemodule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

@Composable
fun ScenarioBriefScreen(
  scenario: AssessmentScenario,
  onBegin: () -> Unit,
  onBack: () -> Unit = {},
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(Color(0xFF022A3A), Black)))
      .padding(20.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    Text(
      text = "Back",
      color = White,
      fontFamily = GSFlex,
      fontWeight = FontWeight.SemiBold,
      fontSize = 15.sp,
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable(onClick = onBack)
        .padding(vertical = 6.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "SCENARIO ASSESSMENT · Module ${scenario.moduleId}",
      color = Primary,
      fontFamily = GSCode,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Not Guided Simulation — identify faults, then fix without yellow coaches.",
      color = White.copy(alpha = 0.7f),
      fontFamily = GSFlex,
      fontSize = 15.sp,
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = scenario.title,
      color = Color(0xFFE8B84A),
      fontFamily = GSCode,
      fontWeight = FontWeight.SemiBold,
      fontSize = 20.sp,
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text("Situation", color = White.copy(alpha = 0.7f), fontFamily = GSFlex, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Text(scenario.situation, color = White, fontFamily = GSFlex, fontSize = 17.sp, lineHeight = 24.sp)

    if (scenario.faults.isNotEmpty()) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        "Faults to identify & correct",
        color = White.copy(alpha = 0.7f),
        fontFamily = GSFlex,
        fontSize = 13.sp,
      )
      Spacer(modifier = Modifier.height(8.dp))
      scenario.faults.forEach { fault ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF3A1515))
            .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        ) {
          Text("⚠  $fault", color = White, fontFamily = GSFlex, fontSize = 14.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Your goal", color = White.copy(alpha = 0.7f), fontFamily = GSFlex, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Text(scenario.goal, color = Color(0xFF2ECC71), fontFamily = GSFlex, fontSize = 15.sp)

    Spacer(modifier = Modifier.height(12.dp))
    Text(
      "No yellow coach on this path — apply TESDA CSS NC II procedures from Guided practice.",
      color = White.copy(alpha = 0.55f),
      fontFamily = GSFlex,
      fontSize = 13.sp,
    )

    Spacer(modifier = Modifier.height(28.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Primary)
        .clickable(onClick = onBegin),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        "Begin scenario ▶",
        color = White,
        fontFamily = GSCode,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
      )
    }
  }
}
