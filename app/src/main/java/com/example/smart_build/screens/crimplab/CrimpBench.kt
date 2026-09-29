package com.example.smart_build.screens.crimplab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSFlex
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private data class HitRect(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float,
) {
  fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
  val cx get() = (left + right) / 2f
  val cy get() = (top + bottom) / 2f
  val width get() = right - left
  val height get() = bottom - top
}

private data class BenchLayout(
  val cable: HitRect? = null,
  val mat: HitRect? = null,
  val stripZone: HitRect? = null,
  val stripper: HitRect? = null,
  val pairs: List<HitRect> = emptyList(),
  val plug: HitRect? = null,
  val pins: List<HitRect> = emptyList(),
  val tray: HitRect? = null,
  val trayWires: List<HitRect> = emptyList(),
  val insertHandle: HitRect? = null,
  val crimper: HitRect? = null,
  val testerButton: HitRect? = null,
  val key: HitRect? = null,
)

private data class WireDrag(val wire: WireColor, val at: Offset)

private sealed interface DragKind {
  data object Cable : DragKind
  data object Stripper : DragKind
  data class Pair(val index: Int) : DragKind
  data class Conductor(val wire: WireColor, val fromPin: Int?) : DragKind
  data object Insert : DragKind
  data object Crimp : DragKind
}

private val Jacket = Color(0xFF6B7780)
private val Boot = Color(0xFF121212)
private val Housing = Color(0xFF16303C)
private val HousingEdge = Color(0xFF8FB8C9)
private val Gold = Color(0xFFE1B84A)
private val MatFill = Color(0xFF0C3330)
private val Danger = Color(0xFFE74C3C)
private val OkGreen = Color(0xFF2ECC71)

private fun wireByLabel(label: String) = T568B_WIRES.first { it.label == label }

private val TWISTED_PAIRS = listOf(
  "Orange pair" to (wireByLabel("WO") to wireByLabel("O")),
  "Green pair" to (wireByLabel("WG") to wireByLabel("G")),
  "Blue pair" to (wireByLabel("WBl") to wireByLabel("Bl")),
  "Brown pair" to (wireByLabel("WBr") to wireByLabel("Br")),
)

private fun benchLayout(
  size: Size,
  state: CrimpLabUiState,
  hints: Boolean,
  topInsetPx: Float,
  dp: Float,
): BenchLayout {
  val w = size.width
  val h = size.height
  val caption = 28f * dp
  return when (state.step) {
    CrimpStep.SELECT -> {
      val coil = min(w * 0.56f, h * 0.34f)
      val cable = HitRect(
        left = (w - coil) / 2f,
        top = topInsetPx + 8f * dp,
        right = (w + coil) / 2f,
        bottom = topInsetPx + 8f * dp + coil,
      )
      BenchLayout(
        cable = cable,
        mat = HitRect(16f * dp, cable.bottom + 16f * dp, w - 16f * dp, h - caption),
      )
    }
    CrimpStep.STRIP -> {
      val cable = HitRect(12f * dp, topInsetPx + 8f * dp, w - 12f * dp, h - caption - 36f * dp)
      val zone = HitRect(cable.left + cable.width * 0.42f, cable.top, cable.right, cable.bottom)
      val travel = zone.width * state.stripProgress
      val sx = zone.right - travel
      BenchLayout(
        cable = cable,
        stripZone = zone,
        stripper = HitRect(sx - 28f * dp, cable.top + 12f * dp, sx + 28f * dp, cable.bottom - 12f * dp),
      )
    }
    CrimpStep.UNTWIST -> {
      val top = topInsetPx + 8f * dp
      val bottom = h - caption
      val gap = 10f * dp
      val rowH = (bottom - top - gap * 3f) / 4f
      BenchLayout(
        pairs = List(4) { i ->
          val t = top + i * (rowH + gap)
          HitRect(12f * dp, t, w - 12f * dp, t + rowH)
        },
      )
    }
    CrimpStep.ORDER_A, CrimpStep.ORDER_B -> {
      val keyH = if (hints) 58f * dp else 0f
      val key = if (hints) {
        HitRect(12f * dp, topInsetPx + 4f * dp, w - 12f * dp, topInsetPx + 4f * dp + keyH)
      } else {
        null
      }
      val trayH = h * 0.26f
      val tray = HitRect(10f * dp, h - caption - trayH, w - 10f * dp, h - caption - 4f * dp)
      val plugTop = (key?.bottom ?: topInsetPx) + 22f * dp
      val plug = HitRect(16f * dp, plugTop, w - 16f * dp, tray.top - 8f * dp)
      val bootH = plug.height * 0.18f
      val channelTop = plug.top + 8f * dp
      val channelBottom = plug.bottom - bootH
      val gap = 4f * dp
      val innerL = plug.left + 8f * dp
      val innerW = (plug.width - 16f * dp - gap * 7f) / 8f
      val pins = List(8) { i ->
        val l = innerL + i * (innerW + gap)
        HitRect(l, channelTop, l + innerW, channelBottom)
      }
      val n = state.palette.size
      val wires = if (n == 0) {
        emptyList()
      } else {
        val chipGap = 6f * dp
        val maxChip = 48f * dp
        val avail = tray.width - 12f * dp - chipGap * (n - 1)
        val chipW = min(maxChip, avail / n)
        val chipH = min(tray.height - 22f * dp, 62f * dp)
        val total = n * chipW + (n - 1) * chipGap
        var x = tray.left + (tray.width - total) / 2f
        val y = tray.bottom - chipH - 6f * dp
        List(n) {
          val rect = HitRect(x, y, x + chipW, y + chipH)
          x += chipW + chipGap
          rect
        }
      }
      BenchLayout(plug = plug, pins = pins, tray = tray, trayWires = wires, key = key)
    }
    CrimpStep.INSERT_A, CrimpStep.INSERT_B -> {
      val plug = HitRect(w * 0.14f, topInsetPx + 12f * dp, w * 0.86f, h * 0.50f)
      BenchLayout(
        plug = plug,
        insertHandle = HitRect(plug.left, plug.bottom, plug.right, h - caption),
        pins = pinChannels(plug, dp),
      )
    }
    CrimpStep.CRIMP_A, CrimpStep.CRIMP_B -> {
      val plug = HitRect(w * 0.18f, topInsetPx + 10f * dp, w * 0.82f, h * 0.40f)
      BenchLayout(
        plug = plug,
        pins = pinChannels(plug, dp),
        crimper = HitRect(8f * dp, plug.bottom - 16f * dp, w - 8f * dp, h - caption),
      )
    }
    CrimpStep.TEST -> {
      val body = HitRect(16f * dp, topInsetPx + 12f * dp, w - 16f * dp, h - caption - 8f * dp)
      BenchLayout(
        plug = body,
        testerButton = HitRect(body.cx - 42f * dp, body.bottom - 62f * dp, body.cx + 42f * dp, body.bottom - 16f * dp),
      )
    }
  }
}

