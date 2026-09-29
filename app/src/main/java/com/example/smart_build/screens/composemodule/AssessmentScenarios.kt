package com.example.smart_build.screens.composemodule

/**
 * Panelist / manuscript: Scenario-Based Assessments — place the student in a
 * situation where they must identify and correct errors (not only “same lab, no hints”).
 */
data class AssessmentScenario(
  val moduleId: Int,
  val title: String,
  val situation: String,
  val faults: List<String>,
  val goal: String,
)

object AssessmentScenarios {
  fun forModule(moduleId: Int): AssessmentScenario = when (moduleId) {
    2 -> AssessmentScenario(
      moduleId = 2,
      title = "Broken small-office link",
      situation = "A trainee finished a ‘straight-through’ run, but the switch–PC link has no light. " +
        "The cable on the bench is actually a crossover, and the topology sketch is incomplete.",
      faults = listOf(
        "Wrong cable standard used for PC-to-switch (should be straight-through T568B both ends — you will build the correct cable first).",
        "Missing or wrong device order / cabling on the bench sketch.",
      ),
      goal = "Build the correct cable, then restore modem→router→switch→AP/PC so link lights pass.",
    )
    3 -> AssessmentScenario(
      moduleId = 3,
      title = "Broken client–server + over-permission",
      situation = "The trainee used a crossover between the switch and file server (link down), " +
        "and HR staff can open IT documents. Fix cabling first (straight-through), then rebuild least privilege.",
      faults = listOf(
        "The switch-to-server cable must be straight-through, not crossover.",
        "HR can open IT documents. That access must be denied.",
        "Groups, NTFS, or the share may be incomplete or wrong.",
      ),
      goal = "Connect the switch, server, and PC with straight-through cables. Then set roles, folders, NTFS, and the share so HR is allowed on HR and denied on IT.",
    )
    4 -> AssessmentScenario(
      moduleId = 4,
      title = "Ticket #SB-4401 — slow PC, no internet",
      situation = "A client reports a cluttered desktop, full disk, and dead web access. Action Center shows multiple warnings. " +
        "A previous tech only ran one cleanup step and left the ticket open.",
      faults = listOf(
        "Desktop not personalized / wallpaper still default.",
        "Junk files and Disk Cleanup not finished.",
        "Gateway / external ping path not verified in CMD.",
        "Ticket not closed with documentation.",
      ),
      goal = "Clear every Action Center warning in a proper service order and close the visit.",
    )
    else -> AssessmentScenario(
      moduleId = moduleId,
      title = "Assessment scenario",
      situation = "Apply TESDA-aligned procedures without guided coaches.",
      faults = emptyList(),
      goal = "Complete the station correctly.",
    )
  }
}
