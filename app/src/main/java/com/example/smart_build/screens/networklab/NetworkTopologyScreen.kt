package com.example.smart_build.screens.networklab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.screens.packettracer.PacketTracerWorkspace
import com.example.smart_build.screens.packettracer.PtCable
import com.example.smart_build.screens.packettracer.PtCableTool
import com.example.smart_build.screens.packettracer.PtDeviceKind
import com.example.smart_build.screens.packettracer.PtPlacedDevice
import com.example.smart_build.screens.packettracer.evaluateLink
import com.example.smart_build.screens.packettracer.matches
import com.example.smart_build.screens.serverlab.LanDeskPage
import com.example.smart_build.screens.serverlab.LanDeviceDesk
import com.example.smart_build.ui.RuleFeedbackDialog
import com.example.smart_build.ui.theme.White
import kotlinx.coroutines.delay

/**
 * Module 2 network station — same hands-on flow as Module 3:
 * place the devices, cable them with the right media, type each PC's IP address,
 * then open Command Prompt and ping by hand.
 *
 * Two-switch office LAN, so BOTH cable types from the crimp stations are needed:
 *   PC0 —(straight)— Switch0 —(crossover)— Switch1 —(straight)— PC1
 */
private val PALETTE = listOf(PtDeviceKind.SWITCH, PtDeviceKind.PC, PtDeviceKind.SERVER, PtDeviceKind.ROUTER)

private val CORE_IDS = setOf("pc0", "pc1", "sw0", "sw1")

/** Ids handed out per device type. The first two PCs and switches are the graded LAN; the rest are extras. */
private val IDS_BY_KIND = mapOf(
  PtDeviceKind.SWITCH to listOf("sw0", "sw1", "sw2"),
  PtDeviceKind.PC to listOf("pc0", "pc1", "pc2", "pc3"),
  PtDeviceKind.SERVER to listOf("srv0"),
  PtDeviceKind.ROUTER to listOf("rt0"),
)

private data class Hop(val a: String, val b: String, val tool: PtCableTool)

private val REQUIRED = listOf(
  Hop("pc0", "sw0", PtCableTool.STRAIGHT),
  Hop("sw0", "sw1", PtCableTool.CROSSOVER),
  Hop("sw1", "pc1", PtCableTool.STRAIGHT),
)

private const val PC0_IP = "192.168.1.10"
private const val PC1_IP = "192.168.1.20"
private const val MASK = "255.255.255.0"

private val LAYOUT = mapOf(
  "pc0" to Offset(0.12f, 0.72f),
  "sw0" to Offset(0.38f, 0.40f),
  "sw1" to Offset(0.62f, 0.40f),
  "pc1" to Offset(0.88f, 0.72f),
  "sw2" to Offset(0.50f, 0.18f),
  "pc2" to Offset(0.30f, 0.86f),
  "pc3" to Offset(0.70f, 0.86f),
  "srv0" to Offset(0.88f, 0.30f),
  "rt0" to Offset(0.12f, 0.30f),
)

private fun hostnameOf(id: String) = when {
  id.startsWith("pc") -> "PC" + id.removePrefix("pc")
  id.startsWith("sw") -> "Switch" + id.removePrefix("sw")
  id.startsWith("srv") -> "Server" + id.removePrefix("srv")
  else -> "Router" + id.removePrefix("rt")
}

private fun faultedDevices() = listOf(
  PtPlacedDevice("pc0", PtDeviceKind.PC, LAYOUT.getValue("pc0").x, LAYOUT.getValue("pc0").y, "PC0"),
  PtPlacedDevice("pc1", PtDeviceKind.PC, LAYOUT.getValue("pc1").x, LAYOUT.getValue("pc1").y, "PC1"),
)

