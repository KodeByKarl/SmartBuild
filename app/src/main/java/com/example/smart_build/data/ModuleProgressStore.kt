package com.example.smart_build.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.jan.supabase.auth.auth
import com.example.smart_build.data.client.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Per-account local cache + Supabase sync for flowchart progress.
 * Prefs are scoped by auth user id so signing in as another student never
 * inherits (or overwrites) the previous account's "Your Progress".
 */
object ModuleProgressStore {
  private const val LEGACY_PREFS = "smartbuild_module_progress"
  private const val VERSION_KEY = "meta_version"
  /** v7: per-user prefs; stop wiping finished M1–4 rows back to 0%. */
  private const val VERSION = 7
  private const val TAG = "ModuleProgressStore"
  private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  @Volatile
  private var boundUserId: String? = null

  /**
   * Point the cache at the signed-in student. Call on Home / login / sign-out.
   * Switching accounts never copies another user's local numbers.
   */
  fun bindAccount(context: Context?, userId: String?) {
    val normalized = userId?.takeIf { it.isNotBlank() }
    if (normalized == boundUserId) return
    Log.d(TAG, "bindAccount ${boundUserId ?: "none"} → ${normalized ?: "guest"}")
    boundUserId = normalized
    if (context != null) {
      migrateIfNeeded(context)
    }
  }

  /** Drop the active account pointer (sign-out). Prefs files stay on disk per user. */
  fun unbindAccount() {
    boundUserId = null
  }

  /** Resolve the current Supabase user and bind (no-op if unchanged). */
  fun bindCurrentUser(context: Context) {
    val id = SupabaseClient.client.auth.currentUserOrNull()?.id
    bindAccount(context, id)
  }

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

  /**
   * Pull this account's remote rows into its local cache.
   * Never max-merges another user's leftovers — prefs are already scoped.
   */
  suspend fun pullFromRemote(context: Context) {
    bindCurrentUser(context)
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
    val version = p.getInt(VERSION_KEY, 0)
    if (version >= VERSION) return

    if (version in 1..2) {
      // Ancient schema — wipe this prefs file only (never cross-user).
      p.edit().clear().putInt(VERSION_KEY, VERSION).apply()
      claimOrDropLegacy(context)
      return
    }
    // v6 wiped finished M1–4 rows back to 0%. v7 keeps finished work and
    // moves the old device-global file into the signed-in account once.
    claimOrDropLegacy(context)
    p.edit().putInt(VERSION_KEY, VERSION).apply()
    repairFalseIntroComplete(context)
  }

  /**
   * One-shot: the first signed-in account after upgrade inherits the old
   * device-global cache (so progress is not lost). Later accounts get a
   * clean scoped file + remote pull — never the previous student's numbers.
   */
  private fun claimOrDropLegacy(context: Context) {
    val app = context.applicationContext
    val legacy = app.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
    if (legacy.all.isEmpty()) return
    val meta = app.getSharedPreferences("smartbuild_progress_meta", Context.MODE_PRIVATE)
    val claimedBy = meta.getString("legacy_claimed_by", null)
    val user = boundUserId
    if (user != null && (claimedBy == null || claimedBy == user)) {
      val scopedEmpty = (0..4).none {
        prefs(context).getFloat(progressKey(it), 0f) > 0.5f ||
          prefs(context).getBoolean(guidedKey(it), false) ||
          prefs(context).getBoolean(assessmentKey(it), false)
      }
      if (scopedEmpty) {
        val ed = prefs(context).edit()
        for (id in 0..4) {
          ed.putFloat(progressKey(id), legacy.getFloat(progressKey(id), 0f))
          ed.putBoolean(guidedKey(id), legacy.getBoolean(guidedKey(id), false))
          ed.putBoolean(assessmentKey(id), legacy.getBoolean(assessmentKey(id), false))
        }
        ed.putInt(VERSION_KEY, VERSION)
        ed.apply()
        Log.d(TAG, "Imported legacy progress into account $user")
      }
      meta.edit().putString("legacy_claimed_by", user).apply()
    }
    legacy.edit().clear().apply()
    Log.d(TAG, "Cleared legacy global progress prefs")
  }

  private fun prefs(context: Context): SharedPreferences {
    val app = context.applicationContext
    val user = boundUserId
      ?: SupabaseClient.client.auth.currentUserOrNull()?.id?.also { boundUserId = it }
    val name = if (user.isNullOrBlank()) {
      "smartbuild_module_progress_guest"
    } else {
      "smartbuild_module_progress_$user"
    }
    return app.getSharedPreferences(name, Context.MODE_PRIVATE)
  }

  private fun progressKey(id: Int) = "progress_$id"
  private fun guidedKey(id: Int) = "guided_$id"
  private fun assessmentKey(id: Int) = "assessment_$id"
}
