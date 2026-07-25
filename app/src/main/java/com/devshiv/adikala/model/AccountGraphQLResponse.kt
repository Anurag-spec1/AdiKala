package com.devshiv.adikala.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class AccountGraphQLResponse(
    @SerializedName("data")
    val data: JsonObject? = null,

    @SerializedName("errors")
    val errors: List<AccountGraphQLError>? = null
)

data class AccountGraphQLError(
    @SerializedName("message")
    val message: String? = null
)