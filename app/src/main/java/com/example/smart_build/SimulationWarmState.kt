package com.example.smart_build

sealed interface SimulationWarmState {
  data object Cold : SimulationWarmState
  data object EngineReady : SimulationWarmState
  data object Warming : SimulationWarmState
  data object ModuleReady : SimulationWarmState
}
