package com.example.smart_build.screens.serverlab

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
import com.example.smart_build.screens.packettracer.defaultLayoutFor
import com.example.smart_build.screens.packettracer.evaluateLink
import com.example.smart_build.screens.packettracer.matches
import com.example.smart_build.ui.RuleFeedbackDialog
import com.example.smart_build.ui.theme.White
import kotlinx.coroutines.delay

private val PALETTE = listOf(PtDeviceKind.SWITCH, PtDeviceKind.SERVER, PtDeviceKind.PC)

/** Required topology hops — Copper Straight-Through only. */
private val REQUIRED = listOf(
  "pc" to "switch",
  "server" to "switch",
)

@Composable
fun ServerTopologyScreen(
  navController: NavHostController,
  hints: Boolean = true,
  embedded: Boolean = false,
  startFaulted: Boolean = false,
  onStationComplete: (() -> Unit)? = null,
  onLeave: (() -> Unit)? = null,
) {
  var devices by remember {
    mutableStateOf(
      if (startFaulted) {
        listOf(
          PtPlacedDevice("pc", PtDeviceKind.PC, 0.18f, 0.72f, "PC0"),
          PtPlacedDevice("server", PtDeviceKind.SERVER, 0.82f, 0.72f, "Server0"),
        )
      } else {
        emptyList()
      },
    )
  }
  var cables by remember {
    mutableStateOf(
      if (startFaulted) {
        listOf(
          PtCable(
            id = "bad1",
            fromId = "pc",
            toId = "server",
            tool = PtCableTool.CROSSOVER,
            fromPort = "Fa0",
            toPort = "Fa0",
            linkUp = false,
            ruleCorrect = false,
          ),
        )
      } else {
        emptyList()
      },
    )
  }
  var tool by remember { mutableStateOf(PtCableTool.SELECT) }
  var pendingFrom by remember { mutableStateOf<String?>(null) }
  var selectedId by remember { mutableStateOf<String?>(null) }
  var phase by remember { mutableStateOf(0) }
  var status by remember {
    mutableStateOf(
      if (startFaulted) {
        "Fault: the switch is missing and the PC is cabled straight to the server. Rebuild the LAN."
      } else {
        "Place Switch, Server, and PC. Select lets you drag them."
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
  var pcIp by remember { mutableStateOf("") }
  var pcMask by remember { mutableStateOf("") }
  var srvIp by remember { mutableStateOf("") }
  var srvMask by remember { mutableStateOf("") }
  var pcReady by remember { mutableStateOf(false) }
  var srvReady by remember { mutableStateOf(false) }

  LaunchedEffect(celebrateId) {
    if (celebrateId != null) {
      delay(1800)
      celebrateId = null
    }
  }

  fun reset() {
    dialog = null
    devices = emptyList()
    cables = emptyList()
    tool = PtCableTool.SELECT
    pendingFrom = null
    selectedId = null
    phase = 0
    passed = false
    celebrateId = null
    deskHost = null
    deskPage = LanDeskPage.HOME
    pcIp = ""
    pcMask = ""
    srvIp = ""
    srvMask = ""
    pcReady = false
    srvReady = false
    status = "Workspace cleared. Place Switch, Server, and PC."
  }

  fun applyAddress(host: String, ip: String, mask: String) {
    val want = if (host == "pc") "192.168.10.20" else "192.168.10.10"
    val who = if (host == "pc") "PC0" else "Server0"
    val ipNorm = ip.trim()
    val maskNorm = mask.trim()
    val octets = ipNorm.split('.')
    val valid = octets.size == 4 && octets.all { part ->
      part.isNotEmpty() && part.all { it.isDigit() } && part.toInt() in 0..255
    }
    when {
      !valid -> {
        dialog = false to (
          "Not an IP address" to
            "Type four numbers from 0 to 255, separated by dots."
          )
      }
      maskNorm != "255.255.255.0" -> {
        dialog = false to (
          "Wrong subnet mask" to
            "Use 255.255.255.0 on both devices so they share the same LAN."
          )
      }
      ipNorm != want -> {
        dialog = false to (
          "Wrong address" to
            "$who must use $want. The PC is 192.168.10.20 and the Server is 192.168.10.10."
          )
      }
      else -> {
        if (host == "pc") {
          pcIp = ipNorm
          pcMask = maskNorm
          pcReady = true
        } else {
          srvIp = ipNorm
          srvMask = maskNorm
          srvReady = true
        }
        val both = (host == "pc" && srvReady) || (host != "pc" && pcReady)
        dialog = true to (
          "Address saved" to
            if (both) {
              "$who is $want. Open Command Prompt on PC0 and type ping 192.168.10.10."
            } else {
              "$who is $want. Set the address on the other device next."
            }
          )
      }
    }
  }

  fun placeDevice(kind: PtDeviceKind) {
    if (phase > 0 || passed) return
    if (devices.any { it.kind == kind }) return
    val id = when (kind) {
      PtDeviceKind.SWITCH -> "switch"
      PtDeviceKind.SERVER -> "server"
      PtDeviceKind.PC -> "pc"
      else -> kind.name.lowercase()
    }
    val pos = defaultLayoutFor(kind, devices.size)
    val hostname = when (kind) {
      PtDeviceKind.SWITCH -> "Switch0"
      PtDeviceKind.SERVER -> "Server0"
      PtDeviceKind.PC -> "PC0"
      else -> kind.shortLabel
    }
    devices = devices + PtPlacedDevice(id, kind, pos.x, pos.y, hostname)
    status = "Placed $hostname (${devices.size} of 3). Drag with Select to move it."
  }

  fun removeCableBetween(a: String, b: String) {
    cables = cables.filterNot { it.matches(a, b) }
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
        status = "Removed cables on ${devices.find { it.id == id }?.hostname}"
        pendingFrom = null
      }
      PtCableTool.STRAIGHT, PtCableTool.CROSSOVER -> {
        if (phase < 1 && devices.size < 3) {
          dialog = false to (
            "Place devices first" to
              "Add Switch, Server, and PC on the workspace before you connect cables."
            )
          return
        }
        val from = pendingFrom
        if (from == null) {
          pendingFrom = id
          status = "Now tap the second device to connect"
        } else if (from == id) {
          pendingFrom = null
          status = "Cancelled — pick first device again"
        } else {
          removeCableBetween(from, id)
          cableSeq += 1
          val fromDev = devices.find { it.id == from } ?: return
          val toDev = devices.find { it.id == id } ?: return
          val (linkUp, ruleCorrect) = evaluateLink(fromDev.kind, toDev.kind, tool)
          val newId = "c$cableSeq"
          val portA = if (fromDev.kind == PtDeviceKind.PC) "Fa0" else "Fa0/1"
          val portB = if (toDev.kind == PtDeviceKind.SWITCH) "Fa0/${cables.size + 1}" else "Fa0"
          cables = cables + PtCable(
            id = newId,
            fromId = from,
            toId = id,
            tool = tool,
            fromPort = portA,
            toPort = portB,
            linkUp = linkUp,
            ruleCorrect = ruleCorrect,
          )
          pendingFrom = null
          val typeName = if (tool == PtCableTool.STRAIGHT) "Straight-Through" else "Cross-Over"
          status = "${fromDev.hostname} to ${toDev.hostname} · $typeName" +
            if (ruleCorrect) " · LINK UP" else " · WRONG MEDIA"

          val pair = setOf(from, id)
          val isRequiredHop = pair == setOf("pc", "switch") || pair == setOf("server", "switch")
          val isWrongDirect = pair == setOf("pc", "server")
          when {
            isWrongDirect -> {
              dialog = false to (
                "Wrong link" to
                  "Do not connect the PC straight to the Server.\n" +
                  "Both must connect to the Switch with a straight-through cable.\n" +
                  "Fix: choose Delete, tap a device, then reconnect Straight-Through to the Switch."
                )
            }
            isRequiredHop && !ruleCorrect -> {
              dialog = false to (
                "Wrong cable type" to
                  "A PC or Server connected to a Switch needs a straight-through cable (solid line).\n" +
                  "Crossover (dashed orange) stays red.\n" +
                  "Fix: Delete that cable, choose Straight-Through, and connect again."
                )
            }
            ruleCorrect && isRequiredHop -> {
              celebrateId = newId
              dialog = true to (
                "Link up" to
                  "Straight-through is correct. The cable glows green."
                )
            }
            ruleCorrect -> {
              celebrateId = newId
            }
            else -> {
              dialog = false to (
                "Incorrect media" to
                  "Cable type does not match device classes. Use Straight-Through for unlike devices."
                )
            }
          }
        }
      }
      PtCableTool.SELECT -> {
        val d = devices.find { it.id == id }
        if (phase >= 2 && (id == "pc" || id == "server")) {
          deskHost = id
          deskPage = LanDeskPage.HOME
          status = "Opened ${d?.hostname}. Set the IP, then ping from the PC."
        } else if (phase >= 2 && id == "switch") {
          dialog = false to (
            "Switch has no IP" to
              "This switch only joins the cables. Set the address on PC0 and on Server0."
            )
        } else {
          status = "${d?.hostname} selected. Drag to move it."
        }
      }
    }
  }

  fun onPrimary() {
    when (phase) {
      0 -> {
        val ids = devices.map { it.id }.toSet()
        if (ids != setOf("switch", "server", "pc")) {
          dialog = false to (
            "Incomplete topology" to
              "Place Switch0, Server0, and PC0 from the Devices bar."
            )
          status = "Need all three devices on the workspace"
          return
        }
        phase = 1
        tool = PtCableTool.STRAIGHT
        status = "Straight-Through: tap one device, then the other. Green means the cable is correct."
        dialog = true to (
          "Devices placed" to
            "Choose Straight-Through. Connect PC to Switch, then Server to Switch. A crossover cable stays red."
          )
      }
      1 -> {
        val good = REQUIRED.all { (a, b) ->
          cables.any { it.matches(a, b) && it.ruleCorrect && it.tool == PtCableTool.STRAIGHT }
        }
        val hasBad = cables.any { !it.ruleCorrect }
        if (!good || hasBad) {
          dialog = false to (
            "Cabling incomplete or wrong" to
              "Need a green link on both:\n• PC to Switch (straight-through)\n• Server to Switch (straight-through)\n" +
              "Delete red/orange wrong cables first."
            )
          status = "Fix cables until both hops show green pulse"
          return
        }
        phase = 2
        tool = PtCableTool.SELECT
        status = "Cables are up. Open PC0 and Server0 and type their IP addresses."
        dialog = true to (
          "Cabling OK" to
            "Both straight-through links are up.\n" +
            "Tap PC0, open IP Configuration, and type 192.168.10.20 with mask 255.255.255.0.\n" +
            "Tap Server0 and type 192.168.10.10 with the same mask.\n" +
            "Then open Command Prompt on PC0 and type ping 192.168.10.10."
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

  val linksReady = REQUIRED.all { (a, b) ->
    cables.any { it.matches(a, b) && it.ruleCorrect && it.tool == PtCableTool.STRAIGHT }
  } && cables.none { !it.ruleCorrect }
  val nowLine = when (phase) {
    0 -> "Tap Switch, Server, and PC on the bar (${devices.size} of 3). Select, then drag a device to move it."
    1 -> "Choose Straight. Tap PC, then Switch. Tap Server, then Switch. A correct cable glows green."
    else -> when {
      passed -> "Ping succeeded. Continue to the file server."
      !pcReady -> "Tap PC0. Open IP Configuration. Type 192.168.10.20 and mask 255.255.255.0, then OK."
      !srvReady -> "Tap Server0. Open IP Configuration. Type 192.168.10.10 and mask 255.255.255.0, then OK."
      else -> "On PC0, open Command Prompt. Type ping 192.168.10.10 and press Enter."
    }
  }
  val actionLabel = when {
    passed -> "Next: file server"
    phase == 0 && devices.map { it.id }.toSet() == setOf("switch", "server", "pc") -> "Lock placement"
    phase == 1 && linksReady -> "Lock cabling"
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
          PtCableTool.STRAIGHT -> "Straight-Through — tap device, then device"
          PtCableTool.CROSSOVER -> "Crossover stays red between a PC or Server and the Switch"
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
      val isPc = host == "pc"
      LanDeviceDesk(
        hostname = if (isPc) "PC0" else "Server0",
        isPc = isPc,
        configured = if (isPc) pcReady else srvReady,
        currentIp = if (isPc) pcIp else srvIp,
        currentMask = if (isPc) pcMask else srvMask,
        canPing = pcReady && srvReady,
        page = deskPage,
        onPage = { deskPage = it },
        onApply = { ip, mask -> applyAddress(host, ip, mask) },
        onPingOk = {
          if (!passed) {
            passed = true
            status = "Ping 192.168.10.10 succeeded."
            dialog = true to (
              "Ping success" to
                "PC 192.168.10.20 reached Server 192.168.10.10 through the Switch. Continue to the file server setup."
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
            "Goal: Set the PC and Server addresses, then ping 192.168.10.10 from the PC.",
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
        LanChip("Back") { leave() }
      }
    } else {
      Row(
        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        LanChip("Guide") { guideVisible = true }
        LanChip("Reset") { reset() }
        LanChip("${phase + 1}/3") {}
        LanChip("Back") { leave() }
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
private fun LanChip(label: String, onClick: () -> Unit) {
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
