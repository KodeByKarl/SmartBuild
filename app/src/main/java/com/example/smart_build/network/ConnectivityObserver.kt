package com.example.smart_build.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
  val isConnected: Flow<Boolean>
}