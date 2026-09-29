package com.example.smart_build.screens.packettracer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/** Device kinds for the Module 3 logical network (also reused by Module 2). */
enum class PtDeviceKind(val label: String, val shortLabel: String) {
  PC("PC", "PC"),
  SWITCH("Switch", "Switch"),
  SERVER("Server", "Server"),
  ROUTER("Router", "Router"),
  MODEM("Cable Modem", "Modem"),
  AP("Access Point", "AP"),
}

/**
 * Copper cable tools.
 * Straight-through = solid black; crossover = dashed orange.
 */
enum class PtCableTool(
  val label: String,
  val shortLabel: String,
  val color: Color,
  val dashed: Boolean,
) {
  SELECT("Select", "◆", Color(0xFF2C3E50), dashed = false),
  DELETE("Delete", "⌫", Color(0xFFE74C3C), dashed = false),
  STRAIGHT(
    "Copper Straight-Through",
    "——",
    Color(0xFF1A1A1A),
    dashed = false,
  ),
  CROSSOVER(
    "Copper Cross-Over",
    "- -",
    Color(0xFFE67E22),
    dashed = true,
  ),
}

data class PtPlacedDevice(
  val id: String,
  val kind: PtDeviceKind,
  /** Normalized 0–1 position on the workspace. */
  val x: Float,
  val y: Float,
  val hostname: String = kind.shortLabel,
)

data class PtCable(
  val id: String,
  val fromId: String,
  val toId: String,
  val tool: PtCableTool,
  val fromPort: String = "Fa0/1",
  val toPort: String = "Fa0/1",
  /** Green link light — only when cable type matches device classes. */
  val linkUp: Boolean = false,
  /** Rule-correct for this lab topology (drives celebration animation). */
  val ruleCorrect: Boolean = false,
)

fun PtCable.matches(a: String, b: String): Boolean =
  (fromId == a && toId == b) || (fromId == b && toId == a)

/**
 * Cable media rules (simplified):
 * - Unlike classes (PC↔Switch, Server↔Switch, …) → Straight-Through
 * - Like classes (PC↔PC, Switch↔Switch, …) → Cross-Over
 * - PC↔Server direct is not used in Module 3 LAN design → never correct
 */
fun expectedCable(a: PtDeviceKind, b: PtDeviceKind): PtCableTool? {
  val pair = setOf(a, b)
  if (pair == setOf(PtDeviceKind.PC, PtDeviceKind.SERVER)) return null
  val like = a == b
  return if (like) PtCableTool.CROSSOVER else PtCableTool.STRAIGHT
}

fun evaluateLink(
  fromKind: PtDeviceKind,
  toKind: PtDeviceKind,
  tool: PtCableTool,
): Pair<Boolean /*linkUp*/, Boolean /*ruleCorrect*/> {
  if (tool != PtCableTool.STRAIGHT && tool != PtCableTool.CROSSOVER) {
    return false to false
  }
  val expected = expectedCable(fromKind, toKind) ?: return false to false
  val correct = tool == expected
  return correct to correct
}

/** Classic PT workspace gray. */
val PtWorkspaceBg = Color(0xFFC5C5C5)
val PtWorkspaceGrid = Color(0xFFB0B0B0)
val PtPanelBg = Color(0xFF2D2D30)
val PtToolbarBg = Color(0xFFE8E8E8)
val PtDeviceFill = Color(0xFFF4F4F4)
val PtDeviceBorder = Color(0xFF3A3A3A)
val PtAccent = Color(0xFF0078D4)
val PtLinkGreen = Color(0xFF2ECC71)
val PtLinkRed = Color(0xFFE74C3C)

fun defaultLayoutFor(kind: PtDeviceKind, index: Int): Offset = when (kind) {
  PtDeviceKind.SWITCH -> Offset(0.50f, 0.42f)
  PtDeviceKind.PC -> Offset(0.18f, 0.72f)
  PtDeviceKind.SERVER -> Offset(0.82f, 0.72f)
  PtDeviceKind.ROUTER -> Offset(0.50f, 0.22f)
  PtDeviceKind.MODEM -> Offset(0.20f, 0.22f)
  PtDeviceKind.AP -> Offset(0.80f, 0.42f)
}.let { base ->
  Offset(base.x + (index % 3) * 0.02f, base.y + (index % 2) * 0.03f)
}
