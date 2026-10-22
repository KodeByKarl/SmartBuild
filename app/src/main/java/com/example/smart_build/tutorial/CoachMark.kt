package com.example.smart_build.tutorial

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * One tutorial step. [targetKey] points at a [coachTarget] on screen and gets spotlighted;
 * a step without a target (or whose target is not on screen) is a centred GCash-style modal.
 */
data class CoachStep(
  val title: String,
  val body: String,
  val targetKey: String? = null,
  val emoji: String = "",
)

/** "Has this student already seen tutorial X?" — scoped per signed-in account. */
object TutorialStore {
  private const val PREFS = "smartbuild_tutorial"

  private fun prefs(context: Context) =
    context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

  private fun userId(): String? = try {
    SupabaseClient.client.auth.currentUserOrNull()?.id
  } catch (_: Throwable) {
    null
  }

  private fun key(id: String) = "${userId()}:$id"

  /**
   * Tutorials are for NEW accounts only. Treated as already seen when: nobody is signed in
   * yet (never mark under a guest key), the tour was shown/skipped before, or the account
   * already has progress (an existing student who updated the app is not "new").
   */
  fun isSeen(context: Context, id: String): Boolean {
    if (userId() == null) return true
    if (prefs(context).getBoolean(key(id), false)) return true
    return isExistingStudent(context)
  }

  /**
   * Decided ONCE per account, the first time the app asks: an account that already has
   * progress then is an existing student. A new student who later gains progress stays "new"
   * so they still get the Module 2–4 lab tours.
   */
  private fun isExistingStudent(context: Context): Boolean {
    val p = prefs(context)
    val flag = "${userId()}:__existing"
    if (!p.contains(flag)) {
      p.edit().putBoolean(flag, (0..4).any { ModuleProgressStore.progress(context, it) > 0f }).apply()
    }
    return p.getBoolean(flag, false)
  }

  fun markSeen(context: Context, id: String) {
    if (userId() == null) return
    prefs(context).edit().putBoolean(key(id), true).apply()
  }

  /** "Replay tutorials" — forget everything this account has seen. */
  fun resetAll(context: Context) {
    val uid = userId() ?: return
    val prefix = "$uid:"
    val editor = prefs(context).edit()
    prefs(context).all.keys.filter { it.startsWith(prefix) }.forEach { editor.remove(it) }
    editor.apply()
  }
}

private class Tour(val id: String, val steps: List<CoachStep>, val context: Context)

/** Holds the on-screen target rectangles and the running tour. One per screen / window. */
@Stable
class CoachController {
  internal val targets = mutableStateMapOf<String, Rect>()
  private val queue = ArrayDeque<Tour>()
  private var current by mutableStateOf<Tour?>(null)
  internal var index by mutableIntStateOf(0)
    private set

  internal val steps: List<CoachStep> get() = current?.steps.orEmpty()
  val isRunning: Boolean get() = current != null

  /** Target of the step being shown (lets a scrolling screen bring it into view). */
  val activeTargetKey: String? get() = steps.getOrNull(index)?.targetKey

  /** Start a tour (queued behind a running one). [force] replays it even if already seen. */
  fun start(context: Context, id: String, steps: List<CoachStep>, force: Boolean = false) {
    if (steps.isEmpty()) return
    if (!force && TutorialStore.isSeen(context, id)) return
    if (current?.id == id || queue.any { it.id == id }) return
    val tour = Tour(id, steps, context.applicationContext)
    if (current == null) {
      current = tour
      index = 0
    } else {
      queue.addLast(tour)
    }
  }

  internal fun next() {
    if (index < steps.lastIndex) index += 1 else finish()
  }

  internal fun back() {
    if (index > 0) index -= 1
  }

  /** Done or skipped: either way the student has seen it. */
  internal fun finish() {
    current?.let { TutorialStore.markSeen(it.context, it.id) }
    current = queue.removeFirstOrNull()
    index = 0
  }
}

@Composable
fun rememberCoachController(): CoachController = remember { CoachController() }

/** Mark a composable as spotlight-able by tutorial steps whose `targetKey == key`. */
fun Modifier.coachTarget(controller: CoachController?, key: String): Modifier =
  if (controller == null) this
  else this.onGloballyPositioned { controller.targets[key] = it.boundsInWindow() }

/** Starts [steps] once per account (after [delayMs] so the screen can settle). */
@Composable
fun CoachTourEffect(
  controller: CoachController,
  tourId: String,
  steps: List<CoachStep>,
  context: Context,
  enabled: Boolean = true,
  delayMs: Long = 700,
) {
  LaunchedEffect(tourId, enabled) {
    if (!enabled || TutorialStore.isSeen(context, tourId)) return@LaunchedEffect
    delay(delayMs)
    controller.start(context, tourId, steps)
  }
}

