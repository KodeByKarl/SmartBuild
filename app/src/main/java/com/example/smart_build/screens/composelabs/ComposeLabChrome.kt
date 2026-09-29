package com.example.smart_build.screens.composelabs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White
import com.example.smart_build.ui.theme.labSp

/**
 * Playground-first chrome for Compose labs (M2–M4).
 * Interaction / container / bench get most of the viewport; guides & buttons stay slim.
 */
@Composable
fun ComposeLabScaffold(
  navController: NavHostController,
  eyebrow: String,
  title: String,
  progressLabel: String,
  coachTitle: String,
  coachBody: String,
  status: String,
  statusOk: Boolean = false,
  primaryLabel: String,
  onPrimary: () -> Unit,
  primaryEnabled: Boolean = true,
  onReset: (() -> Unit)? = null,
  showCoach: Boolean = true,
  nextStep: String = coachTitle,
  onComplete: (() -> Unit)? = null,
  completeLabel: String = "Continue ▶",
  embedded: Boolean = false,
  onBack: (() -> Unit)? = null,
  goal: String = "",
  content: @Composable BoxScope.() -> Unit,
) {
  fun goBack() {
    if (onBack != null) onBack()
    else if (!navController.popBackStack()) navController.navigateUp()
  }

  val showBack = !embedded || onBack != null
  if (showBack) {
    BackHandler { goBack() }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(listOf(Color(0xFF022A3A), Black, Color(0xFF011018))),
      ),
  ) {
    val maxW = maxWidth
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          horizontal = (maxW.value * 0.025f).coerceIn(8f, 14f).dp,
          vertical = 6.dp,
        ),
    ) {
      // —— Slim header (brand + progress only) ——
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (showBack) {
          IconButton(
            onClick = { goBack() },
            modifier = Modifier.size(32.dp),
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = White,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Text(
          text = eyebrow,
          color = Primary,
          fontFamily = GSCode,
          fontWeight = FontWeight.Bold,
          fontSize = labSp(maxW, 0.015f, 11f, 13f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Text(
          text = progressLabel,
          color = White.copy(alpha = 0.75f),
          fontFamily = GSFlex,
          fontSize = labSp(maxW, 0.013f, 10f, 12f),
        )
      }
      Text(
        text = title,
        color = White.copy(alpha = 0.5f),
        fontFamily = GSFlex,
        fontSize = labSp(maxW, 0.012f, 10f, 11f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = if (showBack) 32.dp else 0.dp),
      )

      Spacer(modifier = Modifier.height(4.dp))

      if (goal.isNotBlank()) {
        Text(
          text = "Goal: $goal",
          color = White,
          fontFamily = GSFlex,
          fontWeight = FontWeight.SemiBold,
          fontSize = labSp(maxW, 0.014f, 12f, 13f),
          lineHeight = labSp(maxW, 0.018f, 16f, 18f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        )
      }
      val guideLine = when {
        showCoach -> buildString {
          append(coachTitle)
          if (coachBody.isNotBlank()) {
            append(" ")
            append(coachBody)
          }
        }
        nextStep.isNotBlank() -> "Now: $nextStep"
        else -> ""
      }
      if (guideLine.isNotBlank()) {
        Text(
          text = guideLine,
          color = if (showCoach) Color(0xFFE8B84A) else White.copy(alpha = 0.75f),
          fontFamily = GSFlex,
          fontWeight = FontWeight.Medium,
          fontSize = labSp(maxW, 0.013f, 11f, 12f),
          lineHeight = labSp(maxW, 0.017f, 15f, 17f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // —— PLAYGROUND (priority) ——
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF071A22))
          .border(1.dp, Primary.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
        content = content,
      )

      Spacer(modifier = Modifier.height(4.dp))

      // —— Compact footer ——
      Text(
        text = status,
        color = if (statusOk) Color(0xFF2ECC71) else White.copy(alpha = 0.8f),
        fontFamily = GSFlex,
        fontSize = labSp(maxW, 0.012f, 10f, 11f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (onReset != null) {
          LabChromeButton(
            label = "Reset",
            modifier = Modifier.weight(0.28f),
            background = Color(0xFF003247),
            onClick = onReset,
          )
        }
        LabChromeButton(
          label = if (statusOk) {
            if (onComplete != null) completeLabel else "Back to Home"
          } else {
            primaryLabel
          },
          modifier = Modifier.weight(if (onReset != null) 0.72f else 1f),
          background = when {
            !primaryEnabled -> Color(0xFF003247)
            statusOk -> Color(0xFF1E8449)
            else -> Primary
          },
          enabled = primaryEnabled,
          onClick = {
            if (statusOk) {
              if (onComplete != null) onComplete() else goBack()
            } else {
              onPrimary()
            }
          },
        )
      }
    }
  }
}

@Composable
fun LabChromeButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  background: Color = Primary,
  enabled: Boolean = true,
) {
  val interaction = remember { MutableInteractionSource() }
  Box(
    modifier = modifier
      .height(40.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(if (enabled) background else Color(0xFF003247))
      .clickable(
        enabled = enabled,
        interactionSource = interaction,
        indication = null,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color = White,
      fontFamily = GSCode,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun StepList(steps: List<String>, currentIndex: Int, allDone: Boolean) {
  steps.forEachIndexed { index, label ->
    val done = allDone || index < currentIndex
    val current = !allDone && index == currentIndex
    Text(
      text = "${when {
        done -> "✓"
        current -> "▶"
        else -> "·"
      }}  $label",
      color = when {
        done -> Color(0xFF2ECC71)
        current -> Color(0xFFE8B84A)
        else -> White.copy(alpha = 0.45f)
      },
      fontFamily = GSFlex,
      fontSize = 12.sp,
      modifier = Modifier.padding(vertical = 1.dp),
    )
  }
}