private fun pinChannels(plug: HitRect, dp: Float): List<HitRect> {
  val bootH = plug.height * 0.18f
  val gap = 4f * dp
  val top = plug.top + 8f * dp
  val bottom = plug.bottom - bootH
  val innerL = plug.left + 8f * dp
  val innerW = (plug.width - 16f * dp - gap * 7f) / 8f
  return List(8) { i ->
    val l = innerL + i * (innerW + gap)
    HitRect(l, top, l + innerW, bottom)
  }
}

@Composable
fun CrimpBench(
  state: CrimpLabUiState,
  hints: Boolean,
  topInset: Dp,
  heldPalette: Int,
  heldPin: Int,
  onCableDropped: () -> Unit,
  onCableMissed: () -> Unit,
  onStrip: (Float) -> Unit,
  onUntwist: (Int, Float) -> Unit,
  onInsert: (Float) -> Unit,
  onCrimp: (Float) -> Unit,
  onTest: () -> Unit,
  onPlace: (WireColor, Int?, Int) -> Unit,
  onReturn: (Int) -> Unit,
  onTrayTapped: (Int) -> Unit,
  onPinTapped: (Int) -> Unit,
) {
  val textMeasurer = rememberTextMeasurer()
  val stateRef = rememberUpdatedState(state)
  val hintsRef = rememberUpdatedState(hints)
  val insetRef = rememberUpdatedState(topInset)
  val dropCable = rememberUpdatedState(onCableDropped)
  val missCable = rememberUpdatedState(onCableMissed)
  val stripCb = rememberUpdatedState(onStrip)
  val untwistCb = rememberUpdatedState(onUntwist)
  val insertCb = rememberUpdatedState(onInsert)
  val crimpCb = rememberUpdatedState(onCrimp)
  val testCb = rememberUpdatedState(onTest)
  val placeCb = rememberUpdatedState(onPlace)
  val returnCb = rememberUpdatedState(onReturn)
  val trayTapCb = rememberUpdatedState(onTrayTapped)
  val pinTapCb = rememberUpdatedState(onPinTapped)

  var cableAt by remember { mutableStateOf<Offset?>(null) }
  var wireAt by remember { mutableStateOf<WireDrag?>(null) }
  var hoverPin by remember { mutableIntStateOf(-1) }

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(state.step) {
        val slop = viewConfiguration.touchSlop
        val widthPx = size.width.toFloat()
        val heightPx = size.height.toFloat()
        if (widthPx < 2f || heightPx < 2f) return@pointerInput
        val dpPx = 1.dp.toPx()
        val densityScope = this
        fun current(): BenchLayout = benchLayout(
          Size(densityScope.size.width.toFloat(), densityScope.size.height.toFloat()),
          stateRef.value,
          hintsRef.value,
          with(densityScope) { insetRef.value.toPx() },
          dpPx,
        )
        fun classify(x: Float, y: Float): DragKind? {
          val layout = current()
          val s = stateRef.value
          return when (s.step) {
            CrimpStep.SELECT -> if (layout.cable?.contains(x, y) == true) DragKind.Cable else null
            CrimpStep.STRIP ->
              if (layout.stripper?.contains(x, y) == true || layout.stripZone?.contains(x, y) == true) {
                DragKind.Stripper
              } else {
                null
              }
            CrimpStep.UNTWIST ->
              layout.pairs.indexOfFirst { it.contains(x, y) }.takeIf { it >= 0 }?.let { DragKind.Pair(it) }
            CrimpStep.ORDER_A, CrimpStep.ORDER_B -> {
              val pin = layout.pins.indexOfFirst { it.contains(x, y) }
              val onPin = s.pins.getOrNull(pin)
              if (pin >= 0 && onPin != null) {
                DragKind.Conductor(onPin, pin)
              } else {
                val wi = layout.trayWires.indexOfFirst { it.contains(x, y) }
                s.palette.getOrNull(wi)?.let { DragKind.Conductor(it, null) }
              }
            }
            CrimpStep.INSERT_A, CrimpStep.INSERT_B ->
              if (layout.insertHandle?.contains(x, y) == true || layout.plug?.contains(x, y) == true) {
                DragKind.Insert
              } else {
                null
              }
            CrimpStep.CRIMP_A, CrimpStep.CRIMP_B ->
              if (layout.crimper?.contains(x, y) == true || layout.plug?.contains(x, y) == true) {
                DragKind.Crimp
              } else {
                null
              }
            CrimpStep.TEST -> null
          }
        }
        awaitEachGesture {
          val down = awaitFirstDown()
          val pointerId = down.id
          val start = down.position
          var pos = start
          var past = false
          var kind: DragKind? = null
          var localInsert = stateRef.value.insertProgress
          var localCrimp = stateRef.value.crimpProgress
          val localUntwist = stateRef.value.untwist.toMutableList()
          while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
            if (!change.pressed) {
              pos = change.position
              break
            }
            val delta = change.position - pos
            pos = change.position
            if (!past && (pos - start).getDistance() > slop) {
              past = true
              kind = classify(start.x, start.y)
            }
            if (past && kind != null) {
              change.consume()
              when (val k = kind) {
                DragKind.Cable -> cableAt = pos
                DragKind.Stripper -> {
                  val zone = current().stripZone
                  if (zone != null && zone.width > 1f) {
                    stripCb.value(((zone.right - pos.x) / zone.width).coerceIn(0f, 1f))
                  }
                }
                is DragKind.Pair -> {
                  val band = current().pairs.getOrNull(k.index)
                  val span = (band?.width ?: widthPx).coerceAtLeast(1f)
                  val next = (localUntwist.getOrElse(k.index) { 0f } + abs(delta.x) / span * 1.35f)
                    .coerceIn(0f, 1f)
                  if (k.index in localUntwist.indices) localUntwist[k.index] = next
                  untwistCb.value(k.index, next)
                }
                is DragKind.Conductor -> {
                  wireAt = WireDrag(k.wire, pos)
                  hoverPin = current().pins.indexOfFirst { it.contains(pos.x, pos.y) }
                }
                DragKind.Insert -> {
                  localInsert = (localInsert + (-delta.y) / (heightPx * 0.33f)).coerceIn(0f, 1f)
                  insertCb.value(localInsert)
                }
                DragKind.Crimp -> {
                  localCrimp = (localCrimp + delta.y / (heightPx * 0.30f)).coerceIn(0f, 1f)
                  crimpCb.value(localCrimp)
                }
              }
            }
          }
          val layout = current()
          val stepNow = stateRef.value.step
          if (stepNow == CrimpStep.TEST && layout.testerButton?.contains(pos.x, pos.y) == true) {
            testCb.value()
          } else if (!past) {
            if (stepNow == CrimpStep.ORDER_A || stepNow == CrimpStep.ORDER_B) {
              val pin = layout.pins.indexOfFirst { it.contains(start.x, start.y) }
              if (pin >= 0) {
                pinTapCb.value(pin)
              } else {
                val wi = layout.trayWires.indexOfFirst { it.contains(start.x, start.y) }
                if (wi >= 0) trayTapCb.value(wi)
              }
            }
          } else {
            when (val k = kind) {
              DragKind.Cable -> {
                if (layout.mat?.contains(pos.x, pos.y) == true) dropCable.value() else missCable.value()
              }
              is DragKind.Conductor -> {
                val pin = layout.pins.indexOfFirst { it.contains(pos.x, pos.y) }
                if (pin >= 0) {
                  placeCb.value(k.wire, k.fromPin, pin)
                } else if (k.fromPin != null && layout.tray?.contains(pos.x, pos.y) == true) {
                  returnCb.value(k.fromPin)
                }
              }
              else -> Unit
            }
          }
          cableAt = null
          wireAt = null
          hoverPin = -1
        }
      },
  ) {
    val dp = 1.dp.toPx()
    val layout = benchLayout(size, state, hints, topInset.toPx(), dp)
    drawRect(
      Brush.verticalGradient(listOf(Color(0xFF12485A), Color(0xFF082028))),
      size = size,
    )
    when (state.step) {
      CrimpStep.SELECT -> drawSelect(layout, cableAt, dp, textMeasurer)
      CrimpStep.STRIP -> drawStrip(layout, state.stripProgress, dp, textMeasurer)
      CrimpStep.UNTWIST -> drawUntwist(layout, state.untwist, dp, textMeasurer)
      CrimpStep.ORDER_A, CrimpStep.ORDER_B -> drawOrder(
        layout, state, hints, heldPalette, heldPin, hoverPin, dp, textMeasurer,
      )
      CrimpStep.INSERT_A, CrimpStep.INSERT_B -> drawInsert(layout, state, dp, textMeasurer)
      CrimpStep.CRIMP_A, CrimpStep.CRIMP_B -> drawCrimp(layout, state, dp, textMeasurer)
      CrimpStep.TEST -> drawTester(layout, state, dp, textMeasurer)
    }
    wireAt?.let { ghost ->
      val chip = 22f * dp
      drawRoundRect(
        Color(ghost.wire.argb),
        topLeft = Offset(ghost.at.x - chip, ghost.at.y - chip * 1.3f),
        size = Size(chip * 2f, chip * 2.4f),
        cornerRadius = CornerRadius(6f * dp),
      )
      stripe(ghost.wire, HitRect(ghost.at.x - chip, ghost.at.y - chip * 1.3f, ghost.at.x + chip, ghost.at.y + chip * 1.1f))
      centeredLabel(textMeasurer, ghost.wire.label, ghost.at.x, ghost.at.y - 6f * dp, 11.sp, labelInk(ghost.wire))
    }
    val caption = when (state.step) {
      CrimpStep.SELECT -> "Drag the coil down onto the mat"
      CrimpStep.STRIP -> "Drag the stripper left to peel the jacket"
      CrimpStep.UNTWIST -> "Swipe each twisted pair until it lies flat"
      CrimpStep.ORDER_A, CrimpStep.ORDER_B -> "Drag into a pin. Drop on a filled pin to replace it."
      CrimpStep.INSERT_A, CrimpStep.INSERT_B -> "Drag the wires up into the plug"
      CrimpStep.CRIMP_A, CrimpStep.CRIMP_B -> "Drag down to close the crimper"
      CrimpStep.TEST -> if (state.passed) "Wire map passed" else "Press TEST on the cable tester"
    }
    centeredLabel(
      textMeasurer,
      caption,
      size.width / 2f,
      size.height - 22f * dp,
      12.sp,
      Gold,
    )
  }
}