/** Put this LAST inside the screen's root Box so it draws above everything. */
@Composable
fun CoachHost(controller: CoachController) {
  if (!controller.isRunning) return
  val steps = controller.steps
  val index = controller.index.coerceIn(0, steps.lastIndex)
  val step = steps[index]

  BackHandler { controller.finish() }

  var hostOrigin by remember { mutableStateOf(Offset.Zero) }
  var hostSize by remember { mutableStateOf(IntSize.Zero) }
  val density = LocalDensity.current

  val target: Rect? = step.targetKey
    ?.let { controller.targets[it] }
    ?.takeIf { it.width > 1f && it.height > 1f }
    ?.translate(-hostOrigin.x, -hostOrigin.y)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .onGloballyPositioned {
        hostOrigin = it.positionInWindow()
        hostSize = it.size
      }
      // Swallow every touch: the tour must be finished (or skipped) with its own buttons.
      .pointerInput(Unit) { detectTapGestures { } },
  ) {
    val pulse by rememberInfiniteTransition(label = "coach-pulse").animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
      label = "coach-pulse-value",
    )

    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
      drawRect(Color.Black.copy(alpha = 0.78f))
      if (target != null) {
        val pad = 8.dp.toPx()
        val radius = 14.dp.toPx()
        val hole = target.inflate(pad)
        drawRoundRect(
          color = Color.Transparent,
          topLeft = hole.topLeft,
          size = Size(hole.width, hole.height),
          cornerRadius = CornerRadius(radius),
          blendMode = BlendMode.Clear,
        )
        drawRoundRect(
          color = Primary,
          topLeft = hole.topLeft,
          size = Size(hole.width, hole.height),
          cornerRadius = CornerRadius(radius),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()),
        )
        val grow = pulse * 10.dp.toPx()
        val ring = hole.inflate(grow)
        drawRoundRect(
          color = Primary.copy(alpha = (1f - pulse) * 0.7f),
          topLeft = ring.topLeft,
          size = Size(ring.width, ring.height),
          cornerRadius = CornerRadius(radius + grow),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
        )
      }
    }

    var cardSize by remember(index) { mutableStateOf(IntSize.Zero) }
    val measured = cardSize != IntSize.Zero
    val gapPx = with(density) { 18.dp.toPx() }
    val edgePx = with(density) { 12.dp.toPx() }

    val cardModifier = if (target == null) {
      Modifier.align(Alignment.Center)
    } else {
      val w = cardSize.width.toFloat()
      val h = cardSize.height.toFloat()
      val freeBelow = hostSize.height - target.bottom
      val freeAbove = target.top
      val freeRight = hostSize.width - target.right
      val freeLeft = target.left
      // Prefer below / above (cheap to read), then the sides; else the roomiest side.
      val place = when {
        freeBelow >= h + gapPx -> 'B'
        freeAbove >= h + gapPx -> 'T'
        freeRight >= w + gapPx -> 'R'
        freeLeft >= w + gapPx -> 'L'
        else -> listOf('B' to freeBelow, 'T' to freeAbove, 'R' to freeRight, 'L' to freeLeft)
          .maxBy { it.second }.first
      }
      var x: Float
      var y: Float
      when (place) {
        'B' -> { x = target.center.x - w / 2; y = target.bottom + gapPx }
        'T' -> { x = target.center.x - w / 2; y = target.top - gapPx - h }
        'R' -> { x = target.right + gapPx; y = target.center.y - h / 2 }
        else -> { x = target.left - gapPx - w; y = target.center.y - h / 2 }
      }
      x = x.coerceIn(edgePx, (hostSize.width - w - edgePx).coerceAtLeast(edgePx))
      y = y.coerceIn(edgePx, (hostSize.height - h - edgePx).coerceAtLeast(edgePx))
      Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }
    }

    CoachCard(
      step = step,
      index = index,
      total = steps.size,
      isModal = target == null,
      onBack = { controller.back() },
      onNext = { controller.next() },
      onSkip = { controller.finish() },
      modifier = cardModifier
        .onSizeChanged { cardSize = it }
        .alpha(if (measured || target == null) 1f else 0f),
    )
  }
}

@Composable
private fun CoachCard(
  step: CoachStep,
  index: Int,
  total: Int,
  isModal: Boolean,
  onBack: () -> Unit,
  onNext: () -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(18.dp)
  val last = index == total - 1
  Column(
    modifier = modifier
      .padding(16.dp)
      .widthIn(max = if (isModal) 420.dp else 340.dp)
      .clip(shape)
      .background(Color(0xFF0A2A3A))
      .border(1.5.dp, Primary.copy(alpha = 0.8f), shape)
      .padding(18.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = if (isModal) Alignment.CenterHorizontally else Alignment.Start,
  ) {
    if (step.emoji.isNotBlank() && isModal) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(Primary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
      ) {
        Text(step.emoji, fontSize = 32.sp)
      }
    }
    Text(
      text = if (!isModal && step.emoji.isNotBlank()) "${step.emoji}  ${step.title}" else step.title,
      color = White,
      fontFamily = GSFlex,
      fontWeight = FontWeight.Bold,
      fontSize = if (isModal) 20.sp else 17.sp,
      lineHeight = 24.sp,
      textAlign = if (isModal) androidx.compose.ui.text.style.TextAlign.Center else null,
    )
    Text(
      text = step.body,
      color = White.copy(alpha = 0.82f),
      fontFamily = GSFlex,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      textAlign = if (isModal) androidx.compose.ui.text.style.TextAlign.Center else null,
    )

    if (total > 1) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, if (isModal) Alignment.CenterHorizontally else Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(total) { i ->
          Box(
            modifier = Modifier
              .size(width = if (i == index) 18.dp else 7.dp, height = 7.dp)
              .clip(CircleShape)
              .background(if (i == index) Primary else White.copy(alpha = 0.25f)),
          )
        }
      }
    }

    Spacer(Modifier.height(2.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      if (last) {
        Spacer(Modifier.size(1.dp))
      } else {
        Text(
          text = "Skip",
          color = White.copy(alpha = 0.6f),
          fontFamily = GSFlex,
          fontSize = 14.sp,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSkip)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (index > 0) {
          Text(
            text = "Back",
            color = Primary,
            fontFamily = GSCode,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable(onClick = onBack)
              .padding(horizontal = 12.dp, vertical = 10.dp),
          )
        }
        Text(
          text = if (last) "Got it" else "Next",
          color = White,
          fontFamily = GSCode,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Primary)
            .clickable(onClick = onNext)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        )
      }
    }
  }
}
