package com.example.smart_build.data.client

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions

object SupabaseClient {
  val client = createSupabaseClient(
    supabaseUrl = "https://bremitowvdmqfxgxogkb.supabase.co",
    supabaseKey = "sb_publishable_-Jff5pmZ14byP_ZSc40T8g_2e-irkDl"
  ) {
    install(Auth) {
      // Used for Android authentication callbacks.
      scheme = "smartbuild"
      host = "auth"
    }
    install(Functions)
  }
}