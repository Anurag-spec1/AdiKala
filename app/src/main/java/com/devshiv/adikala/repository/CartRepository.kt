package com.devshiv.adikala.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CartRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val endpoint = "https://www.adikala.in/api/2026-01/graphql.json"
    private val token = "a8931a13b14b1fdc80e95151a87c6655"

    data class CartData(
        val id: String,
        val checkoutUrl: String?,
        val totalQuantity: Int,
        val totalAmount: String,
        val currencyCode: String,
        val items: List<CartLineItem>
    )

    data class CartLineItem(
        val lineId: String,
        val variantId: String,
        val quantity: Int,
        val title: String,
        val price: String,
        val imageUrl: String?,
        val size: String?
    )

    suspend fun createCart(variantId: String, quantity: Int): CartData? = withContext(Dispatchers.IO) {
        try {
            val query = """
            mutation CartCreate {
                cartCreate(input: { lines: [{ merchandiseId: "$variantId", quantity: $quantity }] }) {
                    cart {
                        id
                        checkoutUrl
                        totalQuantity
                        cost { totalAmount { amount currencyCode } }
                        lines(first: 100) { nodes { id quantity merchandise {
                            ... on ProductVariant {
                                id title
                                selectedOptions { name value }
                                price { amount currencyCode }
                                image { url }
                            }
                        } } }
                    }
                    userErrors { field message }
                }
            }
            """.trimIndent()

            val response = execute(query)
            val cartJson = response.optJSONObject("data")
                ?.optJSONObject("cartCreate")
                ?.optJSONObject("cart")

            parseCart(cartJson)
        } catch (e: Exception) {
            Log.e("CartRepo", "createCart failed", e)
            null
        }
    }

    suspend fun getCart(cartId: String): CartData? = withContext(Dispatchers.IO) {
        try {
            val query = """
            query CartGet {
                cart(id: "$cartId") {
                    id checkoutUrl totalQuantity
                    cost { totalAmount { amount currencyCode } }
                    lines(first: 100) { nodes { id quantity merchandise {
                        ... on ProductVariant {
                            id title
                            selectedOptions { name value }
                            price { amount currencyCode }
                            image { url }
                        }
                    } } }
                }
            }
            """.trimIndent()

            val response = execute(query)
            val cartJson = response.optJSONObject("data")?.optJSONObject("cart")
            parseCart(cartJson)
        } catch (e: Exception) {
            Log.e("CartRepo", "getCart failed", e)
            null
        }
    }

    suspend fun addToCart(cartId: String, variantId: String, quantity: Int): CartData? = withContext(Dispatchers.IO) {
        try {
            val query = """
            mutation CartLinesAdd {
                cartLinesAdd(cartId: "$cartId", lines: [{ merchandiseId: "$variantId", quantity: $quantity }]) {
                    cart {
                        id checkoutUrl totalQuantity
                        cost { totalAmount { amount currencyCode } }
                        lines(first: 100) { nodes { id quantity merchandise {
                            ... on ProductVariant {
                                id title
                                selectedOptions { name value }
                                price { amount currencyCode }
                                image { url }
                            }
                        } } }
                    }
                    userErrors { field message }
                }
            }
            """.trimIndent()

            val response = execute(query)
            val cartJson = response.optJSONObject("data")
                ?.optJSONObject("cartLinesAdd")
                ?.optJSONObject("cart")

            parseCart(cartJson)
        } catch (e: Exception) {
            Log.e("CartRepo", "addToCart failed", e)
            null
        }
    }

    suspend fun updateQuantity(cartId: String, lineId: String, quantity: Int): CartData? = withContext(Dispatchers.IO) {
        try {
            val query = """
            mutation CartLinesUpdate {
                cartLinesUpdate(cartId: "$cartId", lines: [{ id: "$lineId", quantity: $quantity }]) {
                    cart {
                        id checkoutUrl totalQuantity
                        cost { totalAmount { amount currencyCode } }
                        lines(first: 100) { nodes { id quantity merchandise {
                            ... on ProductVariant {
                                id title
                                selectedOptions { name value }
                                price { amount currencyCode }
                                image { url }
                            }
                        } } }
                    }
                    userErrors { field message }
                }
            }
            """.trimIndent()

            val response = execute(query)
            val cartJson = response.optJSONObject("data")
                ?.optJSONObject("cartLinesUpdate")
                ?.optJSONObject("cart")

            parseCart(cartJson)
        } catch (e: Exception) {
            Log.e("CartRepo", "updateQuantity failed", e)
            null
        }
    }

    suspend fun removeFromCart(cartId: String, lineIds: List<String>): CartData? = withContext(Dispatchers.IO) {
        try {
            val ids = lineIds.joinToString("\", \"") { it }
            val query = """
            mutation CartLinesRemove {
                cartLinesRemove(cartId: "$cartId", lineIds: ["$ids"]) {
                    cart {
                        id checkoutUrl totalQuantity
                        cost { totalAmount { amount currencyCode } }
                        lines(first: 100) { nodes { id quantity merchandise {
                            ... on ProductVariant {
                                id title
                                selectedOptions { name value }
                                price { amount currencyCode }
                                image { url }
                            }
                        } } }
                    }
                    userErrors { field message }
                }
            }
            """.trimIndent()

            val response = execute(query)
            val cartJson = response.optJSONObject("data")
                ?.optJSONObject("cartLinesRemove")
                ?.optJSONObject("cart")

            parseCart(cartJson)
        } catch (e: Exception) {
            Log.e("CartRepo", "removeFromCart failed", e)
            null
        }
    }

    private suspend fun execute(query: String): JSONObject {
        val body = JSONObject().apply { put("query", query) }.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(endpoint)
            .header("X-Shopify-Storefront-Access-Token", token)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        return JSONObject(response.body?.string() ?: "{}")
    }

    private fun parseCart(json: JSONObject?): CartData? {
        if (json == null) return null

        val cost = json.optJSONObject("cost")?.optJSONObject("totalAmount")
        val lines = json.optJSONObject("lines")?.optJSONArray("nodes") ?: return null

        val items = mutableListOf<CartLineItem>()
        for (i in 0 until lines.length()) {
            val line = lines.getJSONObject(i)
            val merch = line.optJSONObject("merchandise") ?: continue
            val price = merch.optJSONObject("price")
            val image = merch.optJSONObject("image")
            val options = merch.optJSONArray("selectedOptions")

            var size: String? = null
            if (options != null) {
                for (j in 0 until options.length()) {
                    val opt = options.getJSONObject(j)
                    if (opt.optString("name").equals("Size", true) ||
                        opt.optString("name").equals("Title", true)) {
                        size = opt.optString("value", null)
                    }
                }
            }

            items.add(
                CartLineItem(
                    lineId = line.optString("id"),
                    variantId = merch.optString("id"),
                    quantity = line.optInt("quantity", 1),
                    title = merch.optString("title", ""),
                    price = "${price?.optString("currencyCode", "INR")} ${price?.optString("amount", "0")}",
                    imageUrl = image?.optString("url"),
                    size = size
                )
            )
        }

        return CartData(
            id = json.optString("id"),
            checkoutUrl = json.optString("checkoutUrl", null),
            totalQuantity = json.optInt("totalQuantity", 0),
            totalAmount = "${cost?.optString("currencyCode", "INR")} ${cost?.optString("amount", "0")}",
            currencyCode = cost?.optString("currencyCode", "INR") ?: "INR",
            items = items
        )
    }
}