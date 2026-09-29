package com.example.smart_build.data

/** Home reopens this module card after Back leaves a lab. */
object ReturnToModule {
  var moduleId: Int? = null

  fun mark(id: Int) {
    moduleId = id
  }

  fun consume(): Int? = moduleId.also { moduleId = null }
}
