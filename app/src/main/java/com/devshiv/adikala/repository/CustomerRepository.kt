package com.devshiv.adikala.repository

import android.util.Log
import com.google.gson.JsonObject
import com.devshiv.adikala.api.CustomerQueries
import com.devshiv.adikala.api.RetrofitClient
import com.devshiv.adikala.model.CustomerAddress
import com.devshiv.adikala.model.CustomerProfile
import com.devshiv.adikala.model.GraphQLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomerRepository {

    private val api = RetrofitClient.accountApi

    /**
     * @param accessToken RAW access token — do NOT prefix with "Bearer ".
     * Shopify's Customer Account API requires the Authorization header to be
     * the token value directly (confirmed in Shopify's own docs/examples).
     * A "Bearer " prefix will cause every request to fail authentication.
     */
    suspend fun getProfile(accessToken: String): CustomerProfile? = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(query = CustomerQueries.GET_PROFILE)
            val response = api.executeCustomerGraphQL(accessToken, request)

            if (response.isSuccessful) {
                val data: JsonObject = response.body()?.data ?: return@withContext null

                if (!data.has("customer") || data.get("customer").isJsonNull) {
                    return@withContext null
                }

                val customerObj: JsonObject = data.getAsJsonObject("customer")

                val emailObj: JsonObject? = if (customerObj.has("emailAddress") && !customerObj.get("emailAddress").isJsonNull) {
                    customerObj.getAsJsonObject("emailAddress")
                } else null

                val phoneObj: JsonObject? = if (customerObj.has("phoneNumber") && !customerObj.get("phoneNumber").isJsonNull) {
                    customerObj.getAsJsonObject("phoneNumber")
                } else null

                val profile = CustomerProfile(
                    id = customerObj.get("id")?.asString ?: "",
                    firstName = customerObj.get("firstName")?.asString,
                    lastName = customerObj.get("lastName")?.asString,
                    email = emailObj?.get("emailAddress")?.asString,
                    phone = phoneObj?.get("phoneNumber")?.asString
                )

                if (customerObj.has("defaultAddress") && !customerObj.get("defaultAddress").isJsonNull) {
                    val addressObj = customerObj.getAsJsonObject("defaultAddress")
                    profile.defaultAddress = CustomerAddress(
                        address1 = addressObj.get("address1")?.asString ?: "",
                        address2 = addressObj.get("address2")?.asString ?: "",
                        city = addressObj.get("city")?.asString ?: "",
                        country = addressObj.get("country")?.asString ?: "",
                        zip = addressObj.get("zip")?.asString ?: "",
                        phone = addressObj.get("phone")?.asString ?: ""
                    )
                }

                Log.d("CustomerRepo", "Profile loaded: ${profile.email}")
                profile
            } else if (response.code() == 401) {
                // Access token expired/invalid — caller should refresh via
                // ShopifyAuthService.refreshAccessToken() and retry once.
                Log.w("CustomerRepo", "401 Unauthorized — token likely expired")
                null
            } else {
                Log.e("CustomerRepo", "Failed: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error: ${e.message}", e)
            null
        }
    }
}