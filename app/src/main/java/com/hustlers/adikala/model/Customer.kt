package com.hustlers.adikala.model

data class CustomerProfile(
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    var defaultAddress: CustomerAddress? = null
)

data class CustomerAddress(
    val address1: String = "",
    val address2: String = "",
    val city: String = "",
    val country: String = "",
    val zip: String = "",
    val phone: String = ""
)
