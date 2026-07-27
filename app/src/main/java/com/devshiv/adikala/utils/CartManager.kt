package com.devshiv.adikala.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private const val PREFS_NAME = "adikala_cart"
    private const val KEY_CART_ID = "active_cart_id"
    private const val KEY_PRODUCT_NAMES = "product_names"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCartId(context: Context): String? = prefs(context).getString(KEY_CART_ID, null)

    fun saveCartId(context: Context, cartId: String) =
        prefs(context).edit().putString(KEY_CART_ID, cartId).apply()

    fun saveProductName(context: Context, variantId: String, name: String) {
        val map = getProductNames(context).toMutableMap()
        map[variantId] = name
        prefs(context).edit().putString(KEY_PRODUCT_NAMES, Gson().toJson(map)).apply()
    }

    fun getProductName(context: Context, variantId: String): String? {
        return getProductNames(context)[variantId]
    }

    fun removeProductName(context: Context, variantId: String) {
        val map = getProductNames(context).toMutableMap()
        map.remove(variantId)
        prefs(context).edit().putString(KEY_PRODUCT_NAMES, Gson().toJson(map)).apply()
    }

    private fun getProductNames(context: Context): Map<String, String> {
        val json = prefs(context).getString(KEY_PRODUCT_NAMES, null) ?: return emptyMap()
        return try {
            Gson().fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}