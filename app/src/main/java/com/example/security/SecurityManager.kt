package com.example.security

import android.content.Context
import android.content.SharedPreferences

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("corderof_security_prefs", Context.MODE_PRIVATE)

    fun isPinLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false)
    }

    fun setPinLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }

    fun getStoredPin(): String {
        return prefs.getString(KEY_PIN_CODE, "") ?: ""
    }

    fun setPinCode(pin: String) {
        prefs.edit().putString(KEY_PIN_CODE, pin).apply()
        if (pin.isNotEmpty()) {
            setPinLockEnabled(true)
        }
    }

    fun verifyPin(inputPin: String): Boolean {
        val stored = getStoredPin()
        return stored.isEmpty() || stored == inputPin
    }

    fun isOfflineOnlyMode(): Boolean {
        return prefs.getBoolean(KEY_OFFLINE_MODE, true)
    }

    fun setOfflineOnlyMode(offline: Boolean) {
        prefs.edit().putBoolean(KEY_OFFLINE_MODE, offline).apply()
    }

    companion object {
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_OFFLINE_MODE = "offline_mode"
    }
}
