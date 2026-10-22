package com.example.smart_build.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.data.CssPart
import com.example.smart_build.data.CssPartsCatalog
import com.example.smart_build.screens.search.components.CategoryChipRow
import com.example.smart_build.tutorial.CoachHost
import com.example.smart_build.tutorial.CoachTourEffect
import com.example.smart_build.tutorial.TutorialContent
import com.example.smart_build.tutorial.coachTarget
import com.example.smart_build.tutorial.rememberCoachController
import androidx.compose.ui.platform.LocalContext
import com.example.smart_build.screens.search.components.PartCard
import com.example.smart_build.screens.search.components.PartDetailSheet
import com.example.smart_build.screens.search.components.PartImageViewer
import com.example.smart_build.ui.theme.ThemePrefs
import com.example.smart_build.ui.theme.ScreenFg
import com.example.smart_build.ui.theme.ScreenBg
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

@Composable
fun ComponentSearchPage(navController: NavHostController) {
  var query by remember { mutableStateOf("") }
  var category by remember { mutableStateOf(CssPartsCatalog.ALL) }
  var selected by remember { mutableStateOf<CssPart?>(null) }
  var zoomed by remember { mutableStateOf<CssPart?>(null) }

  val coach = rememberCoachController()
  CoachTourEffect(
    controller = coach,
    tourId = TutorialContent.SEARCH_ID,
    steps = TutorialContent.search,
    context = LocalContext.current,
  )

  val hits = remember(query, category) {
    CssPartsCatalog.filter(query, category)
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(ScreenBg)
  ) {
    val columns = if (maxWidth > 900.dp) 3 else 2

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        IconButton(
          onClick = { navController.popBackStack() },
          modifier = Modifier.coachTarget(coach, "search_back"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = ScreenFg,
          )
        }

        OutlinedTextField(
          value = query,
          onValueChange = {
            query = it
            selected = null
          },
          modifier = Modifier
            .weight(1f)
            .coachTarget(coach, "search_field")
            .clip(RoundedCornerShape(28.dp)),
          placeholder = {
            Text("Search Components", color = ScreenFg.copy(alpha = 0.55f))
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Primary)
          },
          singleLine = true,
          shape = RoundedCornerShape(28.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ScreenFg,
            unfocusedTextColor = ScreenFg,
            focusedBorderColor = Primary,
            unfocusedBorderColor = Primary.copy(alpha = 0.45f),
            focusedContainerColor = if (ThemePrefs.lightMode) Color.White else Color(0xFF0A3A55),
            unfocusedContainerColor = if (ThemePrefs.lightMode) Color.White else Color(0xFF072C42),
            cursorColor = Primary,
          ),
        )
      }

      Text(
        text = "CSS Parts Encyclopedia",
        color = ScreenFg,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
      )
      Text(
        text = "Hardware · networks · cabling · servers · internet & cloud",
        color = ScreenFg.copy(alpha = 0.65f),
        fontSize = 14.sp,
      )

      Box(modifier = Modifier.coachTarget(coach, "search_chips")) {
      CategoryChipRow(
        selected = category,
        onSelect = {
          category = it
          selected = null
        },
      )
      }

      if (hits.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No parts found. Try another name or category.",
            color = Color(0xFFFFB074),
          )
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(columns),
          modifier = Modifier.weight(1f).coachTarget(coach, "search_grid"),
          contentPadding = PaddingValues(bottom = if (selected != null) 280.dp else 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(hits, key = { it.id }) { part ->
            PartCard(
              part = part,
              onClick = { selected = part },
              onImageClick = { zoomed = part },
            )
          }
        }
      }
    }

    selected?.let { part ->
      PartDetailSheet(
        part = part,
        onClose = { selected = null },
        onImageClick = { zoomed = part },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .height(maxHeight * 0.62f),
      )
    }

    zoomed?.let { part ->
      PartImageViewer(part = part, onDismiss = { zoomed = null })
    }

    CoachHost(coach)
  }
}
