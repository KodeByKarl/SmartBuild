package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.home.ModuleCardData

@Composable
fun ModuleCardExpanded(
  module: ModuleCardData,
  maxWidth: Dp,
  maxHeight: Dp,
  onBack: () -> Unit
) {

  val shape = RoundedCornerShape(
    bottomStart = (maxWidth.value * 0.022f).dp,
    bottomEnd = (maxWidth.value * 0.022f).dp
  )


  Column(
    modifier = Modifier
      .verticalScroll(
        rememberScrollState()
      )
      .fillMaxSize()
      .background(
        Color(0xFF001923)
      )
  ) {


    // ========================================================
    // EXPANDED HEADER
    // ========================================================

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(
          (maxHeight.value * 0.693f).dp
        )
        .clip(shape)
        .border(
          width = (maxWidth.value * 0.002f).dp,
          color = Primary,
          shape = shape
        )
    ) {

      // ------------------------------------------------------
      // IMAGE + GRADIENT
      // ------------------------------------------------------

      ModuleCardBackground(
        module = module
      )


      // ------------------------------------------------------
      // BACK BUTTON
      // ------------------------------------------------------

      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(
            top = (maxHeight.value * 0.04f).dp,
            end = (maxWidth.value * 0.034f).dp
          )
          .border(
            width = (maxWidth.value * 0.002f).dp,
            color = Color.White.copy(alpha = 0.55f),
            shape = RoundedCornerShape((maxWidth.value * 0.013f).dp)
          )
          .clickable {
            onBack()
          }
          .padding(
            horizontal = (maxWidth.value * 0.017f).dp,
            vertical = (maxHeight.value * 0.018f).dp
          )
      ) {

        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {

          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = White.copy(alpha = 0.75f),
            modifier = Modifier.size((maxWidth.value * 0.016f).dp)
          )

          Spacer(
            modifier = Modifier.width((maxWidth.value * 0.006f).dp)
          )

          Text(
            text = "Back",
            style = Typography.titleMedium,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = (maxWidth.value * 0.011f).sp
          )
        }
      }


      // ------------------------------------------------------
      // TITLE CONTENT
      // ------------------------------------------------------

      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(
            horizontal = (maxWidth.value * 0.034f).dp,
            vertical = (maxHeight.value * 0.04f).dp
          )
      ) {

        // Module number

        Box(
          modifier = Modifier
            .border(
              width = (maxWidth.value * 0.002f).dp,
              color = Color.White.copy(alpha = 0.8f),
              shape = RoundedCornerShape((maxWidth.value * 0.006f).dp)
            )
            .padding(
              horizontal = (maxWidth.value * 0.009f).dp,
              vertical = (maxHeight.value * 0.008f).dp
            )
        ) {

          Text(
            text = module.number,
            style = Typography.labelLarge.copy(fontFamily = GSCode),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = (maxWidth.value * 0.011f).sp,
            fontWeight = FontWeight.Medium
          )
        }


        Spacer(
          modifier = Modifier.height((maxHeight.value * 0.015f).dp)
        )


        // Title

        Text(
          text = module.title,
          color = Color.White,
          style = Typography.displayMedium.copy(fontFamily = GSCode),
          fontSize = (maxWidth.value * 0.034f).sp,
          fontWeight = FontWeight.ExtraBold,
          lineHeight = (maxHeight.value * 0.05f).sp
        )


        Spacer(
          modifier = Modifier.height((maxHeight.value * 0.018f).dp)
        )


        // Description

        Text(
          text = module.description,
          color = Color.White.copy(alpha = 0.72f),
          fontSize = (maxWidth.value * 0.0125f).sp,
          lineHeight = (maxHeight.value * 0.03f).sp
        )
      }
    }


    // ========================================================
    // CONTENT BELOW THE CARD
    // ========================================================

    Spacer(Modifier.height((maxHeight.value * 0.075f).dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = (maxWidth.value * 0.034f).dp,
          vertical = (maxHeight.value * 0.035f).dp
        ),
      horizontalArrangement = Arrangement.spacedBy(
        (maxWidth.value * 0.035f).dp
      ),
      verticalAlignment = Alignment.Top
    ) {


      // ======================================================
      // LEFT CONTENT
      // ======================================================

      Column(
        modifier = Modifier.weight(1f)
      ) {

        Text(
          text = "Hello there!",
          style = Typography.headlineMedium.copy(fontFamily = GSFlex),
          color = Color.White.copy(alpha = 0.72f),
          fontSize = (maxWidth.value * 0.022f).sp
        )


        Spacer(
          modifier = Modifier.height(
            (maxHeight.value * 0.01f).dp
          )
        )


        Text(
          text =
            "Welcome to the very first of the core modules " +
                "of this course, the ${module.title}.",
          style = Typography.headlineMedium.copy(fontFamily = GSFlex),

          color = Color.White.copy(alpha = 0.72f),

          fontSize =
            (maxWidth.value * 0.021f).sp,

          lineHeight =
            (maxHeight.value * 0.055f).sp
        )


        Spacer(
          modifier = Modifier.height(
            (maxHeight.value * 0.055f).dp
          )
        )


        Text(
          text =
            "At the end of this introductory, you will be able to:",

          style = Typography.headlineMedium.copy(fontFamily = GSFlex),
          color =
            Color.White.copy(alpha = 0.72f),

          fontSize =
            (maxWidth.value * 0.021f).sp,

          fontWeight =
            FontWeight.Medium
        )


        Spacer(
          modifier = Modifier.height((maxHeight.value * 0.013f).dp)
        )


        BenefitItem("Benefit 1", maxWidth = maxWidth)
        BenefitItem("Benefit 2", maxWidth = maxWidth)
        BenefitItem("Benefit 3", maxWidth = maxWidth)
        BenefitItem("Benefit 4", maxWidth = maxWidth)
        BenefitItem("Benefit 5", maxWidth = maxWidth)
      }


      // ======================================================
      // RIGHT PROGRESS CARD
      // ======================================================

      if (module.number == "Module 0") {
        Box(
          modifier = Modifier
            .width(
              (maxWidth.value * 0.28f).dp
            )
            .height((maxHeight.value * 0.07f).dp)
            .clip(
              RoundedCornerShape((maxWidth.value * 0.011f).dp)
            )
            .background(
              Color(0xFF1591C2)
            )
            .clickable {
              // Start...
            },
          contentAlignment = Alignment.Center
        ) {

          Row(
            verticalAlignment =
              Alignment.CenterVertically
          ) {

            Text(
              text = "Start",
              color = Color.White,
              fontSize = (maxWidth.value * 0.013f).sp,
              fontWeight = FontWeight.Medium
            )

            Spacer(
              modifier = Modifier.width((maxWidth.value * 0.006f).dp)
            )

            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = "Start",
              tint = White,
              modifier = Modifier.size((maxWidth.value * 0.016f).dp)
            )
          }
        }
      } else {
        ProgressCard(
          progress = 0.25f,
          maxWidth = maxWidth,
          maxHeight = maxHeight
        )
      }
    }
  }
}