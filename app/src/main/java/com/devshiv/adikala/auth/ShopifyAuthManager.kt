package com.devshiv.adikala.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ShopifyAuthManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            "shopify_auth_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_EMAIL = "customer_email"
        private const val KEY_FIRST_NAME = "customer_first_name"
        private const val KEY_LAST_NAME = "customer_last_name"
        private const val KEY_PHONE = "customer_phone"
    }

    fun saveTokens(access: String, refresh: String?, idToken: String?, expiresAt: Long) {
        prefs.edit().apply {
            putString(KEY_ACCESS, access)
            if (!refresh.isNullOrEmpty()) putString(KEY_REFRESH, refresh)
            if (!idToken.isNullOrEmpty()) putString(KEY_ID_TOKEN, idToken)
            putLong(KEY_EXPIRES_AT, expiresAt)
        }.apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    fun getIdToken(): String? = prefs.getString(KEY_ID_TOKEN, null)
    fun getExpiresAt(): Long = prefs.getLong(KEY_EXPIRES_AT, 0L)

    fun isLoggedIn(): Boolean = !getRefreshToken().isNullOrEmpty()


    fun saveProfile(firstName: String?, lastName: String?, email: String?, phone: String?) {
        prefs.edit()
            .putString(KEY_FIRST_NAME, firstName)
            .putString(KEY_LAST_NAME, lastName)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .apply()
    }

    fun getFirstName(): String? = prefs.getString(KEY_FIRST_NAME, null)
    fun getLastName(): String? = prefs.getString(KEY_LAST_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)

    fun getFullName(): String {
        val first = getFirstName().orEmpty()
        val last = getLastName().orEmpty()
        return listOf(first, last).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { "User" }
    }

    fun getInitial(): String =
        (getFirstName() ?: getLastName() ?: getEmail() ?: "U").trim().firstOrNull()?.uppercase() ?: "U"

    fun clear() = prefs.edit().clear().apply()
}