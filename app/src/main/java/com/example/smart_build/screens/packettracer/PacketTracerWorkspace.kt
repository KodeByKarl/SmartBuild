package com.example.smart_build.screens.packettracer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Logical network workspace:
 * movable devices, zoom, copper cables (solid / dashed), link-up pulse when correct.
 */
@Composable
fun PacketTracerWorkspace(
  devices: List<PtPlacedDevice>,
  cables: List<PtCable>,
  activeTool: PtCableTool,
  onToolSelected: (PtCableTool) -> Unit,
  devicePalette: List<PtDeviceKind>,
  onPlaceDevice: (PtDeviceKind) -> Unit,
  pendingFromId: String?,
  selectedDeviceId: String?,
  onDeviceTap: (String) -> Unit,
  /** Normalized delta drag (Select tool). */
  onDeviceDrag: (id: String, dx: Float, dy: Float) -> Unit,
  canPlaceDevices: Boolean,
  statusLine: String,
  celebrateCableId: String? = null,
  modifier: Modifier = Modifier,
) {
  var zoom by remember { mutableFloatStateOf(1f) }

  val pulse = rememberInfiniteTransition(label = "linkPulse")
  val pulseScale by pulse.animateFloat(
    initialValue = 0.7f,
    targetValue = 1.55f,
    animationSpec = infiniteRepeatable(
      animation = tween(650, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulseScale",
  )
  val pulseAlpha by pulse.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(650, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulseAlpha",
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PtPanelBg),
  ) {
    // Workspace
    BoxWithConstraints(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(0.dp))
        .background(PtWorkspaceBg),
    ) {
      val w = constraints.maxWidth.toFloat().coerceAtLeast(1f)
      val h = constraints.maxHeight.toFloat().coerceAtLeast(1f)
      val density = LocalDensity.current

      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = zoom
            scaleY = zoom
            // Keep zoom centered
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
          },
      ) {
        // Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
          val step = 24.dp.toPx()
          var x = 0f
          while (x < size.width) {
            drawLine(PtWorkspaceGrid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
          }
          var y = 0f
          while (y < size.height) {
            drawLine(PtWorkspaceGrid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
          }
        }

        // Cables
        Canvas(modifier = Modifier.fillMaxSize()) {
          cables.forEach { cable ->
            val a = devices.find { it.id == cable.fromId } ?: return@forEach
            val b = devices.find { it.id == cable.toId } ?: return@forEach
            val p1 = Offset(a.x * size.width, a.y * size.height)
            val p2 = Offset(b.x * size.width, b.y * size.height)
            val celebrating = cable.id == celebrateCableId || cable.ruleCorrect
            val baseWidth = if (celebrating && cable.linkUp) 4.5f else 3.5f
            val pathEffect = if (cable.tool.dashed) {
              PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            } else {
              null
            }
            // Glow when correct
            if (cable.linkUp && cable.ruleCorrect) {
              drawLine(
                PtLinkGreen.copy(alpha = pulseAlpha * 0.45f),
                p1,
                p2,
                strokeWidth = baseWidth + 8f * pulseScale,
              )
            }
            drawLine(
              color = when {
                cable.linkUp && cable.ruleCorrect -> cable.tool.color
                !cable.ruleCorrect && cable.tool == PtCableTool.CROSSOVER -> PtCableTool.CROSSOVER.color
                !cable.ruleCorrect -> PtLinkRed.copy(alpha = 0.85f)
                else -> cable.tool.color
              },
              start = p1,
              end = p2,
              strokeWidth = baseWidth,
              pathEffect = pathEffect,
            )
            val mid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
            if (cable.linkUp && cable.ruleCorrect) {
              drawCircle(
                PtLinkGreen.copy(alpha = pulseAlpha),
                radius = 6f * pulseScale,
                center = mid,
              )
              drawCircle(PtLinkGreen, radius = 4.5f, center = mid)
            } else {
              drawCircle(PtLinkRed, radius = 5f, center = mid)
            }
          }
          if (pendingFromId != null) {
            val a = devices.find { it.id == pendingFromId }
            if (a != null) {
              val p1 = Offset(a.x * size.width, a.y * size.height)
              drawCircle(PtAccent.copy(alpha = 0.35f), radius = 28f, center = p1)
            }
          }
        }

        // Devices — drag when Select tool
        devices.forEach { device ->
          val px = (device.x * w).roundToInt()
          val py = (device.y * h).roundToInt()
          val box = 84.dp
          val boxPx = with(density) { box.roundToPx() }
          val canDrag = activeTool == PtCableTool.SELECT
          val marked = device.id == pendingFromId || device.id == selectedDeviceId
          Box(
            modifier = Modifier
              .offset { IntOffset(px - boxPx / 2, py - boxPx / 2) }
              .size(box)
              .clip(RoundedCornerShape(8.dp))
              .border(
                width = if (marked) 2.dp else 0.dp,
                color = when {
                  device.id == pendingFromId -> PtAccent
                  device.id == selectedDeviceId -> Color(0xFFE8B84A)
                  else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp),
              )
              .then(
                if (canDrag) {
                  Modifier.pointerInput(device.id, w, h, zoom) {
                    detectDragGestures(
                      onDrag = { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x / (w * zoom)
                        val dy = dragAmount.y / (h * zoom)
                        onDeviceDrag(device.id, dx, dy)
                      },
                    )
                  }
                } else {
                  Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDeviceTap(device.id) },
                  )
                },
              )
              .then(
                if (canDrag) {
                  Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDeviceTap(device.id) },
                  )
                } else {
                  Modifier
                },
              )
              .padding(4.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              DeviceFace(device.kind, compact = false)
              Text(
                device.hostname,
                color = Color(0xFF1C2833),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        if (devices.isEmpty()) {
          Text(
            "Place Switch, Server, and PC from the bar below.\nThen connect both to the Switch with Straight-Through.",
            color = Color(0xFF555555),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
          )
        }
      }
    }

    // Device + cable toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .background(PtToolbarBg)
        .border(1.dp, Color(0xFFBDBDBD))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 6.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("Devices", color = Color(0xFF333333), fontSize = 9.sp, fontWeight = FontWeight.Bold)
      devicePalette.forEach { kind ->
        val already = devices.any { it.kind == kind }
        val interaction = remember { MutableInteractionSource() }
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(if (already) Color(0xFFD5F5E3) else Color.White)
            .border(
              1.dp,
              if (canPlaceDevices && !already) PtAccent else Color(0xFFBDBDBD),
              RoundedCornerShape(4.dp),
            )
            .clickable(
              enabled = canPlaceDevices && !already,
              interactionSource = interaction,
              indication = null,
              onClick = { onPlaceDevice(kind) },
            )
            .padding(2.dp),
        ) {
          DeviceFace(kind, compact = true)
          Text(kind.shortLabel, color = Color.Black, fontSize = 8.sp, maxLines = 1)
        }
      }
      Spacer(Modifier.width(8.dp))
      Text("Cables", color = Color(0xFF333333), fontSize = 9.sp, fontWeight = FontWeight.Bold)
      listOf(
        PtCableTool.SELECT,
        PtCableTool.STRAIGHT,
        PtCableTool.CROSSOVER,
        PtCableTool.DELETE,
      ).forEach { tool ->
        val selected = activeTool == tool
        val interaction = remember { MutableInteractionSource() }
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(0xFFD6EAF8) else Color.White)
            .border(1.dp, if (selected) PtAccent else Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
            .clickable(
              interactionSource = interaction,
              indication = null,
              onClick = { onToolSelected(tool) },
            )
            .padding(2.dp),
        ) {
          Text(
            tool.shortLabel,
            color = tool.color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            when (tool) {
              PtCableTool.STRAIGHT -> "Straight"
              PtCableTool.CROSSOVER -> "Cross-Over"
              PtCableTool.DELETE -> "Delete"
              else -> "Select"
            },
            color = Color(0xFF333333),
            fontSize = 7.sp,
            maxLines = 1,
          )
        }
      }
      Spacer(Modifier.width(8.dp))
      ZoomChip("-") { zoom = (zoom - 0.15f).coerceIn(0.6f, 1.8f) }
      Text(
        "${(zoom * 100).roundToInt()}%",
        color = Color(0xFF333333),
        fontSize = 9.sp,
      )
      ZoomChip("+") { zoom = (zoom + 0.15f).coerceIn(0.6f, 1.8f) }
    }
  }
}

