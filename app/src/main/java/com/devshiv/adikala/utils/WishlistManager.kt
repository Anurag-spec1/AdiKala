package com.devshiv.adikala.utils

import android.content.Context
import android.content.SharedPreferences
import com.devshiv.adikala.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object WishlistManager {

    private const val PREFS_NAME = "wishlist_prefs"
    private const val KEY_WISHLIST = "wishlist_items"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getWishlist(context: Context): List<Product> {
        val json = getPrefs(context).getString(KEY_WISHLIST, "[]") ?: "[]"
        val type = object : TypeToken<List<Product>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isWishlisted(context: Context, productId: String): Boolean {
        return getWishlist(context).any { it.id == productId }
    }

    fun toggleWishlist(context: Context, product: Product): Boolean {
        val wishlist = getWishlist(context).toMutableList()
        val existingIndex = wishlist.indexOfFirst { it.id == product.id }

        return if (existingIndex >= 0) {
            wishlist.removeAt(existingIndex)
            saveWishlist(context, wishlist)
            false
        } else {
            wishlist.add(0, product.copy(isWishlisted = true))
            saveWishlist(context, wishlist)
            true
        }
    }

    fun addToWishlist(context: Context, product: Product) {
        val wishlist = getWishlist(context).toMutableList()
        if (wishlist.none { it.id == product.id }) {
            wishlist.add(0, product.copy(isWishlisted = true))
            saveWishlist(context, wishlist)
        }
    }

    fun removeFromWishlist(context: Context, productId: String) {
        val wishlist = getWishlist(context).toMutableList()
        wishlist.removeAll { it.id == productId }
        saveWishlist(context, wishlist)
    }

    fun getWishlistCount(context: Context): Int {
        return getWishlist(context).size
    }

    private fun saveWishlist(context: Context, wishlist: List<Product>) {
        getPrefs(context).edit().putString(KEY_WISHLIST, gson.toJson(wishlist)).apply()
    }

    fun clearWishlist(context: Context) {
        getPrefs(context).edit().remove(KEY_WISHLIST).apply()
    }
}