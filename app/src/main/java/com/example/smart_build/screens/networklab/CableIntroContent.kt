package com.example.smart_build.screens.networklab

/**
 * Page before straight-through / crossover practice (panelist request).
 */
data class CableIntroPoint(
  val title: String,
  val body: String,
)

val CABLE_INTRO_POINTS = listOf(
  CableIntroPoint(
    "Straight-through (T568B–T568B)",
    "Same pin order on both ends. Used for PC→switch, switch→router, and most office drops. " +
      "Transmit and receive stay on their pairs end-to-end.",
  ),
  CableIntroPoint(
    "Crossover (T568A–T568B)",
    "Ends use different standards so TX/RX pairs swap. Classic use: PC→PC or switch→switch " +
      "when Auto-MDI-X is off. Modern gear often auto-corrects, but CSS NC II still trains the wiring.",
  ),
  CableIntroPoint(
    "How to tell them apart",
    "Compare pin 1 colors: both white/orange → straight (T568B both). " +
      "Pin 1 white/green on one end and white/orange on the other → crossover.",
  ),
)
