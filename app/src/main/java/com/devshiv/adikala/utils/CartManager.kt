package com.devshiv.adikala.utils

import android.content.Context
import android.content.SharedPreferences

object CartManager {
    private const val PREFS_NAME = "adikala_cart"
    private const val KEY_CART_ID = "active_cart_id"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCartId(context: Context): String? = prefs(context).getString(KEY_CART_ID, null)

    fun saveCartId(context: Context, cartId: String) =
        prefs(context).edit().putString(KEY_CART_ID, cartId).apply()

    fun clear(context: Context) =
        prefs(context).edit().remove(KEY_CART_ID).apply()
}