package com.example.smart_build.viewmodel.home

data class ModuleCardData(
  val number: String,
  /** Module id 0..4 used for progress lookups. */
  val moduleId: Int,
  val title: String,
  val description: String,
  val contents: String,
  val benefits: List<String>,
  val image: Int? = null,
  val locked: Boolean = false,
  /** 0..100 from local cache / Supabase. */
  val progressPercent: Float = 0f,
  val guidedDone: Boolean = false,
  val assessmentDone: Boolean = false,
  val onGS: () -> Unit,
  val onAS: () -> Unit,
)
