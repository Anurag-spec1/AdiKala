package com.devshiv.adikala.model

data class Profile(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    val defaultAddress: Address?
)

data class Address(
    val id: String?,
    val firstName: String? = null,
    val lastName: String? = null,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val country: String?,
    val state: String?,
    val zip: String?,
    val phone: String?,
    val isDefault: Boolean = false
)
