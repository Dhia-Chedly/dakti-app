package com.dakti.app.util

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromStorage(raw: String?): AppThemeMode {
            return entries.firstOrNull { mode ->
                mode.name.equals(raw, ignoreCase = true)
            } ?: LIGHT
        }
    }
}
