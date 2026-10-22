package com.example.smart_build.tutorial

/**
 * Every tutorial in the app. Ids end in a version: bump it to show a tour again
 * to students who already saw the old one.
 */
object TutorialContent {

  // —— Home ——————————————————————————————————————————————————————————————
  const val HOME_ID = "home_tour_v1"
  const val HOME_CARD_ID = "home_card_v1"
  const val SEARCH_ID = "search_tour_v1"
  const val LAB_CHROME_ID = "lab_chrome_v1"

  val home = listOf(
    CoachStep(
      emoji = "👋",
      title = "Welcome to SmartBuild!",
      body = "Learn Computer Systems Servicing hands-on. Let's take a quick tour of the app — it only takes a minute.",
    ),
    CoachStep(
      targetKey = "home_search",
      emoji = "🔎",
      title = "Component Search",
      body = "Look up any computer part — CPU, RAM, motherboard, cables, servers and more — with pictures and explanations.",
    ),
    CoachStep(
      targetKey = "home_help",
      emoji = "❓",
      title = "Help",
      body = "Tap here anytime to read how SmartBuild works, or to replay this tutorial.",
    ),
    CoachStep(
      targetKey = "home_profile",
      emoji = "👤",
      title = "Your profile",
      body = "Change your password, see the credits, or sign out. Your progress is saved to your account.",
    ),
    CoachStep(
      targetKey = "home_modules",
      emoji = "👆",
      title = "Your modules",
      body = "Swipe left and right to browse the Preparatory Module and Modules 1 to 4. Tap a card to open it. Start with the Preparatory Module!",
    ),
  )

  /** Shown the first time a module card is opened (it lives in its own window). */
  fun homeCard(moduleId: Int): List<CoachStep> = buildList {
    add(
      CoachStep(
        targetKey = "card_back",
        emoji = "⬅️",
        title = "Back to the modules",
        body = "Tap Back to close this card and return to the module list.",
      ),
    )
    add(
      CoachStep(
        targetKey = "card_progress",
        emoji = "📈",
        title = "Your progress",
        body = "The circle fills up as you finish this module. It is saved to your account.",
      ),
    )
    if (moduleId == 0) {
      add(
        CoachStep(
          targetKey = "card_gs",
          emoji = "▶️",
          title = "Start the lesson",
          body = "The Preparatory Module is lesson-only. Tap here to start or continue — you pick up where you left off.",
        ),
      )
    } else {
      add(
        CoachStep(
          targetKey = "card_gs",
          emoji = "🧭",
          title = "Guided Simulation",
          body = "Start here. Read the lessons, then practice step by step with hints that tell you exactly what to do.",
        ),
      )
      add(
        CoachStep(
          targetKey = "card_as",
          emoji = "🏁",
          title = "Assessment",
          body = "Locked until you finish the Guided Simulation. Then you get a real scenario with faults to fix — no hints this time!",
        ),
      )
    }
  }

  val search = listOf(
    CoachStep(
      targetKey = "search_back",
      emoji = "⬅️",
      title = "Go back",
      body = "Return to the Home screen.",
    ),
    CoachStep(
      targetKey = "search_field",
      emoji = "⌨️",
      title = "Search a part",
      body = "Type a part name, like \"RAM\" or \"router\", and the list filters instantly.",
    ),
    CoachStep(
      targetKey = "search_chips",
      emoji = "🗂️",
      title = "Browse by category",
      body = "Swipe through the chips to see only hardware, networks, cabling, servers or internet parts.",
    ),
    CoachStep(
      targetKey = "search_grid",
      emoji = "🖼️",
      title = "Tap a part",
      body = "Tap a card to read about it. Tap the picture to zoom in.",
    ),
  )

  // —— Compose labs (Modules 2–4) —————————————————————————————————————————
  /** Shared by every lab that uses the lab scaffold; shown once. */
  fun labChrome(hasReset: Boolean): List<CoachStep> = buildList {
    add(
      CoachStep(
        targetKey = "lab_header",
        emoji = "📍",
        title = "Where you are",
        body = "The top bar shows the lab name and your progress. Use the back arrow to leave the lab.",
      ),
    )
    add(
      CoachStep(
        targetKey = "lab_guide",
        emoji = "💡",
        title = "Your goal and hints",
        body = "Read the goal first. In Guided Simulation the yellow \"DO THIS\" line tells you the next move. In the Assessment it is gone — you're on your own!",
      ),
    )
    add(
      CoachStep(
        targetKey = "lab_playground",
        emoji = "🛠️",
        title = "The workbench",
        body = "This is where you do the work — drag, tap and connect things here.",
      ),
    )
    add(
      CoachStep(
        targetKey = "lab_status",
        emoji = "✅",
        title = "Status",
        body = "Tells you what just happened and whether you did it right.",
      ),
    )
    if (hasReset) {
      add(
        CoachStep(
          targetKey = "lab_reset",
          emoji = "🔄",
          title = "Reset",
          body = "Made a mess? Reset starts this step again. It's safe to try.",
        ),
      )
    }
    add(
      CoachStep(
        targetKey = "lab_primary",
        emoji = "▶️",
        title = "Main button",
        body = "Tap it to check your work and move on. It turns green when the step is complete.",
      ),
    )
  }

  const val CABLE_INTRO_ID = "lab_cable_intro_v1"
  val cableIntro = listOf(
    CoachStep(
      emoji = "🔌",
      title = "Cable basics",
      body = "Before you build a cable, learn which one to use. Straight-through joins different devices. Crossover joins like devices. Read the cards, then tap Continue.",
    ),
  )

