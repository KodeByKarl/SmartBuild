package com.example.smart_build.data

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// @EncodeDefault forces kotlinx.serialization to include every field in the
// JSON body even when the value matches its default (false, 0f, null).
// Without it, supabase-kt omits those fields from `columns=` in the upsert
// URL, so PostgREST never writes guided_done / assessment_done to the DB.
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ModuleProgressRow(
  @SerialName("user_id") val userId: String,
  @SerialName("module_id") val moduleId: Int,
  @EncodeDefault val percent: Float = 0f,
  @EncodeDefault @SerialName("guided_done") val guidedDone: Boolean = false,
  @EncodeDefault @SerialName("assessment_done") val assessmentDone: Boolean = false,
  @EncodeDefault @SerialName("updated_at") val updatedAt: String? = null,
)
