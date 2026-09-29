package com.example.smart_build.screens.crimplab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.screens.composelabs.ComposeLabScaffold
import com.example.smart_build.ui.RuleFeedbackDialog
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun CrimpLabScreen(
  navController: NavHostController,
  hints: Boolean = true,
  forcedMode: CableMode? = null,
  allowModeSwitch: Boolean = true,
  embedded: Boolean = false,
  onStationComplete: (() -> Unit)? = null,
  onLeave: (() -> Unit)? = null,
) {
  var state by remember {
    mutableStateOf(
      CrimpLabUiState(
        mode = forcedMode ?: CableMode.STRAIGHT,
        status = if (forcedMode == CableMode.CROSSOVER) {
          "Drag the Cat 6 coil onto the mat for a crossover"
        } else {
          "Drag the Cat 6 coil onto the mat"
        },
      ),
    )
  }
  var dialog by remember { mutableStateOf<Pair<Boolean, Pair<String, String>>?>(null) }
  var heldPalette by remember { mutableIntStateOf(-1) }
  var heldPin by remember { mutableIntStateOf(-1) }

  LaunchedEffect(state.stepIndex) {
    heldPalette = -1
    heldPin = -1
  }

  LaunchedEffect(state.passed) {
    if (!state.passed) return@LaunchedEffect
    for (n in 1..8) {
      delay(140)
      state = state.copy(ledsLit = n)
    }
  }

  fun modeLabel() = if (state.mode == CableMode.STRAIGHT) "Straight-through T568B" else "Crossover T568A/B"

  fun freshBench(mode: CableMode, status: String) = CrimpLabUiState(mode = mode, status = status)

  fun advance(okTitle: String, okBody: String, nextStatus: String, announce: Boolean) {
    val next = (state.stepIndex + 1).coerceAtMost(CrimpStep.entries.lastIndex)
    val resetOrder = CrimpStep.entries[next] == CrimpStep.ORDER_B
    state = state.copy(
      stepIndex = next,
      status = nextStatus,
      flashError = null,
      ledsLit = 0,
      stripProgress = 0f,
      untwist = List(4) { 0f },
      insertProgress = 0f,
      crimpProgress = 0f,
      revealLabel = null,
      palette = if (resetOrder) shuffledPalette() else state.palette,
      pins = if (resetOrder) List(8) { null } else state.pins,
    )
    if (announce) dialog = true to (okTitle to okBody)
  }

  fun onCableDropped() {
    if (state.step != CrimpStep.SELECT || state.passed) return
    advance(
      "Cable on the bench",
      "Cat 6 is on the mat. Peel the jacket next.",
      "Drag the stripper along the jacket",
      announce = false,
    )
  }

  fun onStrip(progress: Float) {
    if (state.step != CrimpStep.STRIP || state.passed) return
    val next = maxOf(state.stripProgress, progress)
    if (next >= 0.9f) {
      advance(
        "Jacket stripped",
        "About 25 mm is open. The pairs are still twisted.",
        "Swipe each pair until the conductors lie flat",
        announce = false,
      )
    } else {
      state = state.copy(stripProgress = next, status = "Stripping the jacket… ${(next * 100).toInt()}%")
    }
  }

  fun onUntwist(index: Int, absolute: Float) {
    if (state.step != CrimpStep.UNTWIST || state.passed) return
    val next = state.untwist.toMutableList()
    if (index !in next.indices) return
    next[index] = maxOf(next[index], absolute).coerceIn(0f, 1f)
    if (next.all { it >= 0.9f }) {
      advance(
        "Pairs untwisted",
        "Eight conductors are flat. Order them in the plug.",
        "Drag each color into an RJ45 pin",
        announce = false,
      )
    } else {
      val done = next.count { it >= 0.9f }
      state = state.copy(untwist = next, status = "Untwisted $done of 4 pairs")
    }
  }

  fun onInsert(progress: Float) {
    val inserting = state.step == CrimpStep.INSERT_A || state.step == CrimpStep.INSERT_B
    if (!inserting || state.passed) return
    val next = maxOf(state.insertProgress, progress)
    if (next >= 0.92f) {
      val endA = state.step == CrimpStep.INSERT_A
      advance(
        if (endA) "End A seated" else "End B seated",
        "Conductors reach the gold contacts. Crimp the plug.",
        "Drag the crimper handles closed",
        announce = false,
      )
    } else {
      state = state.copy(insertProgress = next, status = "Seating conductors… ${(next * 100).toInt()}%")
    }
  }

  fun onCrimp(progress: Float) {
    val crimping = state.step == CrimpStep.CRIMP_A || state.step == CrimpStep.CRIMP_B
    if (!crimping || state.passed) return
    val next = maxOf(state.crimpProgress, progress)
    if (next >= 0.92f) {
      val endA = state.step == CrimpStep.CRIMP_A
      advance(
        if (endA) "End A crimped" else "End B crimped",
        if (endA) "Plug locked. Order the other end." else "Both ends are locked. Run the wire map.",
        if (endA) "Drag End B colors into the plug" else "Press TEST on the cable tester",
        announce = false,
      )
    } else {
      state = state.copy(crimpProgress = next, status = "Squeezing the crimper… ${(next * 100).toInt()}%")
    }
  }

  fun finishEnd(resultPins: List<WireColor?>) {
    val allMatch = resultPins.map { it?.label } == expectedWires(state.mode, state.orderingEndA).map { it.label }
    if (!allMatch) return
    if (state.orderingEndA) {
      state = state.copy(endACorrect = true, pins = List(8) { null }, palette = emptyList(), revealLabel = null)
      advance(
        "End A ordered",
        "Colors match ${if (state.mode == CableMode.STRAIGHT) "T568B" else "T568A"}. Push them into the plug.",
        "Drag the conductors up into the RJ45",
        announce = true,
      )
    } else {
      state = state.copy(endBCorrect = true, pins = List(8) { null }, palette = emptyList(), revealLabel = null)
      advance(
        "End B ordered",
        "End B matches ${if (state.mode == CableMode.STRAIGHT) "T568B (straight)" else "T568B (crossover)"}.",
        "Drag End B up into the plug",
        announce = true,
      )
    }
  }

  fun place(wire: WireColor, fromPin: Int?, pinIndex: Int) {
    if (!state.isOrdering || state.passed) return
    if (fromPin == pinIndex) return
    val expected = expectedWires(state.mode, state.orderingEndA)
    val result = applyPlacement(state.pins, state.palette, expected, wire, fromPin, pinIndex)
    if (!result.correctPin) {
      state = state.copy(
        pins = result.pins,
        palette = result.palette,
        revealLabel = if (hints) result.expectedLabel else null,
        status = "Pin ${pinIndex + 1} needs ${prettyWire(result.expectedLabel)}",
        flashError = "Wrong wire",
      )
      dialog = false to (
        "Wrong wire" to
          "Pin ${pinIndex + 1} needs ${prettyWire(result.expectedLabel)} (${result.expectedLabel}).\n" +
          "You placed ${prettyWire(wire.label)} (${wire.label}).\n\n" +
          "Drop the correct color on this pin to replace it. The other pins stay."
        )
      return
    }
    state = state.copy(
      pins = result.pins,
      palette = result.palette,
      revealLabel = null,
      status = "Pin ${pinIndex + 1} · ${prettyWire(wire.label)}",
      flashError = null,
    )
    if (result.allCorrect) finishEnd(result.pins)
  }

  fun onReturn(fromPin: Int) {
    if (!state.isOrdering || state.passed) return
    val wire = state.pins.getOrNull(fromPin) ?: return
    val pins = state.pins.toMutableList()
    pins[fromPin] = null
    state = state.copy(
      pins = pins,
      palette = state.palette + wire,
      revealLabel = null,
      status = "Pin ${fromPin + 1} is empty — conductor is back in the tray",
    )
  }

  fun onTrayTapped(index: Int) {
    if (!state.isOrdering || state.passed) return
    if (heldPin >= 0) {
      onReturn(heldPin)
      heldPin = -1
      heldPalette = -1
      return
    }
    heldPalette = if (heldPalette == index) -1 else index
    heldPin = -1
  }

  fun onPinTapped(index: Int) {
    if (!state.isOrdering || state.passed) return
    val wire = when {
      heldPalette >= 0 -> state.palette.getOrNull(heldPalette)
      heldPin >= 0 -> state.pins.getOrNull(heldPin)
      else -> null
    }
    if (wire == null) {
      if (state.pins[index] != null) {
        heldPin = index
        heldPalette = -1
      }
      return
    }
    if (heldPin == index) {
      heldPin = -1
      return
    }
    place(wire, heldPin.takeIf { it >= 0 }, index)
    heldPalette = -1
    heldPin = -1
  }

  fun onTest() {
    if (state.step != CrimpStep.TEST || state.passed) return
    state = state.copy(
      ledsLit = 0,
      passed = true,
      status = "PASS · ${modeLabel()} wire map OK",
      flashError = null,
    )
    dialog = true to (
      "Wire-map PASS" to
        if (state.mode == CableMode.STRAIGHT) {
          "LEDs 1–8 light in order. This straight-through cable is good."
        } else {
          "Crossover map checks out (1↔3, 2↔6). Ready for a like-to-like link."
        }
      )
  }

  if (dialog != null) {
    val (ok, msg) = dialog!!
    RuleFeedbackDialog(
      correct = ok,
      title = msg.first,
      body = msg.second,
      onDismiss = { dialog = null },
    )
  }

  val showModes = allowModeSwitch && hints && state.step == CrimpStep.SELECT && !state.passed

  ComposeLabScaffold(
    navController = navController,
    eyebrow = "CRIMP LAB · ${if (hints) "Guided Simulation" else "Scenario Assessment"}",
    title = "Module 2 · ${modeLabel()}",
    progressLabel = state.progressLabel,
    coachTitle = state.step.title,
    coachBody = state.step.coachFor(state.mode),
    nextStep = if (state.passed) "Cable certified — continue to the next station" else state.step.nextStepLabel,
    status = state.status,
    statusOk = state.passed,
    primaryLabel = state.step.nextStepLabel,
    primaryEnabled = state.passed,
    onPrimary = {
      dialog = false to (
        "Use the bench" to
          "Drag the tool on the 2D bench. The button does not skip strip, untwist, or crimp."
        )
    },
    onReset = {
      dialog = null
      heldPalette = -1
      heldPin = -1
      state = freshBench(forcedMode ?: state.mode, "Bench cleared — drag the Cat 6 coil onto the mat")
    },
    showCoach = hints,
    embedded = embedded,
    onBack = onLeave,
    onComplete = onStationComplete,
    completeLabel = if (onStationComplete != null) "Next station ▶" else "Back to Home",
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      CrimpBench(
        state = state,
        hints = hints,
        topInset = if (showModes) 40.dp else 6.dp,
        heldPalette = heldPalette,
        heldPin = heldPin,
        onCableDropped = { onCableDropped() },
        onCableMissed = {
          if (state.step == CrimpStep.SELECT) {
            state = state.copy(status = "Drop the coil on the bench mat")
          }
        },
        onStrip = { onStrip(it) },
        onUntwist = { index, amount -> onUntwist(index, amount) },
        onInsert = { onInsert(it) },
        onCrimp = { onCrimp(it) },
        onTest = { onTest() },
        onPlace = { wire, fromPin, pin -> place(wire, fromPin, pin) },
        onReturn = { onReturn(it) },
        onTrayTapped = { onTrayTapped(it) },
        onPinTapped = { onPinTapped(it) },
      )
      if (showModes) {
        Row(
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          ModeChip("Straight T568B", state.mode == CableMode.STRAIGHT) {
            dialog = null
            state = freshBench(CableMode.STRAIGHT, "Straight-through — drag the coil onto the mat")
          }
          ModeChip("Crossover A/B", state.mode == CableMode.CROSSOVER) {
            dialog = null
            state = freshBench(CableMode.CROSSOVER, "Crossover — drag the coil onto the mat")
          }
        }
      }
    }
  }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
  val interaction = remember { MutableInteractionSource() }
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (selected) Primary.copy(alpha = 0.35f) else Color(0xFF003247))
      .border(1.dp, if (selected) Primary else White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
      .clickable(interactionSource = interaction, indication = null, onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 5.dp),
  ) {
    Text(label, color = White, fontFamily = GSFlex, fontSize = 11.sp)
  }
}
