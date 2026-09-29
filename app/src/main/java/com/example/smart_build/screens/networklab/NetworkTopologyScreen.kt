package com.example.smart_build.screens.networklab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.smart_build.screens.composelabs.ComposeLabScaffold
import com.example.smart_build.ui.RuleFeedbackDialog
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

private data class NetDevice(val id: String, val label: String)

private val ORDER = listOf(
  NetDevice("modem", "Modem"),
  NetDevice("router", "Router"),
  NetDevice("switch", "Switch"),
  NetDevice("ap", "Access Point"),
  NetDevice("pc", "PC"),
)

private val LINKS = listOf(
  "modem" to "router",
  "router" to "switch",
  "switch" to "ap",
  "switch" to "pc",
)

@Composable
fun NetworkTopologyScreen(
  navController: NavHostController,
  hints: Boolean = true,
  embedded: Boolean = false,
  /** Assessment: bench already wrong — student must identify and rebuild. */
  startFaulted: Boolean = false,
  onStationComplete: (() -> Unit)? = null,
  onLeave: (() -> Unit)? = null,
) {
  var placed by remember {
    mutableStateOf(
      if (startFaulted) listOf("pc", "modem", "switch") else emptyList(),
    )
  }
  var links by remember { mutableStateOf(listOf<Pair<String, String>>()) }
  var phase by remember { mutableStateOf(0) }
  var status by remember {
    mutableStateOf(
      if (startFaulted) {
        "FAULT: devices out of order / incomplete. Reset or rebuild modem→…→PC correctly."
      } else {
        "Place devices in ISP → LAN order."
      },
    )
  }
  var passed by remember { mutableStateOf(false) }
  var dialog by remember { mutableStateOf<Pair<Boolean, Pair<String, String>>?>(null) }

  val steps = listOf(
    "Place modem → router → switch → AP → PC",
    "Cable the required links (straight-through)",
    "Verify link lights / path",
  )
  val pathLabel = if (hints) "Guided Simulation" else "Scenario Assessment"

  fun reset() {
    dialog = null
    placed = emptyList()
    links = emptyList()
    phase = 0
    passed = false
    status = "Bench cleared — place devices in ISP → LAN order."
  }

  fun onPrimary() {
    when (phase) {
      0 -> {
        if (placed.size < ORDER.size) {
          status = "Still missing devices — tap palette in order."
          dialog = false to (
            "Incomplete placement" to
              "You placed: ${if (placed.isEmpty()) "(none)" else placed.joinToString(" → ")}. " +
              "Expected: modem → router → switch → AP → PC."
            )
          return
        }
        if (placed != ORDER.map { it.id }) {
          status = "Incorrect device order."
          dialog = false to (
            "Wrong order" to
              "You placed: ${placed.joinToString(" → ")}\n" +
              "Expected: modem → router → switch → AP → PC\n" +
              "Why? ISP edge (modem) must come first so the LAN can get a route.\n" +
              "Fix: Reset or remove devices and place again in order."
            )
          return
        }
        phase = 1
        status = "Tap each required cable hop to light the link."
        dialog = true to ("Placement locked" to "Devices are in ISP → LAN order. Cable the four hops next.")
      }
      1 -> {
        if (links.toSet() != LINKS.toSet()) {
          status = "Links incomplete."
          dialog = false to (
            "Cabling incomplete" to
              "Need modem–router, router–switch, switch–AP, and switch–PC (all straight-through). " +
              "Use Remove on a single wrong cable without resetting the whole bench."
            )
          return
        }
        phase = 2
        status = "Path OK — run verify to confirm link lights."
        dialog = true to ("Cabling locked" to "All required straight-through links are up.")
      }
      2 -> {
        passed = true
        status = "PASS · Cisco-lite topology up"
        dialog = true to ("Path verified" to "Modem → router → switch → AP/PC link lights are good.")
      }
    }
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

  ComposeLabScaffold(
    navController = navController,
    eyebrow = "TOPOLOGY · $pathLabel",
    title = "Module 2 · modem → PC path",
    progressLabel = "${phase + 1} / 3",
    coachTitle = "DO THIS · ${steps[phase]}",
    coachBody = when (phase) {
      0 -> "Add devices from the palette in the correct edge-to-core order."
      1 -> "Create the four required hops. Unlike devices use straight-through."
      else -> "Confirm the path from modem to PC shows link lights."
    },
    nextStep = when {
      passed -> "Topology complete — finish the module"
      phase == 0 -> if (placed.size < ORDER.size) {
        "Place next device (${placed.size}/${ORDER.size})"
      } else {
        "Lock placement when order looks correct"
      }
      phase == 1 -> if (links.size < LINKS.size) {
        "Add remaining links (${links.size}/${LINKS.size}) — or Remove one"
      } else {
        "Lock cabling"
      }
      else -> "Verify path / link lights"
    },
    status = status,
    statusOk = passed,
    primaryLabel = when (phase) {
      0 -> if (placed.size < ORDER.size) "Place next…" else "Lock placement ▶"
      1 -> if (links.size < LINKS.size) "Add links…" else "Lock cabling ▶"
      else -> "Verify path ▶"
    },
    onPrimary = { onPrimary() },
    onReset = { reset() },
    showCoach = hints,
    embedded = embedded,
    onBack = onLeave,
    onComplete = onStationComplete,
    completeLabel = if (onStationComplete != null) "Finish module ▶" else "Back to Home",
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text("Palette", color = White.copy(alpha = 0.55f), fontFamily = GSFlex, fontSize = 11.sp)
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        ORDER.forEach { device ->
          val already = device.id in placed
          val interaction = remember { MutableInteractionSource() }
          Box(
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (already) Color(0xFF1E8449) else Color(0xFF003247))
              .clickable(
                enabled = phase == 0 && !already && !passed,
                interactionSource = interaction,
                indication = null,
              ) {
                placed = placed + device.id
                status = "Placed ${device.label} (${placed.size}/${ORDER.size})"
              },
            contentAlignment = Alignment.Center,
          ) {
            Text(device.label, color = White, fontSize = 11.sp, fontFamily = GSFlex)
          }
        }
      }

      Text("Rack / bench", color = White.copy(alpha = 0.55f), fontFamily = GSFlex, fontSize = 11.sp)
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        placed.forEach { id ->
          val label = ORDER.first { it.id == id }.label
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0A3A4A))
              .border(1.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .padding(10.dp),
          ) {
            Text(label, color = White, fontFamily = GSCode, fontWeight = FontWeight.Medium, fontSize = 13.sp)
          }
        }
        if (placed.isEmpty()) {
          Text("Empty — tap palette devices.", color = White.copy(alpha = 0.45f), fontFamily = GSFlex, fontSize = 12.sp)
        }
      }

      if (phase >= 1) {
        Text("Required links", color = White.copy(alpha = 0.55f), fontFamily = GSFlex, fontSize = 11.sp)
        LINKS.forEach { link ->
          val done = link in links
          val a = ORDER.first { it.id == link.first }.label
          val b = ORDER.first { it.id == link.second }.label
          val interaction = remember { MutableInteractionSource() }
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (done) Color(0xFF145A32) else Color(0xFF1A2F38))
                .clickable(
                  enabled = phase == 1 && !done && !passed,
                  interactionSource = interaction,
                  indication = null,
                ) {
                  links = links + link
                  status = "Linked $a ↔ $b (${links.size}/${LINKS.size}) · tap Remove to undo one link"
                }
                .padding(10.dp),
            ) {
              Text(
                text = "${if (done) "●" else "○"}  $a  ↔  $b  (straight-through)",
                color = if (done) Color(0xFF2ECC71) else White,
                fontFamily = GSFlex,
                fontSize = 12.sp,
              )
            }
            if (done && phase == 1 && !passed) {
              Text(
                text = "Remove",
                color = Color(0xFFE74C3C),
                fontFamily = GSFlex,
                fontSize = 12.sp,
                modifier = Modifier
                  .padding(start = 8.dp)
                  .clickable {
                    links = links.filterNot { it == link }
                    status = "Removed $a ↔ $b — add the correct link when ready"
                  },
              )
            }
          }
        }
      }
    }
  }
}
