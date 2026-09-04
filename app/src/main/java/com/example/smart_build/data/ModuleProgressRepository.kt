package com.example.smart_build.data

import android.util.Log
import com.example.smart_build.data.client.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Syncs per-module student progress with Supabase `module_progress`.
 * Local SharedPreferences remains a cache for offline / fast UI.
 */
object ModuleProgressRepository {
  private const val TABLE = "module_progress"
  private const val TAG = "ModuleProgress"

  suspend fun fetchAll(): List<ModuleProgressRow> = withContext(Dispatchers.IO) {
    val userId = currentUserId() ?: return@withContext emptyList()
    try {
      SupabaseClient.client.from(TABLE)
        .select {
          filter {
            eq("user_id", userId)
          }
        }
        .decodeList<ModuleProgressRow>()
    } catch (e: Exception) {
      Log.e(TAG, "fetchAll failed: ${e.message}", e)
      emptyList()
    }
  }

  suspend fun upsert(
    moduleId: Int,
    percent: Float,
    guidedDone: Boolean,
    assessmentDone: Boolean,
  ): Boolean = withContext(Dispatchers.IO) {
    val userId = currentUserId() ?: return@withContext false
    val row = ModuleProgressRow(
      userId = userId,
      moduleId = moduleId,
      percent = percent.coerceIn(0f, 100f),
      guidedDone = guidedDone,
      assessmentDone = assessmentDone,
      updatedAt = utcNowIso(),
    )
    try {
      // Explicitly list every column so supabase-kt never omits a boolean
      // default from the auto-generated `columns=` query param.  If the table
      // is missing from PostgREST's schema cache (PGRST204) you must reload
      // the cache: Supabase Dashboard → Settings → API → "Reload schema".
      SupabaseClient.client.from(TABLE).upsert(row) {
        onConflict = "user_id,module_id"
      }
      true
    } catch (e: Exception) {
      val msg = e.message ?: ""
      if (msg.contains("PGRST204") || msg.contains("schema cache")) {
        Log.e(TAG, "upsert failed module=$moduleId: SCHEMA CACHE STALE — " +
          "go to Supabase Dashboard → Settings → API → Reload schema. $msg")
      } else {
        Log.e(TAG, "upsert failed module=$moduleId: $msg", e)
      }
      false
    }
  }

  private fun currentUserId(): String? =
    SupabaseClient.client.auth.currentUserOrNull()?.id

  private fun utcNowIso(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(Date())
  }
}
