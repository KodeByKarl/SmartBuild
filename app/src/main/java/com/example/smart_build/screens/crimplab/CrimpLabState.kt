package com.example.smart_build.screens.crimplab

/**
 * Crimping bench — hands-on actions, not click-to-advance.
 * Student must strip, fan, place pins 1–8 correctly, insert, crimp, then test.
 */

enum class CableMode {
  STRAIGHT,
  CROSSOVER,
}

enum class CrimpStep(
  val id: String,
  val title: String,
  val nextStepLabel: String,
) {
  SELECT("select_cable", "1 · Select Cat 6 UTP", "Drag the cable coil onto the bench mat"),
  STRIP("strip_jacket", "2 · Strip jacket ~25 mm", "Drag the stripper along the jacket"),
  UNTWIST("untwist_pairs", "3 · Untwist the pairs", "Swipe each twisted pair until it lies flat"),
  ORDER_A("order_end_a", "4 · Order End A", "Drag conductors into the RJ45 channels"),
  INSERT_A("insert_end_a", "5 · Seat End A", "Push the conductors up into the plug"),
  CRIMP_A("crimp_end_a", "6 · Crimp End A", "Squeeze the crimper handles closed"),
  ORDER_B("order_end_b", "7 · Order End B", "Drag End B conductors into the plug"),
  INSERT_B("insert_end_b", "8 · Seat End B", "Push End B into the second plug"),
  CRIMP_B("crimp_end_b", "9 · Crimp End B", "Squeeze the crimper on End B"),
  TEST("test_cable", "10 · LED wire map", "Press the button on the cable tester"),
}

fun CrimpStep.coachFor(mode: CableMode): String = when (this) {
  CrimpStep.SELECT ->
    "Drag the Cat 6 coil onto the mat — ${if (mode == CableMode.STRAIGHT) "straight-through, T568B both ends" else "crossover, T568A then T568B"}."
  CrimpStep.STRIP ->
    "Drag the stripper from the cable end along the jacket until ~25 mm is peeled."
  CrimpStep.UNTWIST ->
    "Swipe each twisted pair until the two conductors lie straight and side by side."
  CrimpStep.ORDER_A -> if (mode == CableMode.STRAIGHT) {
    "Drag each color into the RJ45. T568B: WO O WG Bl WBl G WBr Br. A wrong pin is flagged at once."
  } else {
    "Drag each color into the RJ45. End A is T568A: WG G WO Bl WBl O WBr Br."
  }
  CrimpStep.INSERT_A ->
    "Drag the fanned conductors up into the plug until they reach the gold contacts."
  CrimpStep.CRIMP_A ->
    "Drag the crimper handles together until the ratchet closes on End A."
  CrimpStep.ORDER_B -> if (mode == CableMode.STRAIGHT) {
    "End B is T568B again. Drop a new color on a filled pin to replace it."
  } else {
    "End B is T568B so the orange and green pairs cross. Replace a pin by dropping on it."
  }
  CrimpStep.INSERT_B ->
    "Push End B up into the plug — jacket stays under the boot."
  CrimpStep.CRIMP_B ->
    "Squeeze the crimper to lock End B, then the tester is next."
  CrimpStep.TEST -> if (mode == CableMode.STRAIGHT) {
    "Press TEST. Straight-through lights LEDs 1–8 in order."
  } else {
    "Press TEST. Crossover map checks the crossed pairs (1↔3, 2↔6)."
  }
}

fun prettyWire(label: String): String = when (label) {
  "WO" -> "White/Orange"
  "O" -> "Orange"
  "WG" -> "White/Green"
  "Bl" -> "Blue"
  "WBl" -> "White/Blue"
  "G" -> "Green"
  "WBr" -> "White/Brown"
  "Br" -> "Brown"
  else -> label
}

data class PlacementResult(
  val pins: List<WireColor?>,
  val palette: List<WireColor>,
  val correctPin: Boolean,
  val allCorrect: Boolean,
  val expectedLabel: String,
)

