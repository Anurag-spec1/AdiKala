package com.hustlers.adikala.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class ShopifyAuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("shopify_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "customer_token"
        private const val KEY_EMAIL = "customer_email"
        private const val KEY_FIRST_NAME = "customer_first_name"
        private const val KEY_LAST_NAME = "customer_last_name"
        private const val KEY_PHONE = "customer_phone"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginData(token: String, firstName: String?, lastName: String?, email: String?, phone: String?) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_FIRST_NAME, firstName)
            .putString(KEY_LAST_NAME, lastName)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        Log.d("ShopifyAuth", "Login data saved")
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getFirstName(): String? = prefs.getString(KEY_FIRST_NAME, null)
    fun getLastName(): String? = prefs.getString(KEY_LAST_NAME, null)

    fun getFullName(): String {
        val first = getFirstName() ?: ""
        val last = getLastName() ?: ""
        return if (first.isNotEmpty() && last.isNotEmpty()) "$first $last"
        else if (first.isNotEmpty()) first
        else if (last.isNotEmpty()) last
        else "User"
    }

    fun getInitial(): String {
        val name = getFirstName() ?: getLastName() ?: "U"
        return name.first().uppercase()
    }

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}