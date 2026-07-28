package com.devshiv.adikala.utils

import android.content.Context
import android.content.SharedPreferences
import com.devshiv.adikala.model.Profile
import com.google.gson.Gson

object ProfileCacheManager {
    private const val PREFS_NAME = "profile_cache"
    private const val KEY_PROFILE = "cached_profile"
    private const val KEY_PHONE = "cached_phone"
    private const val KEY_ADDRESS_NAME = "cached_address_name"
    private const val KEY_ADDRESS = "cached_address"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveProfile(context: Context, profile: Profile) {
        prefs(context).edit()
            .putString(KEY_PROFILE, Gson().toJson(profile))
            .apply()
    }

    fun getProfile(context: Context): Profile? {
        val json = prefs(context).getString(KEY_PROFILE, null) ?: return null
        return try { Gson().fromJson(json, Profile::class.java) } catch (e: Exception) { null }
    }

    fun savePhone(context: Context, phone: String) {
        prefs(context).edit().putString(KEY_PHONE, phone).apply()
    }

    fun getPhone(context: Context): String? = prefs(context).getString(KEY_PHONE, null)

    fun saveAddressName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_ADDRESS_NAME, name).apply()
    }

    fun getAddressName(context: Context): String? = prefs(context).getString(KEY_ADDRESS_NAME, null)

    fun saveAddress(context: Context, address: String) {
        prefs(context).edit().putString(KEY_ADDRESS, address).apply()
    }

    fun getAddress(context: Context): String? = prefs(context).getString(KEY_ADDRESS, null)

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}