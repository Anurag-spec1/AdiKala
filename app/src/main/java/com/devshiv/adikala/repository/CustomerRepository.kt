package com.devshiv.adikala.repository

import android.util.Log
import com.devshiv.adikala.auth.ShopifyAuthConfig
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

    data class Profile(
        val firstName: String?,
        val lastName: String?,
        val email: String?,
        val phone: String?,
        val defaultAddress: Address?
    )

    data class Address(
        val id: String?,
        val address1: String?,
        val address2: String?,
        val city: String?,
        val country: String?,
        val state: String?,
        val zip: String?,
        val phone: String?,
        val isDefault: Boolean = false
    )

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
                        country
                        zip
                    }
                }
            }
            """.trimIndent()

            val body = JSONObject().apply { put("query", query) }.toString().toRequestBody(JSON)
            val request = Request.Builder().url(graphqlUrl).header("Authorization", accessToken).post(body).build()
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
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
            val variables = JSONObject().apply { put("addressesFirst", 20) }

            val query = """
            query PersonalAddressList(${'$'}addressesFirst: Int) {
                customer {
                    addresses(first: ${'$'}addressesFirst, reverse: true, skipDefault: true) {
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

            val customer = json.optJSONObject("data")?.optJSONObject("customer") ?: return@withContext emptyList()
            val defaultAddressId = customer.optJSONObject("defaultAddress")?.optString("id")
            val nodes = customer.optJSONObject("addresses")?.optJSONArray("nodes") ?: return@withContext emptyList()

            (0 until nodes.length()).map { i ->
                val addr = nodes.getJSONObject(i)
                Address(
                    id = addr.optString("id", null),
                    address1 = addr.optString("address1", null),
                    address2 = addr.optString("address2", null),
                    city = addr.optString("city", null),
                    country = addr.optString("country", null),
                    state = addr.optString("provinceCode", null),
                    zip = addr.optString("zip", null),
                    phone = addr.optString("phone", null),
                    isDefault = addr.optString("id") == defaultAddressId
                )
            }
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
            !json.has("errors")
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
}