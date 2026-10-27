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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.ui.theme.readableSp
import com.example.smart_build.tutorial.CoachController
import com.example.smart_build.tutorial.coachTarget

@Composable
fun ProgressCard(
  progress: Float,
  maxWidth: Dp,
  maxHeight: Dp,
  guidedUnlocked: Boolean = true,
  assessmentUnlocked: Boolean = false,
  showAssessment: Boolean = true,
  guidedLabel: String = "Guided Simulation",
  onGS: () -> Unit,
  onAS: () -> Unit,
  coach: CoachController? = null,
) {
  val clamped = progress.coerceIn(0f, 1f)
  val percentLabel = (clamped * 100f).toInt()

  Column(
    modifier = Modifier
      .coachTarget(coach, "card_progress")
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
        color = White.copy(alpha = 0.9f),
        fontSize = readableSp(maxWidth, 0.016f, 16f)
      )

      Box(
        contentAlignment = Alignment.Center
      ) {

        CircularProgressIndicator(
          progress = { clamped },
          modifier = Modifier.size((maxWidth.value * 0.033f).dp),
          strokeWidth = (maxWidth.value * 0.003f).dp
        )

        Text(
          text = "$percentLabel",
          style = Typography.bodyLarge.copy(fontFamily = GSFlex),
          color = White.copy(alpha = 0.9f),
          fontSize = readableSp(maxWidth, 0.009f, 15f)
        )
      }
    }


    Spacer(
      modifier = Modifier.height((maxHeight.value * 0.025f).dp)
    )


    // Guided Simulation / Continue
    Box(
      modifier = Modifier
        .coachTarget(coach, "card_gs")
        .fillMaxWidth()
        .heightIn(min = max(48.dp, (maxHeight.value * 0.09f).dp))
        .clip(RoundedCornerShape((maxWidth.value * 0.011f).dp))
        .background(if (guidedUnlocked) Color(0xFF1591C2) else Color(0xFF003247))
        .clickable(enabled = guidedUnlocked, onClick = onGS),
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
          text = guidedLabel,
          style = Typography.titleMedium,
          color = Color.White,
          fontSize = readableSp(maxWidth, 0.014f, 15f)
        )
      }
    }

    if (showAssessment) {
      Spacer(
        modifier = Modifier.height((maxHeight.value * 0.015f).dp)
      )

      val asEnabled = assessmentUnlocked
      val asAlpha = if (asEnabled) 1f else 0.35f

      Box(
        modifier = Modifier
          .coachTarget(coach, "card_as")
          .fillMaxWidth()
          .heightIn(min = max(48.dp, (maxHeight.value * 0.09f).dp))
          .clip(RoundedCornerShape((maxWidth.value * 0.011f).dp))
          .background(Color(0xFF003247))
          .clickable(enabled = asEnabled, onClick = onAS),
        contentAlignment = Alignment.Center
      ) {

        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {

          Icon(
            imageVector = if (asEnabled) Icons.Default.PlayArrow else Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White.copy(alpha = asAlpha),
            modifier = Modifier.size((maxWidth.value * 0.019f).dp)
          )

          Spacer(
            modifier = Modifier.width((maxWidth.value * 0.006f).dp)
          )

          Text(
            text = "Assessment",
            style = Typography.titleMedium,
            color = Color.White.copy(alpha = asAlpha),
            fontSize = readableSp(maxWidth, 0.014f, 15f)
          )
        }
      }
    }

  }
}