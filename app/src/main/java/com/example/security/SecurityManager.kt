package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

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

    private fun hashPin(pin: String): String {
        if (pin.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(("corderof_salt_" + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun setPinCode(pin: String) {
        if (pin.isEmpty()) {
            prefs.edit().remove(KEY_PIN_CODE).putBoolean(KEY_PIN_ENABLED, false).apply()
        } else {
            val hashed = hashPin(pin)
            prefs.edit().putString(KEY_PIN_CODE, hashed).putBoolean(KEY_PIN_ENABLED, true).apply()
        }
    }

    fun verifyPin(inputPin: String): Boolean {
        val stored = getStoredPin()
        if (stored.isEmpty()) return true
        
        // Check if stored pin is hashed (SHA-256 hex length is 64)
        return if (stored.length == 64) {
            stored == hashPin(inputPin)
        } else {
            // Legacy raw pin match -> auto-upgrade to hashed pin
            val isMatch = stored == inputPin
            if (isMatch) {
                setPinCode(inputPin)
            }
            isMatch
        }
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

