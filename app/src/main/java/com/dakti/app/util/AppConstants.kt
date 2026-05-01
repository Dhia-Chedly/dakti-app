package com.dakti.app.util

import com.dakti.app.BuildConfig

object AppConstants {
    const val DEFAULT_SUPABASE_URL = "https://example.supabase.co/"
    val SUPABASE_URL: String
        get() = BuildConfig.SUPABASE_URL.ifBlank { DEFAULT_SUPABASE_URL }
    val SUPABASE_ANON_KEY: String
        get() = BuildConfig.SUPABASE_ANON_KEY
    val IS_SUPABASE_CONFIGURED: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    const val SKIP_AUTH_FOR_DEMO = false

    const val NETWORK_TIMEOUT_SECONDS = 30L
}
