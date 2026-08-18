package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smart_build.viewmodel.home.ModuleCardData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModuleCarousel(
  modules: List<ModuleCardData>,
  maxWidthh: Dp,
  maxHeightt: Dp,
  modifier: Modifier = Modifier
) {
  if (modules.isEmpty()) return

  BoxWithConstraints(
    modifier = modifier
//      .border(1.dp, Color.Red)
      .fillMaxWidth()
      .fillMaxHeight(0.9f)
  ) {

    // Approximately matches the proportions of your screenshot.
    val cardWidth = maxWidth * 0.78125f
    val sidePadding = (maxWidth - cardWidth) / 2

    val pagerState = rememberPagerState(
      initialPage = 0,
      pageCount = { modules.size }
    )

    HorizontalPager (
      state = pagerState,
      pageSize = PageSize.Fixed(cardWidth),
//      pageSpacing = 60.dp,
      pageSpacing = (maxWidth.value * 0.047f).dp,
      contentPadding = androidx.compose.foundation.layout.PaddingValues(
        horizontal = sidePadding
      ),
      modifier = Modifier.fillMaxWidth()
    ) { page ->

      val pageOffset =
        (pagerState.currentPage - page) +
            pagerState.currentPageOffsetFraction

      val isCurrentPage = page == pagerState.currentPage

      ModuleCard(
        module = modules[page],
        isCurrentPage = isCurrentPage,
        maxWidth = maxWidthh,
        maxHeight = maxHeightt,
        onGS = modules[page].onGS,
        onAS = modules[page].onAS,
        modifier = Modifier
          .graphicsLayer {

            // Slightly shrink cards that aren't in the center.
            val scale = if (isCurrentPage) {
              1f
            } else {
              0.95f
            }

            scaleX = scale
            scaleY = scale

            // Fade the cards on the sides.
            alpha = if (isCurrentPage) {
              1f
            } else {
              0.45f
            }
          }
          .then(
            if (!isCurrentPage) {
              Modifier.blur(2.dp)
            } else {
              Modifier
            }
          )
      )
    }
  }
}