private fun DrawScope.drawSelect(
  layout: BenchLayout,
  cableAt: Offset?,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  val mat = layout.mat ?: return
  val cable = layout.cable ?: return
  drawRoundRect(
    MatFill,
    topLeft = Offset(mat.left, mat.top),
    size = Size(mat.width, mat.height),
    cornerRadius = CornerRadius(16f * dp),
  )
  drawRoundRect(
    Gold,
    topLeft = Offset(mat.left, mat.top),
    size = Size(mat.width, mat.height),
    cornerRadius = CornerRadius(16f * dp),
    style = Stroke(width = 2f * dp, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f * dp, 8f * dp))),
  )
  centeredLabel(measurer, "BENCH MAT", mat.cx, mat.cy - 8f * dp, 14.sp, Gold)
  centeredLabel(measurer, "Drop the cable here", mat.cx, mat.cy + 14f * dp, 12.sp, Color.White.copy(alpha = 0.75f))
  val center = cableAt ?: Offset(cable.cx, cable.cy)
  drawCoil(center, min(cable.width, cable.height) * 0.48f, dp)
  centeredLabel(measurer, "CAT 6", center.x, center.y - 6f * dp, 13.sp, Color.White)
}

private fun DrawScope.drawCoil(center: Offset, radius: Float, dp: Float) {
  val path = Path()
  val steps = 96
  for (i in 0..steps) {
    val t = i / steps.toFloat()
    val ang = t * 3.2f * PI.toFloat()
    val r = radius * (0.16f + 0.84f * t)
    val x = center.x + cos(ang) * r
    val y = center.y + sin(ang) * r * 0.72f
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  drawPath(path, Jacket, style = Stroke(width = 12f * dp, cap = StrokeCap.Round))
  drawPath(path, Color(0xFF9AABB4), style = Stroke(width = 3f * dp, cap = StrokeCap.Round))
}

private fun DrawScope.drawStrip(
  layout: BenchLayout,
  progress: Float,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  val cable = layout.cable ?: return
  val zone = layout.stripZone ?: return
  val peel = zone.width * progress
  val jacketRight = (cable.right - peel).coerceAtLeast(cable.left)
  drawRoundRect(
    Color(0xFF0C222C),
    topLeft = Offset(cable.left, cable.top),
    size = Size(cable.width, cable.height),
    cornerRadius = CornerRadius(18f * dp),
  )
  val innerTop = cable.top + 28f * dp
  val innerBottom = cable.bottom - 16f * dp
  val rowH = (innerBottom - innerTop) / 4f
  TWISTED_PAIRS.forEachIndexed { index, (name, colors) ->
    val mid = innerTop + rowH * index + rowH / 2f
    val amp = rowH * 0.22f
    drawTwist(cable.left + 16f * dp, cable.right - 16f * dp, mid, amp, 0f, Color(colors.first.argb), dp)
    drawTwist(cable.left + 16f * dp, cable.right - 16f * dp, mid, amp, PI.toFloat(), Color(colors.second.argb), dp)
    if (jacketRight < cable.right - 24f * dp) {
      centeredLabel(measurer, name, cable.right - 70f * dp, mid - 18f * dp, 10.sp, Color.White.copy(alpha = 0.85f))
    }
  }
  if (jacketRight - cable.left > 2f) {
    drawRoundRect(
      Jacket,
      topLeft = Offset(cable.left, cable.top),
      size = Size(jacketRight - cable.left, cable.height),
      cornerRadius = CornerRadius(18f * dp),
    )
    drawRoundRect(
      Color.White.copy(alpha = 0.16f),
      topLeft = Offset(cable.left + 14f * dp, cable.top + 10f * dp),
      size = Size((jacketRight - cable.left - 28f * dp).coerceAtLeast(0f), 8f * dp),
      cornerRadius = CornerRadius(4f * dp),
    )
    centeredLabel(measurer, "CAT 6 JACKET", cable.left + 78f * dp, cable.top + 28f * dp, 12.sp, Color.White)
  }
  layout.stripper?.let { tool ->
    drawRoundRect(
      Color(0xFFF1C40F),
      topLeft = Offset(tool.left, tool.top),
      size = Size(tool.width, tool.height),
      cornerRadius = CornerRadius(10f * dp),
    )
    drawRect(
      Color(0xFF222222),
      topLeft = Offset(tool.cx - 3f * dp, tool.top + 10f * dp),
      size = Size(6f * dp, tool.height - 20f * dp),
    )
  }
  val barTop = cable.bottom + 8f * dp
  drawRoundRect(
    Color.White.copy(alpha = 0.12f),
    topLeft = Offset(cable.left, barTop),
    size = Size(cable.width, 8f * dp),
    cornerRadius = CornerRadius(4f * dp),
  )
  drawRoundRect(
    Gold,
    topLeft = Offset(cable.left, barTop),
    size = Size(cable.width * progress, 8f * dp),
    cornerRadius = CornerRadius(4f * dp),
  )
}

private fun DrawScope.drawUntwist(
  layout: BenchLayout,
  untwist: List<Float>,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  layout.pairs.forEachIndexed { index, row ->
    val amount = untwist.getOrElse(index) { 0f }
    val (name, colors) = TWISTED_PAIRS[index]
    val done = amount >= 0.9f
    drawRoundRect(
      if (done) Color(0xFF12382A) else Color(0xFF102A34),
      topLeft = Offset(row.left, row.top),
      size = Size(row.width, row.height),
      cornerRadius = CornerRadius(12f * dp),
    )
    drawRoundRect(
      if (done) OkGreen else Color.White.copy(alpha = 0.16f),
      topLeft = Offset(row.left, row.top),
      size = Size(row.width, row.height),
      cornerRadius = CornerRadius(12f * dp),
      style = Stroke(width = if (done) 2f * dp else 1f * dp),
    )
    val amp = row.height * 0.22f * (1f - amount)
    val split = 7f * dp * amount
    val x0 = row.left + 16f * dp
    val x1 = row.right - 16f * dp
    val mid = row.cy
    drawTwist(x0, x1, mid - split, amp, 0f, Color(colors.first.argb), dp)
    drawTwist(x0, x1, mid + split, amp, PI.toFloat(), Color(colors.second.argb), dp)
    centeredLabel(measurer, name, row.cx, row.top + 2f * dp, 11.sp, Color.White.copy(alpha = 0.9f))
    if (done) {
      centeredLabel(measurer, "FLAT", row.right - 28f * dp, row.cy - 8f * dp, 11.sp, OkGreen)
    }
  }
}

private fun DrawScope.drawTwist(
  x0: Float,
  x1: Float,
  centerY: Float,
  amp: Float,
  phase: Float,
  color: Color,
  dp: Float,
) {
  val path = Path()
  val steps = 48
  for (i in 0..steps) {
    val t = i / steps.toFloat()
    val x = x0 + (x1 - x0) * t
    val y = centerY + sin(t * PI.toFloat() * 7f + phase) * amp
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  drawPath(path, color, style = Stroke(width = 6f * dp, cap = StrokeCap.Round))
}

private fun DrawScope.drawOrder(
  layout: BenchLayout,
  state: CrimpLabUiState,
  hints: Boolean,
  heldPalette: Int,
  heldPin: Int,
  hoverPin: Int,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  val expected = expectedWires(state.mode, state.orderingEndA)
  val standard = if (state.orderingEndA && state.mode == CableMode.CROSSOVER) "T568A" else "T568B"
  val endName = if (state.orderingEndA) "End A" else "End B"
  layout.key?.let { key ->
    drawRoundRect(
      Color(0xFF0E3344),
      topLeft = Offset(key.left, key.top),
      size = Size(key.width, key.height),
      cornerRadius = CornerRadius(8f * dp),
    )
    centeredLabel(measurer, "$endName · $standard", key.left + 8f * dp, key.top + 2f * dp, 11.sp, Gold, center = false)
    val n = expected.size
    val gap = 3f * dp
    val barW = (key.width - 16f * dp - gap * (n - 1)) / n
    val barTop = key.top + 18f * dp
    expected.forEachIndexed { i, wire ->
      val l = key.left + 8f * dp + i * (barW + gap)
      val rect = HitRect(l, barTop, l + barW, key.bottom - 4f * dp)
      drawConductor(rect, wire, dp)
      centeredLabel(measurer, "${i + 1}", rect.cx, rect.top - 1f * dp, 8.sp, Color.White.copy(alpha = 0.7f))
    }
  }
  val plug = layout.plug ?: return
  drawPlugShell(plug, dp, endName, measurer)
  layout.pins.forEachIndexed { i, pin ->
    val wire = state.pins.getOrNull(i)
    val wanted = expected[i]
    val wrong = wire != null && wire.label != wanted.label
    val right = wire != null && !wrong
    drawRoundRect(
      Color(0xFF0C1C24),
      topLeft = Offset(pin.left, pin.top),
      size = Size(pin.width, pin.height),
      cornerRadius = CornerRadius(3f * dp),
    )
    if (wire == null && hints) {
      drawRoundRect(
        Color(wanted.argb).copy(alpha = 0.28f),
        topLeft = Offset(pin.left, pin.top),
        size = Size(pin.width, pin.height),
        cornerRadius = CornerRadius(3f * dp),
      )
    }
    if (wire != null) {
      drawConductor(pin, wire, dp)
      centeredLabel(measurer, wire.label, pin.cx, pin.cy - 6f * dp, 10.sp, labelInk(wire))
    }
    val border = when {
      i == hoverPin -> Gold
      heldPin == i -> Gold
      wrong -> Danger
      right -> OkGreen
      else -> Color.White.copy(alpha = 0.2f)
    }
    drawRoundRect(
      border,
      topLeft = Offset(pin.left, pin.top),
      size = Size(pin.width, pin.height),
      cornerRadius = CornerRadius(3f * dp),
      style = Stroke(width = if (wrong || i == hoverPin || heldPin == i) 2.5f * dp else 1f * dp),
    )
    drawRoundRect(
      Gold,
      topLeft = Offset(pin.left, pin.top),
      size = Size(pin.width, 7f * dp),
      cornerRadius = CornerRadius(2f * dp),
    )
    centeredLabel(measurer, "${i + 1}", pin.cx, pin.top - 16f * dp, 10.sp, Color.White.copy(alpha = 0.8f))
  }
  val tray = layout.tray ?: return
  drawRoundRect(
    Color(0xFF0C2832),
    topLeft = Offset(tray.left, tray.top),
    size = Size(tray.width, tray.height),
    cornerRadius = CornerRadius(10f * dp),
  )
  centeredLabel(measurer, "Loose conductors", tray.left + 10f * dp, tray.top + 4f * dp, 11.sp, Color.White.copy(alpha = 0.7f), center = false)
  if (state.palette.isEmpty()) {
    centeredLabel(measurer, "All eight are in the plug", tray.cx, tray.cy, 12.sp, OkGreen)
  }
  state.palette.forEachIndexed { index, wire ->
    val chip = layout.trayWires.getOrNull(index) ?: return@forEachIndexed
    val selected = heldPalette == index || state.revealLabel == wire.label
    drawConductor(chip, wire, dp)
    if (selected) {
      drawRoundRect(
        if (state.revealLabel == wire.label) Gold else Color.White,
        topLeft = Offset(chip.left, chip.top),
        size = Size(chip.width, chip.height),
        cornerRadius = CornerRadius(6f * dp),
        style = Stroke(width = 2.5f * dp),
      )
    }
    centeredLabel(measurer, wire.label, chip.cx, chip.cy - 6f * dp, 11.sp, labelInk(wire))
  }
}

private fun DrawScope.drawInsert(
  layout: BenchLayout,
  state: CrimpLabUiState,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  val plug = layout.plug ?: return
  val handle = layout.insertHandle
  val endA = state.step == CrimpStep.INSERT_A
  val wires = expectedWires(state.mode, endA)
  drawPlugShell(plug, dp, if (endA) "End A" else "End B", measurer)
  val progress = state.insertProgress
  layout.pins.forEachIndexed { i, pin ->
    val wire = wires[i]
    val top = lerp(plug.bottom + 6f * dp, pin.top, progress)
    val bottom = lerp((handle?.bottom ?: plug.bottom + 80f * dp) - 12f * dp, pin.bottom, progress)
    val rect = HitRect(pin.left, top, pin.right, bottom.coerceAtLeast(top + 8f * dp))
    drawConductor(rect, wire, dp)
    if (progress > 0.72f) {
      centeredLabel(measurer, wire.label, pin.cx, pin.cy - 6f * dp, 9.sp, labelInk(wire))
    }
    drawRoundRect(
      Gold.copy(alpha = 0.35f + 0.65f * progress),
      topLeft = Offset(pin.left, pin.top),
      size = Size(pin.width, 7f * dp),
      cornerRadius = CornerRadius(2f * dp),
    )
  }
  handle?.let {
    centeredLabel(
      measurer,
      "Seat  ${(progress * 100).toInt()}%",
      it.cx,
      it.bottom - 18f * dp,
      12.sp,
      Color.White,
    )
  }
}

private fun DrawScope.drawCrimp(
  layout: BenchLayout,
  state: CrimpLabUiState,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  val plug = layout.plug ?: return
  val endA = state.step == CrimpStep.CRIMP_A
  val wires = expectedWires(state.mode, endA)
  val progress = state.crimpProgress
  drawPlugShell(plug, dp, if (endA) "End A" else "End B", measurer)
  layout.pins.forEachIndexed { i, pin ->
    drawConductor(pin, wires[i], dp)
    drawRoundRect(
      Gold,
      topLeft = Offset(pin.left, pin.top),
      size = Size(pin.width, 7f * dp),
      cornerRadius = CornerRadius(2f * dp),
    )
  }
  if (progress > 0.2f) {
    val y = plug.top + plug.height * 0.62f
    drawLine(
      Color.Black.copy(alpha = progress),
      Offset(plug.left + 10f * dp, y),
      Offset(plug.right - 10f * dp, y),
      strokeWidth = 4f * dp * progress,
      cap = StrokeCap.Round,
    )
  }
  val pivot = Offset(plug.cx, plug.bottom - 4f * dp)
  val handleW = 26f * dp
  val handleH = (layout.crimper?.height ?: 180f * dp) * 0.72f
  val angle = lerp(32f, 5f, progress)
  rotate(angle, pivot) {
    drawRoundRect(
      Color(0xFF1E8449),
      topLeft = Offset(pivot.x - handleW - 10f * dp, pivot.y),
      size = Size(handleW, handleH),
      cornerRadius = CornerRadius(12f * dp),
    )
  }
  rotate(-angle, pivot) {
    drawRoundRect(
      Color(0xFF148040),
      topLeft = Offset(pivot.x + 10f * dp, pivot.y),
      size = Size(handleW, handleH),
      cornerRadius = CornerRadius(12f * dp),
    )
  }
  centeredLabel(measurer, "Crimp  ${(progress * 100).toInt()}%", plug.cx, (layout.crimper?.bottom ?: size.height) - 8f * dp, 12.sp, Color.White)
}

private fun DrawScope.drawTester(
  layout: BenchLayout,
  state: CrimpLabUiState,
  dp: Float,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  val body = layout.plug ?: return
  drawRoundRect(
    Color(0xFF102830),
    topLeft = Offset(body.left, body.top),
    size = Size(body.width, body.height),
    cornerRadius = CornerRadius(16f * dp),
  )
  drawRoundRect(
    if (state.step == CrimpStep.TEST && !state.passed) Gold else HousingEdge.copy(alpha = 0.4f),
    topLeft = Offset(body.left, body.top),
    size = Size(body.width, body.height),
    cornerRadius = CornerRadius(16f * dp),
    style = Stroke(width = 2f * dp),
  )
  centeredLabel(measurer, "CABLE TESTER", body.cx, body.top + 12f * dp, 14.sp, Gold)
  centeredLabel(
    measurer,
    if (state.mode == CableMode.STRAIGHT) "Straight-through map" else "Crossover map",
    body.cx,
    body.top + 32f * dp,
    12.sp,
    Color.White.copy(alpha = 0.75f),
  )
  val lead = HitRect(body.left - 8f * dp, body.cy - 10f * dp, body.left + 28f * dp, body.cy + 10f * dp)
  drawRoundRect(Jacket, Offset(lead.left, lead.top), Size(lead.width, lead.height), CornerRadius(6f * dp))
  val gap = 8f * dp
  val led = min(36f * dp, (body.width - 32f * dp - gap * 7f) / 8f)
  val rowW = 8f * led + gap * 7f
  var x = body.cx - rowW / 2f
  val y = body.top + 70f * dp
  repeat(8) { i ->
    val lit = i < state.ledsLit
    drawCircle(
      if (lit) OkGreen else Color(0xFF1A3038),
      radius = led / 2f,
      center = Offset(x + led / 2f, y + led / 2f),
    )
    drawCircle(
      if (lit) Color(0xFF58D68D) else Color.White.copy(alpha = 0.2f),
      radius = led / 2f,
      center = Offset(x + led / 2f, y + led / 2f),
      style = Stroke(width = 2f * dp),
    )
    centeredLabel(
      measurer,
      "${i + 1}",
      x + led / 2f,
      y + led / 2f - 7f * dp,
      12.sp,
      if (lit) Color.Black else Color.White.copy(alpha = 0.7f),
    )
    x += led + gap
  }
  val button = layout.testerButton ?: return
  drawRoundRect(
    if (state.passed) OkGreen else Color(0xFF0A3A4A),
    topLeft = Offset(button.left, button.top),
    size = Size(button.width, button.height),
    cornerRadius = CornerRadius(20f * dp),
  )
  drawRoundRect(
    Gold,
    topLeft = Offset(button.left, button.top),
    size = Size(button.width, button.height),
    cornerRadius = CornerRadius(20f * dp),
    style = Stroke(width = 2f * dp),
  )
  centeredLabel(measurer, if (state.passed) "PASS" else "TEST", button.cx, button.cy - 8f * dp, 14.sp, Color.White)
}

private fun DrawScope.drawPlugShell(
  plug: HitRect,
  dp: Float,
  endName: String,
  measurer: androidx.compose.ui.text.TextMeasurer,
) {
  drawRoundRect(
    Housing,
    topLeft = Offset(plug.left, plug.top),
    size = Size(plug.width, plug.height),
    cornerRadius = CornerRadius(10f * dp),
  )
  drawRoundRect(
    HousingEdge.copy(alpha = 0.7f),
    topLeft = Offset(plug.left, plug.top),
    size = Size(plug.width, plug.height),
    cornerRadius = CornerRadius(10f * dp),
    style = Stroke(width = 2f * dp),
  )
  val bootH = plug.height * 0.18f
  drawRoundRect(
    Boot,
    topLeft = Offset(plug.left - 4f * dp, plug.bottom - bootH),
    size = Size(plug.width + 8f * dp, bootH + 8f * dp),
    cornerRadius = CornerRadius(8f * dp),
  )
  val jacketW = plug.width * 0.28f
  drawRoundRect(
    Jacket,
    topLeft = Offset(plug.cx - jacketW / 2f, plug.bottom),
    size = Size(jacketW, 16f * dp),
    cornerRadius = CornerRadius(4f * dp),
  )
  centeredLabel(measurer, endName, plug.cx, plug.bottom - bootH + 2f * dp, 11.sp, Color.White.copy(alpha = 0.85f))
}

private fun DrawScope.drawConductor(rect: HitRect, wire: WireColor, dp: Float) {
  drawRoundRect(
    Color(wire.argb),
    topLeft = Offset(rect.left, rect.top),
    size = Size(rect.width, rect.height),
    cornerRadius = CornerRadius(4f * dp),
  )
  stripe(wire, rect)
}

private fun DrawScope.stripe(wire: WireColor, rect: HitRect) {
  val stripe = when (wire.label) {
    "WO" -> Color(0xFFE67E22)
    "WG" -> Color(0xFF1E8449)
    "WBl" -> Color(0xFF2980B9)
    "WBr" -> Color(0xFF6E3B1A)
    else -> null
  } ?: return
  val w = rect.width * 0.28f
  drawRect(stripe, topLeft = Offset(rect.cx - w / 2f, rect.top), size = Size(w, rect.height))
}

private fun DrawScope.centeredLabel(
  measurer: androidx.compose.ui.text.TextMeasurer,
  text: String,
  x: Float,
  y: Float,
  fontSize: androidx.compose.ui.unit.TextUnit,
  color: Color,
  center: Boolean = true,
) {
  val layout = measurer.measure(
    text = text,
    style = TextStyle(
      color = color,
      fontSize = fontSize,
      fontFamily = GSFlex,
      fontWeight = FontWeight.Bold,
    ),
  )
  val left = if (center) x - layout.size.width / 2f else x
  drawText(layout, topLeft = Offset(left, y))
}

private fun labelInk(wire: WireColor): Color {
  val r = ((wire.argb shr 16) and 0xFF) / 255f
  val g = ((wire.argb shr 8) and 0xFF) / 255f
  val b = (wire.argb and 0xFF) / 255f
  val luma = 0.3f * r + 0.59f * g + 0.11f * b
  return if (luma > 0.62f) Color.Black else Color.White
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
