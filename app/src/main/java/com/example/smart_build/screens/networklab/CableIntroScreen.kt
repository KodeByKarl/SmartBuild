package com.example.smart_build.screens.networklab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.screens.composelabs.ComposeLabScaffold
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.White

@Composable
fun CableIntroScreen(
  navController: NavHostController,
  embedded: Boolean = false,
  onLeave: (() -> Unit)? = null,
  onContinue: () -> Unit,
) {
  ComposeLabScaffold(
    navController = navController,
    eyebrow = "CABLE STANDARDS · Before you crimp",
    title = "Module 2 · straight-through vs crossover",
    progressLabel = "Intro",
    coachTitle = "DO THIS · Read then continue",
    coachBody = "Choose the right cable for the link before termination.",
    nextStep = "Read the cards, then continue to the crimping bench",
    status = "Continue when you know both cable types.",
    statusOk = true,
    primaryLabel = "Continue to crimp ▶",
    onPrimary = onContinue,
    showCoach = true,
    embedded = embedded,
    onBack = onLeave,
    onComplete = onContinue,
    completeLabel = "Continue to crimp ▶",
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CABLE_INTRO_POINTS.forEach { point ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0A3A4A))
            .border(1.dp, Color(0xFFE8B84A).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        ) {
          Column {
            Text(
              point.title,
              color = Color(0xFFE8B84A),
              fontFamily = GSCode,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(point.body, color = White, fontFamily = GSFlex, fontSize = 13.sp, lineHeight = 18.sp)
          }
        }
      }
    }
  }
}
