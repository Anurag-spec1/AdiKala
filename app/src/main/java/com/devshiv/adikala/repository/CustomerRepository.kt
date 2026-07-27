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

private const val TAG = "AdikalaAuth"

class CustomerRepository {

    private val api = RetrofitClient.accountApi

    suspend fun getProfile(accessToken: String): CustomerProfile? = withContext(Dispatchers.IO) {
        Log.d(TAG, "getProfile() called, accessToken present=${accessToken.isNotEmpty()}, length=${accessToken.length}")
        try {
            val request = GraphQLRequest(query = CustomerQueries.GET_PROFILE)
            val response = api.executeCustomerGraphQL(accessToken, request)

            Log.d(TAG, "getProfile() HTTP response code=${response.code()}")

            if (response.isSuccessful) {
                val data: JsonObject = response.body()?.data ?: run {
                    Log.e(TAG, "getProfile() response body/data is null despite 2xx")
                    return@withContext null
                }

                if (!data.has("customer") || data.get("customer").isJsonNull) {
                    Log.w(TAG, "getProfile() response has no 'customer' field — check GraphQL query/scopes")
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
                        phone = addressObj.get("phoneNumber")?.asString ?: ""
                    )
                }

                Log.d(TAG, "getProfile() SUCCESS: email=${profile.email}")
                profile
            } else if (response.code() == 401) {
                Log.w(TAG, "getProfile() 401 Unauthorized — access token expired/invalid, caller should refresh and retry")
                null
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "getProfile() failed: code=${response.code()}, body=$errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProfile() exception: ${e.message}", e)
            null
        }
    }
}