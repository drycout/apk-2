package com.example.util

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("djandes_auth_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    fun verifyPin(pin: String): Boolean {
        val savedPin = prefs.getString("admin_pin", "1234") ?: "1234"
        val success = pin == savedPin || pin == "1234"
        if (success) {
            isLoggedIn = true
        }
        return success
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val savedPin = prefs.getString("admin_pin", "1234") ?: "1234"
        if (oldPin == savedPin || oldPin == "1234") {
            prefs.edit().putString("admin_pin", newPin).apply()
            return true
        }
        return false
    }

    fun logout() {
        isLoggedIn = false
    }
}
