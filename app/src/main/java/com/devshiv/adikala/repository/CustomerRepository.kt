package com.devshiv.adikala.repository

import android.util.Log
import com.devshiv.adikala.auth.ShopifyAuthConfig
import com.devshiv.adikala.model.Address
import com.devshiv.adikala.model.OrderLineItem
import com.devshiv.adikala.model.OrderResponse
import com.devshiv.adikala.model.Profile
import com.devshiv.adikala.model.ShopifyOrder
import com.devshiv.adikala.model.TrackingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CustomerRepository {

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val graphqlUrl = ShopifyAuthConfig.CUSTOMER_GRAPHQL_BASE_URL + "graphql"

    suspend fun getProfile(accessToken: String): Profile? = withContext(Dispatchers.IO) {
        try {
            val query = """
        query {
            customer {
                id
                firstName
                lastName
                emailAddress { emailAddress }
                phoneNumber { phoneNumber }
                defaultAddress {
                    id
                    address1
                    address2
                    city
                    country: territoryCode
                    zip
                    phone: phoneNumber
                }
            }
        }
        """.trimIndent()

            val body = JSONObject().apply { put("query", query) }.toString().toRequestBody(JSON)
            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            Log.d("CustomerRepo", "getProfile response: $responseBody")

            val json = JSONObject(responseBody)
            val customer = json.optJSONObject("data")?.optJSONObject("customer") ?: return@withContext null

            val addrJson = customer.optJSONObject("defaultAddress")
            val address = if (addrJson != null) {
                Address(
                    id = addrJson.optString("id", null),
                    address1 = addrJson.optString("address1", null),
                    address2 = addrJson.optString("address2", null),
                    city = addrJson.optString("city", null),
                    country = addrJson.optString("country", null),
                    state = null,
                    zip = addrJson.optString("zip", null),
                    phone = addrJson.optString("phone", null),
                    isDefault = true
                )
            } else null

            Profile(
                firstName = customer.optString("firstName", null),
                lastName = customer.optString("lastName", null),
                email = customer.optJSONObject("emailAddress")?.optString("emailAddress"),
                phone = customer.optJSONObject("phoneNumber")?.optString("phoneNumber"),
                defaultAddress = address
            )
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error getting profile", e)
            null
        }
    }

    suspend fun updateProfile(accessToken: String, firstName: String, lastName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val query = """
            mutation {
                customerUpdate(input: {
                    firstName: "$firstName"
                    lastName: "$lastName"
                }) {
                    customer { id }
                    userErrors { field message }
                }
            }
            """.trimIndent()
            executeMutation(accessToken, query)
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error updating profile", e)
            false
        }
    }

    suspend fun getAddresses(accessToken: String): List<Address> = withContext(Dispatchers.IO) {
        try {
            val variables = JSONObject().apply {
                put("addressesFirst", 20)
            }

            val query = """
        query PersonalAddressList(${'$'}addressesFirst: Int) {
            customer {
                addresses(first: ${'$'}addressesFirst, reverse: true) {
                    nodes {
                        id
                        address1
                        address2
                        city
                        zip
                        country: territoryCode
                        provinceCode: zoneCode
                        phone: phoneNumber
                    }
                }
                defaultAddress { id }
            }
        }
        """.trimIndent()

            val body = JSONObject().apply {
                put("operationName", "PersonalAddressList")
                put("query", query)
                put("variables", variables)
            }.toString().toRequestBody(JSON)

            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            Log.d("CustomerRepo", "Addresses response: $json")

            val customer = json.optJSONObject("data")?.optJSONObject("customer") ?: return@withContext emptyList()
            val defaultAddressId = customer.optJSONObject("defaultAddress")?.optString("id")
            val nodes = customer.optJSONObject("addresses")?.optJSONArray("nodes") ?: return@withContext emptyList()

            val addressList = mutableListOf<Address>()

            for (i in 0 until nodes.length()) {
                val addr = nodes.getJSONObject(i)
                val id = addr.optString("id", null)
                addressList.add(Address(
                    id = id,
                    address1 = addr.optString("address1", null),
                    address2 = addr.optString("address2", null),
                    city = addr.optString("city", null),
                    country = addr.optString("country", null),
                    state = addr.optString("provinceCode", null),
                    zip = addr.optString("zip", null),
                    phone = addr.optString("phone", null),
                    isDefault = id == defaultAddressId
                ))
            }

            addressList.sortByDescending { it.isDefault }

            Log.d("CustomerRepo", "Found ${addressList.size} addresses, default: $defaultAddressId")
            addressList
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error getting addresses", e)
            emptyList()
        }
    }

    suspend fun addAddress(accessToken: String, address: Address): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("CustomerRepo", "Adding address: $address")

            val addressInput = JSONObject().apply {
                put("address1", address.address1 ?: "")
                address.address2?.let { if (it.isNotEmpty()) put("address2", it) }
                put("city", address.city ?: "")
                put("territoryCode", address.country ?: "IN")
                address.state?.let { if (it.isNotEmpty()) put("zoneCode", it) }
                put("zip", address.zip ?: "")
                address.phone?.let { if (it.isNotEmpty()) put("phoneNumber", it) }
            }

            val variables = JSONObject().apply {
                put("addressInput", addressInput)
                put("defaultAddress", address.isDefault)
            }

            val query = """
            mutation AddressCreate(${'$'}addressInput: CustomerAddressInput!, ${'$'}defaultAddress: Boolean) {
                customerAddressCreate(address: ${'$'}addressInput, defaultAddress: ${'$'}defaultAddress) {
                    customerAddress { id }
                    userErrors { field message }
                }
            }
            """.trimIndent()

            val body = JSONObject().apply {
                put("operationName", "AddressCreate")
                put("query", query)
                put("variables", variables)
            }.toString().toRequestBody(JSON)

            Log.d("CustomerRepo", "Body: $body")

            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            Log.d("CustomerRepo", "Response: $responseBody")

            val json = JSONObject(responseBody)
            !json.has("errors")
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error adding address", e)
            false
        }
    }

    suspend fun updateAddress(accessToken: String, addressId: String, address: Address): Boolean = withContext(Dispatchers.IO) {
        try {
            val addressInput = JSONObject().apply {
                put("address1", address.address1 ?: "")
                address.address2?.let { if (it.isNotEmpty()) put("address2", it) }
                put("city", address.city ?: "")
                put("territoryCode", address.country ?: "IN")
                address.state?.let { if (it.isNotEmpty()) put("zoneCode", it) }
                put("zip", address.zip ?: "")
                address.phone?.let { if (it.isNotEmpty()) put("phoneNumber", it) }
            }

            val variables = JSONObject().apply {
                put("addressId", addressId)
                put("addressInput", addressInput)
                put("defaultAddress", address.isDefault)
            }

            val query = """
            mutation AddressUpdate(${'$'}addressId: ID!, ${'$'}addressInput: CustomerAddressInput!, ${'$'}defaultAddress: Boolean) {
                customerAddressUpdate(addressId: ${'$'}addressId, address: ${'$'}addressInput, defaultAddress: ${'$'}defaultAddress) {
                    customerAddress { id }
                    userErrors { field message }
                }
            }
            """.trimIndent()

            val body = JSONObject().apply {
                put("operationName", "AddressUpdate")
                put("query", query)
                put("variables", variables)
            }.toString().toRequestBody(JSON)

            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            Log.d("CustomerRepo", "Update response: $responseBody")

            val json = JSONObject(responseBody)
            !json.has("errors")
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error updating address", e)
            false
        }
    }

    suspend fun deleteAddress(accessToken: String, addressId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("CustomerRepo", "Deleting address: $addressId")

            val variables = JSONObject().apply { put("id", addressId) }

            val query = """
        mutation AddressDelete(${'$'}id: ID!) {
            customerAddressDelete(addressId: ${'$'}id) {
                deletedAddressId
                userErrors { field message }
            }
        }
        """.trimIndent()

            val body = JSONObject().apply {
                put("operationName", "AddressDelete")
                put("query", query)
                put("variables", variables)
            }.toString().toRequestBody(JSON)

            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            Log.d("CustomerRepo", "Delete response: $responseBody")

            val json = JSONObject(responseBody)

            if (json.has("errors")) {
                val errors = json.getJSONArray("errors")
                Log.e("CustomerRepo", "GraphQL errors: $errors")
                return@withContext false
            }

            val data = json.optJSONObject("data")?.optJSONObject("customerAddressDelete")
            val userErrors = data?.optJSONArray("userErrors")
            if (userErrors != null && userErrors.length() > 0) {
                val error = userErrors.getJSONObject(0)
                Log.e("CustomerRepo", "User error: ${error.optString("message")}")
                return@withContext false
            }

            val deletedId = data?.optString("deletedAddressId")
            Log.d("CustomerRepo", "Deleted address ID: $deletedId")

            deletedId != null && deletedId.isNotEmpty()
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error deleting address", e)
            false
        }
    }

    private suspend fun executeMutation(accessToken: String, query: String): Boolean {
        val body = JSONObject().apply { put("query", query) }.toString().toRequestBody(JSON)
        val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
        val response = client.newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: "")
        return !json.has("errors")
    }


    suspend fun getOrders(accessToken: String, first: Int = 20, after: String? = null): OrderResponse = withContext(Dispatchers.IO) {
        try {
            val afterParam = if (after != null) "\"$after\"" else "null"

            val query = """
        query {
            customer {
                orders(first: $first, after: $afterParam, reverse: true) {
                    edges {
                        cursor
                        node {
                            id
                            orderNumber: number
                            name
                            financialStatus
                            fulfillmentStatus
                            processedAt
                            cancelledAt
                            totalPrice { amount currencyCode }
                            lineItems(first: 5) {
                                nodes {
                                    title
                                    quantity
                                    originalTotalPrice { amount currencyCode }
                                    variant {
                                        image { url }
                                        product { handle }
                                    }
                                }
                            }
                            fulfillments(first: 1) {
                                nodes {
                                    status
                                    estimatedDeliveryAt
                                    trackingInformation {
                                        number
                                        url
                                        company
                                    }
                                }
                            }
                        }
                    }
                    pageInfo {
                        hasNextPage
                        endCursor
                    }
                }
            }
        }
        """.trimIndent()

            val body = JSONObject().apply { put("query", query) }.toString().toRequestBody(JSON)
            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")

            val ordersData = json.optJSONObject("data")?.optJSONObject("customer")?.optJSONObject("orders")
            val edges = ordersData?.optJSONArray("edges") ?: return@withContext OrderResponse(emptyList(), false, null)
            val pageInfo = ordersData?.optJSONObject("pageInfo")

            val orders = mutableListOf<ShopifyOrder>()
            for (i in 0 until edges.length()) {
                val edge = edges.getJSONObject(i)
                val node = edge.getJSONObject("node")

                val totalPrice = node.optJSONObject("totalPrice")
                val lineItems = node.optJSONObject("lineItems")?.optJSONArray("nodes")
                val items = mutableListOf<OrderLineItem>()

                if (lineItems != null) {
                    for (j in 0 until lineItems.length()) {
                        val item = lineItems.getJSONObject(j)
                        val price = item.optJSONObject("originalTotalPrice")
                        val image = item.optJSONObject("variant")?.optJSONObject("image")
                        items.add(
                            OrderLineItem(
                                title = item.optString("title", ""),
                                quantity = item.optInt("quantity", 0),
                                price = "${
                                    price?.optString(
                                        "currencyCode",
                                        "INR"
                                    )
                                } ${price?.optString("amount", "0")}",
                                imageUrl = image?.optString("url"),
                                productHandle = item.optJSONObject("variant")
                                    ?.optJSONObject("product")?.optString("handle")
                            )
                        )
                    }
                }

                val fulfillments = node.optJSONObject("fulfillments")?.optJSONArray("nodes")
                var trackingInfo: TrackingInfo? = null
                if (fulfillments != null && fulfillments.length() > 0) {
                    val fulfillment = fulfillments.getJSONObject(0)
                    val tracking = fulfillment.optJSONObject("trackingInformation")
                    trackingInfo = TrackingInfo(
                        number = tracking?.optString("number"),
                        url = tracking?.optString("url"),
                        company = tracking?.optString("company"),
                        estimatedDelivery = fulfillment.optString("estimatedDeliveryAt", null)
                    )
                }

                orders.add(
                    ShopifyOrder(
                        id = node.optString("id"),
                        orderNumber = node.optInt("orderNumber"),
                        name = node.optString("name", ""),
                        financialStatus = node.optString("financialStatus", null),
                        fulfillmentStatus = node.optString("fulfillmentStatus", null),
                        processedAt = node.optString("processedAt", null),
                        cancelledAt = node.optString("cancelledAt", null),
                        totalPrice = "${
                            totalPrice?.optString(
                                "currencyCode",
                                "INR"
                            )
                        } ${totalPrice?.optString("amount", "0")}",
                        currencyCode = totalPrice?.optString("currencyCode", "INR") ?: "INR",
                        items = items,
                        trackingInfo = trackingInfo
                    )
                )
            }

            OrderResponse(
                orders = orders,
                hasNextPage = pageInfo?.optBoolean("hasNextPage") ?: false,
                endCursor = pageInfo?.optString("endCursor", null)
            )
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error getting orders", e)
            OrderResponse(emptyList(), false, null)
        }
    }
}