package com.example.smart_build.viewmodel.home

data class ModuleCardData(
  val number: String,
  val title: String,
  val description: String,
  val contents: String,
  val benefits: List<String>,
  val image: Int? = null,
  val onGS: () -> Unit,
  val onAS: () -> Unit,
)