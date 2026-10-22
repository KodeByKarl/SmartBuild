package com.example.smart_build.data

/** Module names shown on the Godot loading screen when a module is reopened mid-flow. */
object ModuleTitles {
  fun forId(moduleId: Int): String = when (moduleId) {
    0 -> "Preparatory Module | Introduction To Computer Systems Servicing"
    1 -> "Installing and Configuring Computer Systems"
    2 -> "Setting Up Computer Networks"
    3 -> "Setting Up Computer Servers"
    4 -> "Maintaining Computer Systems and Networks"
    else -> "Module $moduleId"
  }
}

/**
 * Where the shared Godot ModuleFlow opens (sent as `simulationType` in prepare).
 * Flow: Lessons → Guided Simulation → Be Ready → Assessment Simulation → Completion.
 */
object ModuleFlowEntry {
  /** Lesson pages (resumes from saved progress). */
  const val LESSONS = 0

  /** "Be Ready for Assessment!" — the Assessment button on the module card. */
  const val BE_READY = 1

  /** Congratulations page — Modules 2–4 after the Android assessment lab. */
  const val COMPLETION = 3
}
