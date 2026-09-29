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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.ui.RuleFeedbackDialog
import com.example.smart_build.ui.theme.White

internal enum class SrvWindow { NONE, ROLES, EXPLORER, GROUPS, SECURITY, SHARING, PC_FILES }

internal data class ServerConfig(
  val fileServerRole: Boolean = false,
  val folderHr: Boolean = false,
  val folderIt: Boolean = false,
  val groupHr: Boolean = false,
  val groupIt: Boolean = false,
  val hrAclGroup: String? = null,
  val itAclGroup: String? = null,
  val shareOn: Boolean = false,
  val shareChangeBoth: Boolean = false,
  val allowOk: Boolean = false,
  val denyOk: Boolean = false,
) {
  val ntfsHrOk: Boolean get() = hrAclGroup == "HR_Users"
  val ntfsItOk: Boolean get() = itAclGroup == "IT_Users"
  val leastPrivilege: Boolean
    get() = ntfsHrOk && ntfsItOk &&
      hrAclGroup != "IT_Users" && itAclGroup != "HR_Users"

  val readyForClient: Boolean
    get() = fileServerRole && folderHr && folderIt && groupHr && groupIt &&
      leastPrivilege && shareOn && shareChangeBoth

  fun missingHint(): String = when {
    !fileServerRole -> "On the server, open Start, then Server Manager. Install only File Server."
    !folderHr || !folderIt -> "Open File Explorer. New folder, type HR, then type IT."
    !groupHr || !groupIt -> "Open Computer Management. New group, type HR_Users and IT_Users."
    !leastPrivilege -> "Open Folder Security. Give HR_Users the HR folder, and IT_Users the IT folder."
    !shareOn || !shareChangeBoth -> "Open Advanced Sharing. Share DeptShares and allow Change for both groups."
    !allowOk -> "Switch to the PC. In File Explorer type \\\\FileServer\\DeptShares\\HR and press Go."
    !denyOk -> "On the PC, open \\\\FileServer\\DeptShares\\IT. Windows should deny access."
    else -> "HR can open HR and is blocked from IT."
  }

  val checklistDone: Int
    get() = listOf(
      fileServerRole, folderHr && folderIt, groupHr && groupIt,
      leastPrivilege, shareOn && shareChangeBoth, allowOk, denyOk,
    ).count { it }
}

