package com.example.smart_build.data.client

import com.example.smart_build.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
  private val url: String = BuildConfig.SUPABASE_URL.trim()
  private val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

  init {
    require(url.isNotEmpty()) {
      "SUPABASE_URL is empty. Copy secrets.properties.example to secrets.properties " +
        "or set the SUPABASE_URL environment variable."
    }
    require(anonKey.isNotEmpty()) {
      "SUPABASE_ANON_KEY is empty. Copy secrets.properties.example to secrets.properties " +
        "or set the SUPABASE_ANON_KEY environment variable."
    }
  }

  val client = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = anonKey
  ) {
    install(Auth) {
      // Used for Android authentication callbacks.
      scheme = BuildConfig.SUPABASE_AUTH_SCHEME.ifBlank { "smartbuild" }
      host = BuildConfig.SUPABASE_AUTH_HOST.ifBlank { "auth" }
    }
    install(Functions)
    install(Postgrest)
  }
}