@Composable
private fun ZoomChip(label: String, onClick: () -> Unit) {
  val interaction = remember { MutableInteractionSource() }
  Box(
    modifier = Modifier
      .size(26.dp)
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFF555555))
      .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
  }
}

@Composable
private fun DeviceFace(kind: PtDeviceKind, compact: Boolean) {
  val width = if (compact) 36.dp else if (kind == PtDeviceKind.SWITCH) 72.dp else 52.dp
  val height = if (compact) 22.dp else if (kind == PtDeviceKind.SWITCH) 34.dp else 46.dp
  Canvas(modifier = Modifier.size(width, height)) {
    when (kind) {
      PtDeviceKind.SWITCH -> drawSwitchFace()
      PtDeviceKind.SERVER -> drawServerFace()
      PtDeviceKind.PC -> drawPcFace()
      PtDeviceKind.ROUTER -> drawRouterFace()
      PtDeviceKind.MODEM -> drawModemFace()
      PtDeviceKind.AP -> drawApFace()
    }
  }
}

private fun DrawScope.drawSwitchFace() {
  val w = size.width
  val h = size.height
  val top = h * 0.18f
  val bodyH = h * 0.68f
  drawRoundRect(Color(0xFF2C3338), Offset(0f, top - 2f), Size(w * 0.07f, bodyH + 4f), CornerRadius(2f))
  drawRoundRect(Color(0xFF2C3338), Offset(w * 0.93f, top - 2f), Size(w * 0.07f, bodyH + 4f), CornerRadius(2f))
  drawRoundRect(Color(0xFF4E5963), Offset(w * 0.05f, top), Size(w * 0.90f, bodyH), CornerRadius(3f))
  drawRoundRect(Color(0xFF6B7780), Offset(w * 0.08f, top + 2f), Size(w * 0.84f, bodyH * 0.28f), CornerRadius(2f))
  val ports = 8
  val gap = w * 0.012f
  val left = w * 0.12f
  val right = w * 0.88f
  val portW = (right - left - gap * (ports - 1)) / ports
  val portH = bodyH * 0.38f
  val portTop = top + bodyH * 0.46f
  repeat(ports) { i ->
    val x = left + i * (portW + gap)
    drawRoundRect(Color(0xFF15191C), Offset(x, portTop), Size(portW, portH), CornerRadius(1.2f))
    drawCircle(
      if (i % 3 == 0) Color(0xFFF4D03F) else Color(0xFF2ECC71),
      radius = (portW * 0.16f).coerceAtLeast(1.2f),
      center = Offset(x + portW / 2f, portTop - bodyH * 0.08f),
    )
  }
}