@Composable
fun ServerLabScreen(
  navController: NavHostController,
  hints: Boolean = true,
  embedded: Boolean = false,
  startFaulted: Boolean = false,
  onStationComplete: (() -> Unit)? = null,
  onLeave: (() -> Unit)? = null,
) {
  var cfg by remember {
    mutableStateOf(
      if (startFaulted) {
        ServerConfig(
          fileServerRole = true,
          folderHr = true,
          folderIt = true,
          groupHr = true,
          groupIt = true,
          hrAclGroup = "HR_Users",
          itAclGroup = "HR_Users",
          shareOn = true,
          shareChangeBoth = true,
        )
      } else {
        ServerConfig()
      },
    )
  }
  var onServer by remember { mutableStateOf(true) }
  var window by remember { mutableStateOf(SrvWindow.NONE) }
  var startOpen by remember { mutableStateOf(false) }
  var passed by remember { mutableStateOf(false) }
  var status by remember {
    mutableStateOf(
      if (startFaulted) "Fault: IT folder is open to HR. Fix it in Folder Security, then retest on the PC."
      else "Server desktop. Open Start and follow the goal.",
    )
  }
  var dialog by remember { mutableStateOf<Pair<Boolean, Pair<String, String>>?>(null) }
  var guideVisible by remember { mutableStateOf(true) }

  fun reset() {
    dialog = null
    passed = false
    onServer = true
    window = SrvWindow.NONE
    startOpen = false
    cfg = ServerConfig()
    status = "Desktop reset. Open Start on the server."
  }

  fun leave() {
    when {
      window != SrvWindow.NONE -> window = SrvWindow.NONE
      startOpen -> startOpen = false
      onLeave != null -> onLeave()
      !embedded && !navController.popBackStack() -> navController.navigateUp()
    }
  }

  BackHandler { leave() }

  if (dialog != null) {
    val (ok, msg) = dialog!!
    RuleFeedbackDialog(
      correct = ok,
      title = msg.first,
      body = msg.second,
      onDismiss = { dialog = null },
    )
  }

  fun finishModule() {
    if (!passed && cfg.checklistDone < 7) {
      dialog = false to ("Not finished" to "Still needed: ${cfg.missingHint()}")
      status = cfg.missingHint()
      return
    }
    passed = true
    if (onStationComplete != null) onStationComplete() else onLeave?.invoke()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    ServerDesktop(
      cfg = cfg,
      hints = hints,
      onServer = onServer,
      window = window,
      startOpen = startOpen,
      onStartOpen = { startOpen = it },
      onWindow = { window = it },
      onToggleMachine = {
        onServer = !onServer
        window = SrvWindow.NONE
        startOpen = false
        status = if (onServer) "Server desktop." else "PC desktop. Signed in as a member of HR_Users."
      },
      onInstallRole = { file, dns, dhcp ->
        when {
          !file -> {
            dialog = false to ("Role missing" to "Check File Server, then Install.")
            status = "File Server is not selected"
          }
          dns || dhcp -> {
            dialog = false to ("Extra role" to "This server only needs File Server. Clear DNS and DHCP.")
            status = "Remove the extra roles"
          }
          else -> {
            cfg = cfg.copy(fileServerRole = true)
            window = SrvWindow.NONE
            status = "File Server role installed"
            dialog = true to ("Role installed" to "File Server is on. Next, create the HR and IT folders in File Explorer.")
          }
        }
      },
      onCreateFolder = { raw ->
        when {
          raw.equals("HR", true) -> {
            if (cfg.folderHr) {
              dialog = false to ("Already exists" to "HR is already in C:\\DeptShares.")
            } else {
              cfg = cfg.copy(folderHr = true)
              status = "Created C:\\DeptShares\\HR"
              dialog = true to ("Folder created" to "C:\\DeptShares\\HR is ready.")
            }
          }
          raw.equals("IT", true) -> {
            if (cfg.folderIt) {
              dialog = false to ("Already exists" to "IT is already in C:\\DeptShares.")
            } else {
              cfg = cfg.copy(folderIt = true)
              status = "Created C:\\DeptShares\\IT"
              dialog = true to ("Folder created" to "C:\\DeptShares\\IT is ready.")
            }
          }
          else -> {
            dialog = false to ("Wrong folder" to "Type HR or IT. Those are the two department folders.")
            status = "Folder name was not HR or IT"
          }
        }
      },
      onCreateGroup = { raw ->
        when {
          raw.equals("HR_Users", true) -> {
            if (cfg.groupHr) {
              dialog = false to ("Already exists" to "HR_Users is already a group.")
            } else {
              cfg = cfg.copy(groupHr = true)
              status = "Group HR_Users created"
              dialog = true to ("Group created" to "HR_Users can be used on the HR folder.")
            }
          }
          raw.equals("IT_Users", true) -> {
            if (cfg.groupIt) {
              dialog = false to ("Already exists" to "IT_Users is already a group.")
            } else {
              cfg = cfg.copy(groupIt = true)
              status = "Group IT_Users created"
              dialog = true to ("Group created" to "IT_Users can be used on the IT folder.")
            }
          }
          else -> {
            dialog = false to ("Wrong group" to "Type HR_Users or IT_Users.")
            status = "Group name was not recognized"
          }
        }
      },
      onGrant = { folder, group ->
        if (!cfg.folderHr || !cfg.folderIt || !cfg.groupHr || !cfg.groupIt) {
          dialog = false to ("Not ready" to "Create both folders and both groups before you set security.")
          return@ServerDesktop
        }
        if (folder == "HR") {
          cfg = cfg.copy(hrAclGroup = group)
          if (group == "HR_Users") {
            status = "HR folder allows HR_Users"
            dialog = true to ("Security updated" to "HR_Users can modify the HR folder.")
          } else {
            status = "HR folder has the wrong group"
            dialog = false to ("Wrong group" to "The HR folder should allow HR_Users, not $group.")
          }
        } else {
          cfg = cfg.copy(itAclGroup = group)
          if (group == "IT_Users") {
            status = "IT folder allows IT_Users"
            dialog = true to ("Security updated" to "IT_Users can modify the IT folder. HR should not be on this list.")
          } else {
            status = "IT folder has the wrong group"
            dialog = false to ("Wrong group" to "The IT folder should allow IT_Users, not $group.")
          }
        }
      },
      onApplyShare = { share, hr, it ->
        if (!share) {
          cfg = cfg.copy(shareOn = false, shareChangeBoth = false)
          dialog = false to ("Not shared" to "Turn on Share this folder, then allow Change for both groups.")
          return@ServerDesktop
        }
        if (!hr || !it) {
          cfg = cfg.copy(shareOn = true, shareChangeBoth = false)
          dialog = false to ("Permissions incomplete" to "Allow Change for both HR_Users and IT_Users, then Apply.")
          return@ServerDesktop
        }
        cfg = cfg.copy(shareOn = true, shareChangeBoth = true)
        window = SrvWindow.NONE
        status = "Share \\\\FileServer\\DeptShares is published"
        dialog = true to ("Share published" to "Switch to the PC and open \\\\FileServer\\DeptShares\\HR.")
      },
      onOpenPath = { raw ->
        val collapsed = raw.trim().replace('/', '\\').lowercase().replace(Regex("\\\\+"), Regex.escapeReplacement("\\"))
        if (!cfg.readyForClient) {
          dialog = false to (
            "Server not ready" to
              "Finish the server first: File Server role, HR and IT folders, both groups, security, and the share."
            )
          return@ServerDesktop
        }
        when (collapsed) {
          "\\fileserver\\deptshares\\hr" -> {
            cfg = cfg.copy(allowOk = true)
            status = "Access granted. HR folder opened."
            dialog = true to ("Access granted" to "HR_Users can open the HR folder. Now try the IT folder.")
          }
          "\\fileserver\\deptshares\\it" -> {
            if (!cfg.leastPrivilege) {
              status = "HR opened IT. That should have been denied."
              dialog = false to (
                "Should be denied" to
                  "HR opened the IT folder. On the server, Folder Security must give HR_Users only the HR folder, and IT_Users only the IT folder."
                )
            } else {
              val next = cfg.copy(denyOk = true)
              cfg = next
              val done = next.checklistDone >= 7
              if (done) {
                passed = true
                status = "PASS. HR is allowed on HR and denied on IT."
              } else {
                status = "Access denied on IT, as required."
              }
              dialog = true to (
                "Access denied" to
                  if (done) {
                    "Windows blocked HR from the IT folder. Least privilege is correct. Tap Save and return."
                  } else {
                    "Windows blocked HR from the IT folder. That is the result we want."
                  }
                )
            }
          }
          else -> {
            dialog = false to (
              "Path not found" to
                "Type \\\\FileServer\\DeptShares\\HR or \\\\FileServer\\DeptShares\\IT, then press Go."
              )
            status = "That path was not found"
          }
        }
      },
    )

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
            "Goal: HR can open HR, and must be denied IT.",
            color = White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (hints) {
            Text(
              "DO THIS: ${cfg.missingHint()}",
              color = Color(0xFFE8B84A),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Text(
            "Tap to hide    ${cfg.checklistDone}/7",
            color = White.copy(alpha = 0.75f),
            fontSize = 10.sp,
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        GuideChip("Back") { leave() }
      }
    } else {
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        GuideChip("Guide") { guideVisible = true }
        GuideChip("Reset") { reset() }
        GuideChip("${cfg.checklistDone}/7") {}
        GuideChip("Back") { leave() }
      }
    }

    if (passed) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 52.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF1E8449))
          .clickable { finishModule() }
          .padding(horizontal = 20.dp, vertical = 12.dp),
      ) {
        Text("Finish module", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
  }
}

@Composable
private fun GuideChip(label: String, onClick: () -> Unit) {
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
