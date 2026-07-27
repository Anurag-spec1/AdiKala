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

    data class ProductSuggestion(
        val id: String,
        val title: String,
        val imageUrl: String,
        val price: String,
        val variantId: String
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
            Log.d("CartRepo", "=== CREATE CART ===")
            Log.d("CartRepo", "Variant ID: $variantId, Quantity: $quantity")

            val query = """
            mutation CartCreate {
                cartCreate(input: { lines: [{ merchandiseId: "$variantId", quantity: $quantity }] }) {
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
            Log.d("CartRepo", "Create cart response: $response")

            val data = response.optJSONObject("data")?.optJSONObject("cartCreate")
            val cartJson = data?.optJSONObject("cart")
            val userErrors = data?.optJSONArray("userErrors")

            if (userErrors != null && userErrors.length() > 0) {
                Log.e("CartRepo", "User errors: $userErrors")
                return@withContext null
            }

            if (response.has("errors")) {
                Log.e("CartRepo", "GraphQL errors: ${response.optJSONArray("errors")}")
                return@withContext null
            }

            parseCart(cartJson)
        } catch (e: Exception) {
            Log.e("CartRepo", "createCart failed", e)
            null
        }
    }

    suspend fun getCheckoutUrl(cartId: String, accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = """
        query {
            cart(id: "$cartId") {
                checkoutUrl
            }
        }
        """.trimIndent()

            val response = execute(query)
            val url = response.optJSONObject("data")?.optJSONObject("cart")?.optString("checkoutUrl")
            Log.d("CartRepo", "Checkout URL: $url")
            url
        } catch (e: Exception) {
            Log.e("CartRepo", "Error getting checkout URL", e)
            null
        }
    }

    suspend fun setCartBuyerIdentity(cartId: String, accessToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("CartRepo", "=== SET CART BUYER IDENTITY ===")

            val query = """
        mutation {
            cartBuyerIdentityUpdate(cartId: "$cartId", buyerIdentity: {
                customerAccessToken: "$accessToken"
            }) {
                cart { id }
                userErrors { field message }
            }
        }
        """.trimIndent()

            val response = execute(query)
            Log.d("CartRepo", "Buyer identity response: $response")

            val userErrors = response.optJSONObject("data")?.optJSONObject("cartBuyerIdentityUpdate")?.optJSONArray("userErrors")
            if (userErrors != null && userErrors.length() > 0) {
                Log.e("CartRepo", "Buyer identity errors: $userErrors")
                return@withContext false
            }

            !response.has("errors")
        } catch (e: Exception) {
            Log.e("CartRepo", "Error setting buyer identity", e)
            false
        }
    }

    suspend fun getCheckoutUrlWithAuth(cartId: String, accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("CartRepo", "=== GET CHECKOUT URL WITH AUTH ===")

            // Step 1: Set buyer identity on cart
            val identitySet = setCartBuyerIdentity(cartId, accessToken)
            if (!identitySet) {
                Log.e("CartRepo", "Failed to set buyer identity")
            }

            // Step 2: Get updated checkout URL
            val query = """
        query {
            cart(id: "$cartId") {
                checkoutUrl
            }
        }
        """.trimIndent()

            val response = execute(query)
            val baseUrl = response.optJSONObject("data")?.optJSONObject("cart")?.optString("checkoutUrl")

            // Step 3: Append sso=silent for browser session SSO
            val finalUrl = if (baseUrl != null) {
                if (baseUrl.contains("?")) {
                    "$baseUrl&sso=silent"
                } else {
                    "$baseUrl?sso=silent"
                }
            } else null

            Log.d("CartRepo", "Final checkout URL: $finalUrl")
            finalUrl
        } catch (e: Exception) {
            Log.e("CartRepo", "Error", e)
            null
        }
    }

    suspend fun getRecommendedProducts(variantIds: List<String>): List<ProductSuggestion> = withContext(Dispatchers.IO) {
        try {
            Log.d("CartRepo", "=== GET RECOMMENDATIONS ===")
            Log.d("CartRepo", "Variant IDs: $variantIds")

            if (variantIds.isEmpty()) {
                Log.d("CartRepo", "No products in cart, falling back to best sellers")
                return@withContext getBestSellers()
            }

            // First, get the Product ID from the Variant ID
            val variantId = variantIds.first()
            val productQuery = """
        query {
            node(id: "$variantId") {
                ... on ProductVariant {
                    product { id }
                }
            }
        }
        """.trimIndent()

            val productResponse = execute(productQuery)
            val productId = productResponse.optJSONObject("data")?.optJSONObject("node")?.optJSONObject("product")?.optString("id")

            Log.d("CartRepo", "Product ID from variant: $productId")

            if (productId == null) {
                Log.d("CartRepo", "Could not find product ID, falling back to best sellers")
                return@withContext getBestSellers()
            }

            // Now get recommendations using Product ID
            val query = """
        query {
            productRecommendations(productId: "$productId") {
                id
                title
                featuredImage { url }
                priceRange { minVariantPrice { amount currencyCode } }
                variants(first: 1) { nodes { id } }
            }
        }
        """.trimIndent()

            val response = execute(query)
            Log.d("CartRepo", "Recommendations response: $response")

            val nodes = response.optJSONObject("data")?.optJSONArray("productRecommendations")
            Log.d("CartRepo", "Recommendations count: ${nodes?.length() ?: 0}")

            if (nodes == null || nodes.length() == 0) {
                Log.d("CartRepo", "No recommendations, falling back to best sellers")
                return@withContext getBestSellers()
            }

            val result = (0 until nodes.length()).map { i ->
                val p = nodes.getJSONObject(i)
                ProductSuggestion(
                    id = p.optString("id"),
                    title = p.optString("title"),
                    imageUrl = p.optJSONObject("featuredImage")?.optString("url") ?: "",
                    price = p.optJSONObject("priceRange")?.optJSONObject("minVariantPrice")?.optString("amount") ?: "0",
                    variantId = p.optJSONObject("variants")?.optJSONArray("nodes")?.getJSONObject(0)?.optString("id") ?: ""
                )
            }

            Log.d("CartRepo", "=== RECOMMENDATIONS RESULT: ${result.size} items ===")
            result
        } catch (e: Exception) {
            Log.e("CartRepo", "getRecommendedProducts failed", e)
            getBestSellers()
        }
    }

   suspend fun getBestSellers(): List<ProductSuggestion> = withContext(Dispatchers.IO) {
        try {
            val query = """
        query {
            products(first: 10, sortKey: BEST_SELLING) {
                nodes {
                    id
                    title
                    featuredImage { url }
                    priceRange { minVariantPrice { amount currencyCode } }
                    variants(first: 1) { nodes { id } }
                }
            }
        }
        """.trimIndent()

            val response = execute(query)
            val products = response.optJSONObject("data")?.optJSONObject("products")?.optJSONArray("nodes")
                ?: return@withContext emptyList()

            (0 until products.length()).map { i ->
                val p = products.getJSONObject(i)
                ProductSuggestion(
                    id = p.optString("id"),
                    title = p.optString("title"),
                    imageUrl = p.optJSONObject("featuredImage")?.optString("url") ?: "",
                    price = p.optJSONObject("priceRange")?.optJSONObject("minVariantPrice")?.optString("amount") ?: "0",
                    variantId = p.optJSONObject("variants")?.optJSONArray("nodes")?.getJSONObject(0)?.optString("id") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("CartRepo", "getBestSellers failed", e)
            emptyList()
        }
    }

    suspend fun getProductSuggestions(limit: Int = 10): List<ProductSuggestion> = withContext(Dispatchers.IO) {
        try {
            val query = """
        query {
            products(first: $limit, sortKey: BEST_SELLING) {
                nodes {
                    id
                    title
                    featuredImage { url }
                    priceRange { minVariantPrice { amount currencyCode } }
                    variants(first: 1) { nodes { id } }
                }
            }
        }
        """.trimIndent()

            val response = execute(query)
            val products = response.optJSONObject("data")?.optJSONObject("products")?.optJSONArray("nodes")
                ?: return@withContext emptyList()

            (0 until products.length()).map { i ->
                val p = products.getJSONObject(i)
                ProductSuggestion(
                    id = p.optString("id"),
                    title = p.optString("title"),
                    imageUrl = p.optJSONObject("featuredImage")?.optString("url") ?: "",
                    price = p.optJSONObject("priceRange")?.optJSONObject("minVariantPrice")?.optString("amount") ?: "0",
                    variantId = p.optJSONObject("variants")?.optJSONArray("nodes")?.getJSONObject(0)?.optString("id") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("CartRepo", "getProductSuggestions failed", e)
            emptyList()
        }
    }

    suspend fun getCart(cartId: String): CartData? = withContext(Dispatchers.IO) {
        try {
            Log.d("CartRepo", "=== GET CART ===")
            Log.d("CartRepo", "Cart ID: $cartId")

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
            Log.d("CartRepo", "Get cart response: $response")

            if (response.has("errors")) {
                Log.e("CartRepo", "GraphQL errors: ${response.optJSONArray("errors")}")
                return@withContext null
            }

            parseCart(response.optJSONObject("data")?.optJSONObject("cart"))
        } catch (e: Exception) {
            Log.e("CartRepo", "getCart failed", e)
            null
        }
    }

    suspend fun addToCart(cartId: String, variantId: String, quantity: Int): CartData? = withContext(Dispatchers.IO) {
        try {
            Log.d("CartRepo", "=== ADD TO CART ===")
            Log.d("CartRepo", "Cart ID: $cartId, Variant: $variantId, Qty: $quantity")

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
            val data = response.optJSONObject("data")?.optJSONObject("cartLinesAdd")
            parseCart(data?.optJSONObject("cart"))
        } catch (e: Exception) {
            Log.e("CartRepo", "addToCart failed", e)
            null
        }
    }

    suspend fun updateQuantity(cartId: String, lineId: String, quantity: Int): CartData? = withContext(Dispatchers.IO) {
        try {
            Log.d("CartRepo", "=== UPDATE QTY ===")
            Log.d("CartRepo", "Cart: $cartId, Line: $lineId, Qty: $quantity")

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
            parseCart(response.optJSONObject("data")?.optJSONObject("cartLinesUpdate")?.optJSONObject("cart"))
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
            parseCart(response.optJSONObject("data")?.optJSONObject("cartLinesRemove")?.optJSONObject("cart"))
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
                    if (opt.optString("name") != "Title") {
                        size = "${opt.optString("name")}: ${opt.optString("value")}"
                    }
                }
            }

            items.add(CartLineItem(
                lineId = line.optString("id"),
                variantId = merch.optString("id"),
                quantity = line.optInt("quantity", 1),
                title = merch.optString("title", ""),
                price = "${price?.optString("currencyCode", "INR")} ${price?.optString("amount", "0")}",
                imageUrl = image?.optString("url"),
                size = size
            ))
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