private fun faultedCables() = listOf(
  PtCable(
    id = "bad1",
    fromId = "pc0",
    toId = "pc1",
    tool = PtCableTool.STRAIGHT,
    fromPort = "Fa0",
    toPort = "Fa0",
    linkUp = false,
    ruleCorrect = false,
  ),
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
  var devices by remember { mutableStateOf(if (startFaulted) faultedDevices() else emptyList()) }
  var cables by remember { mutableStateOf(if (startFaulted) faultedCables() else emptyList()) }
  var tool by remember { mutableStateOf(PtCableTool.SELECT) }
  var pendingFrom by remember { mutableStateOf<String?>(null) }
  var selectedId by remember { mutableStateOf<String?>(null) }
  var phase by remember { mutableStateOf(0) }
  var status by remember {
    mutableStateOf(
      if (startFaulted) {
        "Fault: the two PCs are cabled straight to each other and there are no switches. Rebuild the LAN."
      } else {
        "Place two Switches and two PCs (a Server or Router is optional). Select lets you drag them."
      },
    )
  }
  var passed by remember { mutableStateOf(false) }
  var dialog by remember { mutableStateOf<Pair<Boolean, Pair<String, String>>?>(null) }
  var cableSeq by remember { mutableStateOf(0) }
  var celebrateId by remember { mutableStateOf<String?>(null) }
  var guideVisible by remember { mutableStateOf(true) }
  var deskHost by remember { mutableStateOf<String?>(null) }
  var deskPage by remember { mutableStateOf(LanDeskPage.HOME) }
  var pc0Ip by remember { mutableStateOf("") }
  var pc0Mask by remember { mutableStateOf("") }
  var pc1Ip by remember { mutableStateOf("") }
  var pc1Mask by remember { mutableStateOf("") }
  var pc0Ready by remember { mutableStateOf(false) }
  var pc1Ready by remember { mutableStateOf(false) }

  LaunchedEffect(celebrateId) {
    if (celebrateId != null) {
      delay(1800)
      celebrateId = null
    }
  }

  fun reset() {
    dialog = null
    devices = if (startFaulted) faultedDevices() else emptyList()
    cables = if (startFaulted) faultedCables() else emptyList()
    tool = PtCableTool.SELECT
    pendingFrom = null
    selectedId = null
    phase = 0
    passed = false
    celebrateId = null
    deskHost = null
    deskPage = LanDeskPage.HOME
    pc0Ip = ""
    pc0Mask = ""
    pc1Ip = ""
    pc1Mask = ""
    pc0Ready = false
    pc1Ready = false
    status = if (startFaulted) {
      "Scenario restored. Remove the wrong cable and rebuild the LAN."
    } else {
      "Workspace cleared. Place two Switches and two PCs."
    }
  }

  fun applyAddress(host: String, ip: String, mask: String) {
    val want = if (host == "pc0") PC0_IP else PC1_IP
    val who = hostnameOf(host)
    val ipNorm = ip.trim()
    val maskNorm = mask.trim()
    val octets = ipNorm.split('.')
    val valid = octets.size == 4 && octets.all { part ->
      part.isNotEmpty() && part.all { it.isDigit() } && part.toInt() in 0..255
    }
    when {
      !valid -> dialog = false to (
        "Not an IP address" to "Type four numbers from 0 to 255, separated by dots."
        )
      maskNorm != MASK -> dialog = false to (
        "Wrong subnet mask" to "Use $MASK on both PCs so they share the same LAN."
        )
      ipNorm != want -> dialog = false to (
        "Wrong address" to "$who must use $want. PC0 is $PC0_IP and PC1 is $PC1_IP."
        )
      else -> {
        if (host == "pc0") {
          pc0Ip = ipNorm
          pc0Mask = maskNorm
          pc0Ready = true
        } else {
          pc1Ip = ipNorm
          pc1Mask = maskNorm
          pc1Ready = true
        }
        val both = (host == "pc0" && pc1Ready) || (host == "pc1" && pc0Ready)
        dialog = true to (
          "Address saved" to
            if (both) {
              "$who is $want. Open Command Prompt on PC0 and type ping $PC1_IP."
            } else {
              "$who is $want. Set the address on the other PC next."
            }
          )
      }
    }
  }

  fun placeDevice(kind: PtDeviceKind) {
    if (phase > 0 || passed) return
    val id = IDS_BY_KIND[kind].orEmpty().firstOrNull { cid -> devices.none { it.id == cid } }
    if (id == null) {
      status = "No more ${kind.label}s can be added to the workspace."
      return
    }
    val pos = LAYOUT.getValue(id)
    devices = devices + PtPlacedDevice(id, kind, pos.x, pos.y, hostnameOf(id))
    val coreLeft = CORE_IDS.count { cid -> devices.none { it.id == cid } }
    status = "Placed ${hostnameOf(id)}." +
      if (coreLeft > 0) " $coreLeft required device(s) still to place: two Switches and two PCs." else " Drag with Select to move it."
  }

  fun onDeviceDrag(id: String, dx: Float, dy: Float) {
    devices = devices.map { d ->
      if (d.id != id) d
      else d.copy(
        x = (d.x + dx).coerceIn(0.08f, 0.92f),
        y = (d.y + dy).coerceIn(0.10f, 0.90f),
      )
    }
    selectedId = id
  }

  fun onDeviceTap(id: String) {
    if (passed) return
    selectedId = id
    when (tool) {
      PtCableTool.DELETE -> {
        cables = cables.filterNot { it.fromId == id || it.toId == id }
        status = "Removed cables on ${hostnameOf(id)}"
        pendingFrom = null
      }
      PtCableTool.STRAIGHT, PtCableTool.CROSSOVER -> {
        if (phase < 1 && !devices.map { it.id }.containsAll(CORE_IDS)) {
          dialog = false to (
            "Place devices first" to
              "Add two Switches and two PCs on the workspace before you connect cables."
            )
          return
        }
        val from = pendingFrom
        if (from == null) {
          pendingFrom = id
          status = "Now tap the second device to connect"
        } else if (from == id) {
          pendingFrom = null
          status = "Cancelled — pick the first device again"
        } else {
          cables = cables.filterNot { it.matches(from, id) }
          cableSeq += 1
          val fromDev = devices.find { it.id == from } ?: return
          val toDev = devices.find { it.id == id } ?: return
          val (_, mediaOk) = evaluateLink(fromDev.kind, toDev.kind, tool)
          val correct = mediaOk
          val newId = "c$cableSeq"
          val portA = if (fromDev.kind == PtDeviceKind.PC) "Fa0" else "Fa0/${cables.count { it.fromId == from || it.toId == from } + 1}"
          val portB = if (toDev.kind == PtDeviceKind.PC) "Fa0" else "Fa0/${cables.count { it.fromId == id || it.toId == id } + 1}"
          cables = cables + PtCable(
            id = newId,
            fromId = from,
            toId = id,
            tool = tool,
            fromPort = portA,
            toPort = portB,
            linkUp = correct,
            ruleCorrect = correct,
          )
          pendingFrom = null
          val typeName = if (tool == PtCableTool.STRAIGHT) "Straight-Through" else "Cross-Over"
          status = "${fromDev.hostname} to ${toDev.hostname} · $typeName" +
            if (correct) " · LINK UP" else " · WRONG"

          if (!mediaOk) {
            val unlike = fromDev.kind != toDev.kind
            val why = if (!unlike) {
              "Switch to Switch connects LIKE devices, so it needs a crossover cable (dashed orange).\n"
            } else {
              "A PC to a Switch connects UNLIKE devices, so it needs a straight-through cable (solid).\n"
            }
            dialog = false to ("Wrong cable type" to (why + "Fix: Delete that cable, choose the right tool, and connect again."))
          } else {
            celebrateId = newId
            val good = if (tool == PtCableTool.CROSSOVER) {
              "Crossover between like devices is correct. The cable glows green."
            } else {
              "Straight-through is correct. The cable glows green."
            }
            dialog = true to ("Link up" to good)
          }
        }
      }
      PtCableTool.SELECT -> {
        if (phase >= 2 && (id == "pc0" || id == "pc1")) {
          deskHost = id
          deskPage = LanDeskPage.HOME
          status = "Opened ${hostnameOf(id)}. Set the IP, then ping from the PC."
        } else if (phase >= 2) {
          dialog = false to (
            "Not used in the ping test" to
              "Set the address on PC0 and on PC1 — they are the two computers in the test."
            )
        } else {
          status = "${hostnameOf(id)} selected. Drag to move it."
        }
      }
    }
  }

  fun cablingGood() = REQUIRED.all { hop ->
    cables.any { it.matches(hop.a, hop.b) && it.ruleCorrect && it.tool == hop.tool }
  } && cables.none { !it.ruleCorrect }

  fun onPrimary() {
    when (phase) {
      0 -> {
        val ids = devices.map { it.id }.toSet()
        if (!ids.containsAll(CORE_IDS)) {
          dialog = false to (
            "Incomplete topology" to
              "Place Switch0, Switch1, PC0, and PC1 from the Devices bar."
            )
          status = "Need all four devices on the workspace"
          return
        }
        if (cables.isNotEmpty()) {
          dialog = false to (
            "Wrong cable on the bench" to
              "Delete the cable between the two PCs first. A PC never plugs straight into another PC here."
            )
          return
        }
        phase = 1
        tool = PtCableTool.STRAIGHT
        status = "Straight-Through for PC ↔ Switch. Crossover for Switch ↔ Switch."
        dialog = true to (
          "Devices placed" to
            "Connect PC0 to Switch0 and PC1 to Switch1 with Straight-Through.\n" +
            "Connect Switch0 to Switch1 with Cross-Over. Wrong media stays red."
          )
      }
      1 -> {
        if (!cablingGood()) {
          dialog = false to (
            "Cabling incomplete or wrong" to
              "Need a green link on all three hops:\n" +
              "• PC0 to Switch0 (straight-through)\n" +
              "• Switch0 to Switch1 (crossover)\n" +
              "• Switch1 to PC1 (straight-through)\n" +
              "Delete red cables first."
            )
          status = "Fix the cables until all three hops glow green"
          return
        }
        phase = 2
        tool = PtCableTool.SELECT
        status = "Cables are up. Open PC0 and PC1 and type their IP addresses."
        dialog = true to (
          "Cabling OK" to
            "All three links are up.\n" +
            "Tap PC0, open IP Configuration, and type $PC0_IP with mask $MASK.\n" +
            "Tap PC1 and type $PC1_IP with the same mask.\n" +
            "Then open Command Prompt on PC0 and type ping $PC1_IP."
          )
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

  fun leave() {
    when {
      deskHost != null && deskPage != LanDeskPage.HOME -> deskPage = LanDeskPage.HOME
      deskHost != null -> {
        deskHost = null
        deskPage = LanDeskPage.HOME
      }
      onLeave != null -> onLeave()
      !embedded && !navController.popBackStack() -> navController.navigateUp()
    }
  }

  val allPlaced = devices.map { it.id }.toSet().containsAll(CORE_IDS)
  val nowLine = when (phase) {
    0 -> "Tap Switch twice and PC twice on the bar (${devices.size} of 4). Select, then drag a device to move it."
    1 -> "Straight: PC0 to Switch0, PC1 to Switch1. Cross-Over: Switch0 to Switch1. A correct cable glows green."
    else -> when {
      passed -> "Ping succeeded. Finish the module."
      !pc0Ready -> "Tap PC0. Open IP Configuration. Type $PC0_IP and mask $MASK, then OK."
      !pc1Ready -> "Tap PC1. Open IP Configuration. Type $PC1_IP and mask $MASK, then OK."
      else -> "On PC0, open Command Prompt. Type ping $PC1_IP and press Enter."
    }
  }
  val actionLabel = when {
    passed -> if (onStationComplete != null) "Finish module ▶" else "Back to Home"
    phase == 0 && allPlaced -> "Lock placement"
    phase == 1 && cablingGood() -> "Lock cabling"
    else -> null
  }

  BackHandler { leave() }

  Box(modifier = Modifier.fillMaxSize()) {
    PacketTracerWorkspace(
      devices = devices,
      cables = cables,
      activeTool = tool,
      onToolSelected = {
        tool = it
        pendingFrom = null
        status = when (it) {
          PtCableTool.STRAIGHT -> "Straight-Through — for unlike devices (PC ↔ Switch)"
          PtCableTool.CROSSOVER -> "Cross-Over — for like devices (Switch ↔ Switch)"
          PtCableTool.DELETE -> "Delete — tap a device to remove its cables"
          else -> "Select — drag devices to move · tap to inspect"
        }
      },
      devicePalette = PALETTE,
      onPlaceDevice = { placeDevice(it) },
      pendingFromId = pendingFrom,
      selectedDeviceId = selectedId,
      onDeviceTap = { onDeviceTap(it) },
      onDeviceDrag = { id, dx, dy -> onDeviceDrag(id, dx, dy) },
      canPlaceDevices = phase == 0 && !passed,
      celebrateCableId = celebrateId,
      statusLine = when (tool) {
        PtCableTool.STRAIGHT -> "Straight-Through"
        PtCableTool.CROSSOVER -> "Cross-Over"
        PtCableTool.DELETE -> "Delete"
        else -> "Select / Drag"
      },
      modifier = Modifier.fillMaxSize(),
    )

    val host = deskHost
    if (host != null && phase >= 2) {
      val isPc0 = host == "pc0"
      LanDeviceDesk(
        hostname = hostnameOf(host),
        isPc = true,
        configured = if (isPc0) pc0Ready else pc1Ready,
        currentIp = if (isPc0) pc0Ip else pc1Ip,
        currentMask = if (isPc0) pc0Mask else pc1Mask,
        canPing = pc0Ready && pc1Ready,
        pingTarget = if (isPc0) PC1_IP else PC0_IP,
        setupHint = "Set the IP on PC0 and PC1, then ping again.",
        page = deskPage,
        onPage = { deskPage = it },
        onApply = { ip, mask -> applyAddress(host, ip, mask) },
        onPingOk = {
          if (!passed) {
            passed = true
            val target = if (isPc0) PC1_IP else PC0_IP
            status = "Ping $target succeeded."
            dialog = true to (
              "Ping success" to
                "${hostnameOf(host)} reached $target across both switches. The network is working."
              )
          }
        },
        onClose = { deskHost = null },
      )
    }

    if (guideVisible) {
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, Color(0xFFE8B84A).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .clickable { guideVisible = false }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
          Text(
            "Goal: Cable the two-switch LAN, set both PC addresses, then ping $PC1_IP from PC0.",
            color = White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (hints) {
            Text(
              "DO THIS: $nowLine",
              color = Color(0xFFE8B84A),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Text(
            "Tap to hide    ${phase + 1}/3",
            color = White.copy(alpha = 0.75f),
            fontSize = 10.sp,
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        NetChip("Back") { leave() }
      }
    } else {
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        NetChip("Guide") { guideVisible = true }
        NetChip("Reset") { reset() }
        NetChip("${phase + 1}/3") {}
        NetChip("Back") { leave() }
      }
    }

    if (actionLabel != null) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = if (deskHost != null) 52.dp else 68.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF1E8449))
          .clickable {
            if (passed) {
              if (onStationComplete != null) onStationComplete() else leave()
            } else {
              onPrimary()
            }
          }
          .padding(horizontal = 18.dp, vertical = 10.dp),
      ) {
        Text(actionLabel, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
  }
}

@Composable
private fun NetChip(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.55f))
      .border(1.dp, White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    Text(label, color = White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
  }
}
