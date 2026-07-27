package com.devshiv.adikala.auth

object ShopifyAuthConfig {
    const val CLIENT_ID = "d830ba19-d8f5-4b46-8ebc-a86514207c82"
    const val REDIRECT_URI = "shop.26970751043.app://callback"
    const val AUTH_ENDPOINT = "https://account.adikala.in/authentication/oauth/authorize"
    const val TOKEN_ENDPOINT = "https://account.adikala.in/authentication/oauth/token"
    const val LOGOUT_ENDPOINT = "https://account.adikala.in/authentication/logout"
    const val SCOPE = "openid email customer-account-api:full"
    const val SHOP_ID = "26970751043"
    const val CUSTOMER_API_VERSION = "2025-10"
    const val CUSTOMER_GRAPHQL_BASE_URL = "https://shopify.com/$SHOP_ID/account/customer/api/$CUSTOMER_API_VERSION/"
}