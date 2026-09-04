package com.example.smart_build.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Local cache + Supabase sync for flowchart progress.
 * Never hardcode 0% / 100% in the UI — always read from here after pull.
 */
object ModuleProgressStore {
  private const val PREFS = "smartbuild_module_progress"
  private const val VERSION_KEY = "meta_version"
  private const val VERSION = 6
  private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  fun guidedDone(context: Context, moduleId: Int): Boolean {
    migrateIfNeeded(context)
    return prefs(context).getBoolean(guidedKey(moduleId), false) ||
      assessmentDone(context, moduleId)
  }

  fun assessmentDone(context: Context, moduleId: Int): Boolean {
    migrateIfNeeded(context)
    return prefs(context).getBoolean(assessmentKey(moduleId), false)
  }

  /** 0..100 */
  fun progress(context: Context, moduleId: Int): Float {
    migrateIfNeeded(context)
    return prefs(context).getFloat(progressKey(moduleId), 0f).coerceIn(0f, 100f)
  }

  fun allProgress(context: Context): Map<Int, Float> {
    migrateIfNeeded(context)
    return (0..4).associateWith { progress(context, it) }
  }

  /**
   * Update percent from in-module page progress (monotonic — never decreases).
   * Caps at 99% — only markAssessmentCompleted may set 100%.
   */
  fun setProgressPercent(context: Context, moduleId: Int, percent: Float) {
    migrateIfNeeded(context)
    if (assessmentDone(context, moduleId)) return
    val p = prefs(context)
    val current = p.getFloat(progressKey(moduleId), 0f)
    val next = maxOf(current, percent.coerceIn(0f, 99f))
    p.edit().putFloat(progressKey(moduleId), next).apply()
    pushAsync(context, moduleId)
  }

  fun markGuidedCompleted(context: Context, moduleId: Int) {
    migrateIfNeeded(context)
    val p = prefs(context)
    val current = p.getFloat(progressKey(moduleId), 0f)
    p.edit()
      .putBoolean(guidedKey(moduleId), true)
      .putFloat(progressKey(moduleId), maxOf(current, 50f).coerceAtMost(99f))
      .apply()
    pushAsync(context, moduleId)
  }

  fun markAssessmentCompleted(context: Context, moduleId: Int) {
    migrateIfNeeded(context)
    prefs(context).edit()
      .putBoolean(guidedKey(moduleId), true)
      .putBoolean(assessmentKey(moduleId), true)
      .putFloat(progressKey(moduleId), 100f)
      .apply()
    pushAsync(context, moduleId)
  }

  fun markIntroCompleted(context: Context) {
    markAssessmentCompleted(context, 0)
  }

  fun isModuleUnlocked(_context: Context, _moduleId: Int): Boolean = true

  /** Pull remote rows into local cache (call on Home resume / login). */
  suspend fun pullFromRemote(context: Context) {
    migrateIfNeeded(context)
    val rows = ModuleProgressRepository.fetchAll()
    if (rows.isNotEmpty()) {
      val editor = prefs(context).edit()
      for (row in rows) {
        val id = row.moduleId
        val localPct = prefs(context).getFloat(progressKey(id), 0f)
        val remotePct = row.percent.coerceIn(0f, 100f)
        val assessed = row.assessmentDone || prefs(context).getBoolean(assessmentKey(id), false)
        val guided = row.guidedDone || assessed || prefs(context).getBoolean(guidedKey(id), false)
        val merged = maxOf(localPct, remotePct)
        editor.putFloat(progressKey(id), if (assessed) 100f else merged.coerceAtMost(99f))
        editor.putBoolean(guidedKey(id), guided)
        editor.putBoolean(assessmentKey(id), assessed)
      }
      editor.apply()
    }
    repairFalseIntroComplete(context)
    for (id in 0..4) {
      val local = progress(context, id)
      val localGuided = guidedDone(context, id)
      val localAssessed = assessmentDone(context, id)
      if (local <= 0f && !localGuided && !localAssessed) continue
      val remote = rows.firstOrNull { it.moduleId == id }
      val needsPush = remote == null ||
        local > (remote.percent + 0.5f) ||
        localGuided != remote.guidedDone ||
        localAssessed != remote.assessmentDone
      if (needsPush) {
        ModuleProgressRepository.upsert(
          moduleId = id,
          percent = local,
          guidedDone = localGuided,
          assessmentDone = localAssessed,
        )
      }
    }
  }

  /**
   * Clears Intro marked 100% while Modules 1–4 are untouched — usually from the
   * last-slide progress_update bug, not a real completion.
   */
  private fun repairFalseIntroComplete(context: Context) {
    val introDone = assessmentDone(context, 0) || progress(context, 0) >= 99.5f
    if (!introDone) return
    for (id in 1..4) {
      if (progress(context, id) > 0.5f || guidedDone(context, id) || assessmentDone(context, id)) {
        return
      }
    }
    prefs(context).edit()
      .putFloat(progressKey(0), 0f)
      .putBoolean(guidedKey(0), false)
      .putBoolean(assessmentKey(0), false)
      .apply()
    pushAsync(context, 0)
  }

  private fun pushAsync(context: Context, moduleId: Int) {
    val appCtx = context.applicationContext
    ioScope.launch {
      ModuleProgressRepository.upsert(
        moduleId = moduleId,
        percent = progress(appCtx, moduleId),
        guidedDone = guidedDone(appCtx, moduleId),
        assessmentDone = assessmentDone(appCtx, moduleId),
      )
    }
  }

  private fun migrateIfNeeded(context: Context) {
    val p = prefs(context)
    val version = p.getInt(VERSION_KEY, 1)
    if (version >= VERSION) return
    if (version < 3) {
      p.edit().clear().putInt(VERSION_KEY, VERSION).apply()
      return
    }
    val editor = p.edit().putInt(VERSION_KEY, VERSION)
    if (version < 6) {
      // Persistent Godot replayed assessment_completed onto Guided opens.
      for (id in 1..4) {
        val done = p.getBoolean(assessmentKey(id), false)
        val pct = p.getFloat(progressKey(id), 0f)
        if (done || pct >= 99.5f) {
          editor.putBoolean(assessmentKey(id), false)
          editor.putBoolean(guidedKey(id), false)
          editor.putFloat(progressKey(id), 0f)
        }
      }
    }
    editor.apply()
    repairFalseIntroComplete(context)
  }

  private fun prefs(context: Context) =
    context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

  private fun progressKey(id: Int) = "progress_$id"
  private fun guidedKey(id: Int) = "guided_$id"
  private fun assessmentKey(id: Int) = "assessment_$id"
}