/** Place or replace one pin. The previous conductor returns to the tray. */
fun applyPlacement(
  pins: List<WireColor?>,
  palette: List<WireColor>,
  expected: List<WireColor>,
  wire: WireColor,
  fromPin: Int?,
  pinIndex: Int,
): PlacementResult {
  val expectedLabel = expected[pinIndex].label
  if (fromPin == pinIndex) {
    val already = pins.getOrNull(pinIndex)?.label == wire.label
    return PlacementResult(
      pins = pins,
      palette = palette,
      correctPin = already && wire.label == expectedLabel,
      allCorrect = false,
      expectedLabel = expectedLabel,
    )
  }
  val newPins = pins.toMutableList()
  val newPalette = palette.toMutableList()
  if (fromPin != null) newPins[fromPin] = null
  else {
    val palIdx = newPalette.indexOfFirst { it.label == wire.label }
    if (palIdx >= 0) newPalette.removeAt(palIdx)
  }
  val displaced = newPins[pinIndex]
  if (displaced != null && displaced.label != wire.label) newPalette.add(displaced)
  newPins[pinIndex] = wire
  val correct = wire.label == expectedLabel
  val all = newPins.map { it?.label } == expected.map { it.label }
  return PlacementResult(newPins, newPalette, correct, all, expectedLabel)
}

/** Wire identity for ordering (stable across shuffle). */
data class WireColor(val argb: Long, val label: String)

val T568B_WIRES = listOf(
  WireColor(0xFFE8E0A0L, "WO"),
  WireColor(0xFFE67E22L, "O"),
  WireColor(0xFFA8E6A0L, "WG"),
  WireColor(0xFF3498DBL, "Bl"),
  WireColor(0xFFAED6F1L, "WBl"),
  WireColor(0xFF27AE60L, "G"),
  WireColor(0xFFD7BDE2L, "WBr"),
  WireColor(0xFF8B4513L, "Br"),
)

val T568A_WIRES = listOf(
  WireColor(0xFFA8E6A0L, "WG"),
  WireColor(0xFF27AE60L, "G"),
  WireColor(0xFFE8E0A0L, "WO"),
  WireColor(0xFF3498DBL, "Bl"),
  WireColor(0xFFAED6F1L, "WBl"),
  WireColor(0xFFE67E22L, "O"),
  WireColor(0xFFD7BDE2L, "WBr"),
  WireColor(0xFF8B4513L, "Br"),
)

/** Legacy chip lists for any remaining call sites. */
val T568B_COLORS = T568B_WIRES.map { it.argb to it.label }
val T568A_COLORS = T568A_WIRES.map { it.argb to it.label }

fun expectedWires(mode: CableMode, endA: Boolean): List<WireColor> = when {
  endA && mode == CableMode.CROSSOVER -> T568A_WIRES
  else -> T568B_WIRES
}

/** Scramble palette so correct order is not free. */
fun shuffledPalette(seed: Int = (System.currentTimeMillis() % 10000).toInt()): List<WireColor> {
  val list = T568B_WIRES.toMutableList()
  // Deterministic-ish Fisher–Yates from seed so Reset gets a new mix.
  var s = seed.xor(0x5f3759df)
  for (i in list.lastIndex downTo 1) {
    s = (s * 1103515245 + 12345)
    val j = ((s ushr 16) and 0x7fff) % (i + 1)
    val tmp = list[i]
    list[i] = list[j]
    list[j] = tmp
  }
  return list
}

data class CrimpLabUiState(
  val mode: CableMode = CableMode.STRAIGHT,
  val stepIndex: Int = 0,
  val status: String = "CRIMP LAB · Straight-through T568B",
  val ledsLit: Int = 0,
  val passed: Boolean = false,
  val flashError: String? = null,
  /** Loose wires still available to place (ORDER steps). */
  val palette: List<WireColor> = shuffledPalette(),
  /** Pins 1–8 for the active end; null = empty. */
  val pins: List<WireColor?> = List(8) { null },
  val endACorrect: Boolean = false,
  val endBCorrect: Boolean = false,
  /** 0–1 jacket peel while dragging the stripper. */
  val stripProgress: Float = 0f,
  /** 0–1 untwist amount for orange, green, blue, brown. */
  val untwist: List<Float> = List(4) { 0f },
  val insertProgress: Float = 0f,
  val crimpProgress: Float = 0f,
  /** Guided highlight: tray wire that belongs in the pin just missed. */
  val revealLabel: String? = null,
) {
  val step: CrimpStep get() = CrimpStep.entries[stepIndex.coerceIn(0, CrimpStep.entries.lastIndex)]
  val progressLabel: String get() = "${stepIndex + 1} / ${CrimpStep.entries.size}"
  val pinsFull: Boolean get() = pins.all { it != null }
  val orderingEndA: Boolean get() = step == CrimpStep.ORDER_A
  val orderingEndB: Boolean get() = step == CrimpStep.ORDER_B
  val isOrdering: Boolean get() = orderingEndA || orderingEndB
}