private fun DrawScope.drawServerFace() {
  val w = size.width
  val h = size.height
  drawRoundRect(Color(0xFF1B242C), Offset(w * 0.30f, h * 0.04f), Size(w * 0.40f, h * 0.92f), CornerRadius(4f))
  drawRoundRect(Color(0xFF2E3B48), Offset(w * 0.34f, h * 0.10f), Size(w * 0.32f, h * 0.62f), CornerRadius(2f))
  repeat(4) { i ->
    val y = h * (0.16f + i * 0.13f)
    drawRoundRect(Color(0xFF12181E), Offset(w * 0.37f, y), Size(w * 0.26f, h * 0.08f), CornerRadius(1f))
    drawCircle(Color(0xFF5DADE2), radius = w * 0.015f, center = Offset(w * 0.40f, y + h * 0.04f))
  }
  drawCircle(Color(0xFF2ECC71), radius = w * 0.035f, center = Offset(w * 0.50f, h * 0.84f))
}

private fun DrawScope.drawPcFace() {
  val w = size.width
  val h = size.height
  drawRoundRect(Color(0xFF243140), Offset(w * 0.04f, h * 0.02f), Size(w * 0.62f, h * 0.58f), CornerRadius(3f))
  drawRoundRect(Color(0xFF5DADE2), Offset(w * 0.09f, h * 0.08f), Size(w * 0.52f, h * 0.44f), CornerRadius(2f))
  drawRect(Color(0xFF243140), Offset(w * 0.30f, h * 0.60f), Size(w * 0.10f, h * 0.12f))
  drawRoundRect(Color(0xFF243140), Offset(w * 0.16f, h * 0.70f), Size(w * 0.38f, h * 0.07f), CornerRadius(2f))
  drawRoundRect(Color(0xFF1B242C), Offset(w * 0.72f, h * 0.30f), Size(w * 0.22f, h * 0.56f), CornerRadius(2f))
  drawCircle(Color(0xFF2ECC71), radius = w * 0.025f, center = Offset(w * 0.83f, h * 0.40f))
  drawRect(Color(0xFF12181E), Offset(w * 0.78f, h * 0.50f), Size(w * 0.10f, h * 0.22f))
}

private fun DrawScope.drawRouterFace() {
  val w = size.width
  val h = size.height
  drawRoundRect(Color(0xFF3E4A3A), Offset(w * 0.06f, h * 0.22f), Size(w * 0.88f, h * 0.56f), CornerRadius(4f))
  repeat(4) { i ->
    val x = w * (0.22f + i * 0.16f)
    drawCircle(Color(0xFFF5B041), radius = w * 0.035f, center = Offset(x, h * 0.42f))
  }
  drawRoundRect(Color(0xFF1A1E22), Offset(w * 0.18f, h * 0.55f), Size(w * 0.64f, h * 0.12f), CornerRadius(1f))
}

private fun DrawScope.drawModemFace() {
  val w = size.width
  val h = size.height
  drawRoundRect(Color(0xFF34495E), Offset(w * 0.18f, h * 0.16f), Size(w * 0.64f, h * 0.62f), CornerRadius(6f))
  drawCircle(Color(0xFF2ECC71), radius = w * 0.05f, center = Offset(w * 0.38f, h * 0.36f))
  drawCircle(Color(0xFFF4D03F), radius = w * 0.05f, center = Offset(w * 0.62f, h * 0.36f))
}

private fun DrawScope.drawApFace() {
  val w = size.width
  val h = size.height
  drawRoundRect(Color(0xFF1A5276), Offset(w * 0.28f, h * 0.42f), Size(w * 0.44f, h * 0.28f), CornerRadius(8f))
  drawCircle(Color(0xFF48C9B0).copy(alpha = 0.35f), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.48f), style = Stroke(width = 2f))
  drawCircle(Color(0xFF48C9B0).copy(alpha = 0.7f), radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.48f), style = Stroke(width = 2f))
}