  const val CRIMP_ID = "lab_crimp_v1"
  val crimp = listOf(
    CoachStep(
      emoji = "🧰",
      title = "Make a network cable",
      body = "You'll build a real Ethernet cable on a virtual bench, the way a technician does.",
    ),
    CoachStep(
      emoji = "1️⃣",
      title = "Follow the order",
      body = "Drag the coil to the mat, strip the jacket and untwist the pairs. Then, for each end: arrange the colors, insert them into the plug and crimp. Finish with the cable tester.",
    ),
    CoachStep(
      emoji = "🎨",
      title = "Colors matter",
      body = "Straight-through uses T568B on both ends. Crossover uses T568A on one end. Wrong colors fail the test!",
    ),
  )

  const val TOPOLOGY_ID = "lab_topology_v1"
  val topology = listOf(
    CoachStep(
      emoji = "🌐",
      title = "Build the office network",
      body = "Connect the devices from the internet to the PC. This lab has three short phases.",
    ),
    CoachStep(
      emoji = "🧱",
      title = "Phase 1 · Place",
      body = "Add devices from the palette in the right order: Modem, Router, Switch, Access Point, then PC.",
    ),
    CoachStep(
      emoji = "🔗",
      title = "Phase 2 · Cable",
      body = "Create each link with a cable. Unlike devices use straight-through. Tap a link to remove a wrong one.",
    ),
    CoachStep(
      emoji = "💚",
      title = "Phase 3 · Verify",
      body = "Check that the path from the modem to the PC lights up. Green link lights mean success!",
    ),
  )

  const val SERVER_TOPOLOGY_ID = "lab_server_topology_v1"
  val serverTopology = listOf(
    CoachStep(
      emoji = "🖥️",
      title = "Build the server network",
      body = "Connect a Switch, a Server and a PC, give them addresses, and prove they can talk to each other.",
    ),
    CoachStep(
      emoji = "🧱",
      title = "Place the devices",
      body = "Add the Switch, Server and PC from the device palette. Drag them to move them around.",
    ),
    CoachStep(
      emoji = "🔗",
      title = "Cable them",
      body = "Pick the Straight-Through cable tool, tap one device, then the other. Use Delete to remove a wrong cable.",
    ),
    CoachStep(
      emoji = "🌐",
      title = "Set IPs and ping",
      body = "Tap a device to open its desktop. Enter its IP address and mask, then on the PC open Command Prompt and ping the server.",
    ),
    CoachStep(
      emoji = "✅",
      title = "Lock each phase",
      body = "When a phase looks right, tap the action button to lock it and move to the next one.",
    ),
  )

  const val SERVER_LAB_ID = "lab_server_desk_v1"
  val serverLab = listOf(
    CoachStep(
      emoji = "🗄️",
      title = "Set up a file server",
      body = "You're on the server's desktop. Open the Start menu and work through the setup: role, folders, users, groups and permissions.",
    ),
    CoachStep(
      targetKey = "srv_guide",
      emoji = "💡",
      title = "Goal and hints",
      body = "This card shows the goal, the next DO THIS step (Guided mode only) and how many of the 7 checklist items are done. Tap it to hide it.",
    ),
    CoachStep(
      emoji = "🔄",
      title = "Guide and Reset",
      body = "With the card hidden, the top-right chips appear: Guide brings the card back, Reset restarts the desktop.",
    ),
    CoachStep(
      emoji = "🔐",
      title = "Least privilege",
      body = "Give each group only the access it needs. At the end, test that HR can open the HR folder and is blocked from IT.",
    ),
    CoachStep(
      targetKey = "srv_back",
      emoji = "⬅️",
      title = "Back",
      body = "Leave the lab. When every item is checked, a green \"Finish module\" button appears at the bottom.",
    ),
  )

  const val MAINTENANCE_ID = "lab_maintenance_v1"
  val maintenance = listOf(
    CoachStep(
      emoji = "🧑‍🔧",
      title = "A day at the service desk",
      body = "A customer's PC has problems. You'll handle the whole service visit on a Windows desktop.",
    ),
    CoachStep(
      emoji = "🎫",
      title = "Start with the ticket",
      body = "Open the Ticket icon and accept it. Then work through the jobs: wallpaper, Recycle Bin, Disk Cleanup and a network ping.",
    ),
    CoachStep(
      emoji = "🪟",
      title = "Use the desktop",
      body = "The Start menu, taskbar and desktop icons work like real Windows. In Guided mode, highlighted icons show where to go next.",
    ),
    CoachStep(
      targetKey = "mt_guide",
      emoji = "💡",
      title = "DO THIS",
      body = "This card (Guided mode) tells you the next job and counts your progress out of 6. Tap it to hide it; tap Guide to bring it back.",
    ),
    CoachStep(
      targetKey = "mt_exit",
      emoji = "🚪",
      title = "Exit",
      body = "Leave the lab. Close the service ticket when everything is done, then tap Finish module.",
    ),
  )

  // —— Godot lesson pages (Modules 0–4) ——————————————————————————————————————
  const val LESSON_ID = "lesson_flow_v1"
  val lesson = listOf(
    CoachStep(
      emoji = "📖",
      title = "How lessons work",
      body = "Each module is a set of lesson pages. Read at your own pace — your place is saved automatically.",
    ),
    CoachStep(
      emoji = "👈",
      title = "Swipe to move",
      body = "Swipe left for the next page and right to go back. The dots show which page you're on.",
    ),
    CoachStep(
      emoji = "☑️",
      title = "Do the activities",
      body = "Some pages have checklists or buttons. Finish them to unlock the next page.",
    ),
    CoachStep(
      emoji = "❓",
      title = "Back and Help",
      body = "The Back button at the top-left leaves the module. The Help button at the top-right explains the page again.",
    ),
  )
}
