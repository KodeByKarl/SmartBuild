package com.example.smart_build.screens.search.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_build.data.CssPart
import com.example.smart_build.ui.theme.White
import kotlin.math.max

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 2.5f

/**
 * Fullscreen, image-only view of a part. Pinch or double-tap to zoom, drag to pan
 * while zoomed, single tap or back to close.
 */
@Composable
fun PartImageViewer(
  part: CssPart,
  onDismiss: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false,
    ),
  ) {
    var scale by remember { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    // Keep the image edges from being dragged past the middle of the screen.
    fun clampOffset(candidate: Offset, atScale: Float): Offset {
      val maxX = max(0f, (viewport.width * atScale - viewport.width) / 2f)
      val maxY = max(0f, (viewport.height * atScale - viewport.height) / 2f)
      return Offset(
        candidate.x.coerceIn(-maxX, maxX),
        candidate.y.coerceIn(-maxY, maxY),
      )
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xF2011723))
        .onSizeChanged { viewport = it }
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = { if (scale <= MIN_ZOOM) onDismiss() },
            onDoubleTap = { tap ->
              if (scale > MIN_ZOOM) {
                scale = MIN_ZOOM
                offset = Offset.Zero
              } else {
                val center = Offset(viewport.width / 2f, viewport.height / 2f)
                offset = clampOffset((center - tap) * (DOUBLE_TAP_ZOOM - 1f), DOUBLE_TAP_ZOOM)
                scale = DOUBLE_TAP_ZOOM
              }
            },
          )
        }
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, zoom, _ ->
            val next = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
            offset = if (next <= MIN_ZOOM) Offset.Zero else clampOffset(offset + pan, next)
            scale = next
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(part.imageRes),
        contentDescription = part.title,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offset.x
            translationY = offset.y
          },
      )

      IconButton(
        onClick = onDismiss,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close image",
          tint = White,
        )
      }
    }
  }
}
