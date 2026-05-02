package com.dakti.app.data.local.session

import android.content.SharedPreferences
import com.dakti.app.util.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedPreferencesSessionLocalDataSourceTest {

    @Test
    fun defaultThemeMode_isLight() {
        val prefs = InMemorySharedPreferences()

        val dataSource = SharedPreferencesSessionLocalDataSource(prefs)

        assertEquals(AppThemeMode.LIGHT, dataSource.themeMode.value)
    }

    @Test
    fun setThemeMode_persistsAndRestores() {
        val prefs = InMemorySharedPreferences()
        val dataSource = SharedPreferencesSessionLocalDataSource(prefs)

        dataSource.setThemeMode(AppThemeMode.SYSTEM)

        assertEquals("SYSTEM", prefs.getString("key_theme_mode", null))

        val restored = SharedPreferencesSessionLocalDataSource(prefs)
        assertEquals(AppThemeMode.SYSTEM, restored.themeMode.value)
    }

    @Test
    fun clearSession_doesNotClearThemeMode() {
        val prefs = InMemorySharedPreferences()
        val dataSource = SharedPreferencesSessionLocalDataSource(prefs)

        dataSource.setThemeMode(AppThemeMode.DARK)
        dataSource.setSession(
            userId = "user-1",
            accessToken = "access",
            refreshToken = "refresh"
        )

        dataSource.clearSession()

        assertNull(dataSource.authenticatedUserId.value)
        assertEquals(AppThemeMode.DARK, dataSource.themeMode.value)
        assertEquals("DARK", prefs.getString("key_theme_mode", null))
    }
}

private class InMemorySharedPreferences : SharedPreferences {
    private val storage: MutableMap<String, Any?> = mutableMapOf()
    private val listeners: MutableSet<SharedPreferences.OnSharedPreferenceChangeListener> = mutableSetOf()

    override fun getAll(): MutableMap<String, *> = storage.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? {
        if (key == null) return defValue
        return storage[key] as? String ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        if (key == null) return defValues
        @Suppress("UNCHECKED_CAST")
        return (storage[key] as? MutableSet<String>) ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        if (key == null) return defValue
        return storage[key] as? Int ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        if (key == null) return defValue
        return storage[key] as? Long ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        if (key == null) return defValue
        return storage[key] as? Float ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        if (key == null) return defValue
        return storage[key] as? Boolean ?: defValue
    }

    override fun contains(key: String?): Boolean {
        if (key == null) return false
        return storage.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor = EditorImpl(
        storage = storage,
        sharedPreferences = this,
        listeners = listeners
    )

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners += it }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners -= it }
    }
}

private class EditorImpl(
    private val storage: MutableMap<String, Any?>,
    private val sharedPreferences: SharedPreferences,
    private val listeners: Set<SharedPreferences.OnSharedPreferenceChangeListener>
) : SharedPreferences.Editor {

    private object Removed

    private val staged: MutableMap<String, Any?> = mutableMapOf()
    private var shouldClear: Boolean = false

    override fun putString(key: String?, value: String?): SharedPreferences.Editor = applyChange(key, value)

    override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor =
        applyChange(key, values)

    override fun putInt(key: String?, value: Int): SharedPreferences.Editor = applyChange(key, value)

    override fun putLong(key: String?, value: Long): SharedPreferences.Editor = applyChange(key, value)

    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = applyChange(key, value)

    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = applyChange(key, value)

    override fun remove(key: String?): SharedPreferences.Editor {
        if (key != null) staged[key] = Removed
        return this
    }

    override fun clear(): SharedPreferences.Editor {
        shouldClear = true
        return this
    }

    override fun commit(): Boolean {
        apply()
        return true
    }

    override fun apply() {
        val changedKeys = mutableListOf<String>()

        if (shouldClear) {
            changedKeys += storage.keys
            storage.clear()
        }

        staged.forEach { (key, value) ->
            changedKeys += key
            if (value === Removed) {
                storage.remove(key)
            } else {
                storage[key] = value
            }
        }

        changedKeys.distinct().forEach { key ->
            listeners.forEach { listener ->
                listener.onSharedPreferenceChanged(sharedPreferences, key)
            }
        }
    }

    private fun applyChange(key: String?, value: Any?): SharedPreferences.Editor {
        if (key != null) staged[key] = value
        return this
    }
}
