package com.devshiv.adikala.auth

/**
 * All Shopify Customer Account API OAuth constants for Adikala.in in one place.
 * Values below are confirmed from the shop's Headless > Customer Account API admin screen.
 */
object ShopifyAuthConfig {

    // From: Headless > Customer Account API > Customer Account API credentials
    const val CLIENT_ID = "d830ba19-d8f5-4b46-8ebc-a86514207c82"

    // Must exactly match a registered Callback URI. Public mobile clients are
    // REQUIRED by Shopify to use the "shop.{shop_id}.*" scheme — a custom
    // package-based scheme (e.g. com.devshiv.adikala://...) will be rejected.
    const val REDIRECT_URI = "shop.26970751043.app://callback"

    // From: Headless > Customer Account API > Application endpoints
    const val AUTH_ENDPOINT = "https://account.adikala.in/authentication/oauth/authorize"
    const val TOKEN_ENDPOINT = "https://account.adikala.in/authentication/oauth/token"
    const val LOGOUT_ENDPOINT = "https://account.adikala.in/authentication/logout"

    // Required exactly as-is. Shopify rejects the request without
    // "customer-account-api:full" even if the token looks otherwise valid.
    const val SCOPE = "openid email customer-account-api:full"

    // Shop ID, also embedded in the redirect scheme above.
    const val SHOP_ID = "26970751043"

    // Customer Account API is versioned quarterly (YYYY-MM). Check
    // https://shopify.dev/docs/api/customer for the current supported version
    // before shipping, and again periodically — old versions get sunset.
    const val CUSTOMER_API_VERSION = "2025-10"

    // The single GraphQL endpoint ALL Customer Account API queries go to.
    // Note: this is shopify.com, NOT account.adikala.in — that domain is
    // OAuth/login pages only, not the data API.
    const val CUSTOMER_GRAPHQL_BASE_URL =
        "https://shopify.com/$SHOP_ID/account/customer/api/$CUSTOMER_API_VERSION/"

    // Storefront API (catalog, cart, checkout) — separate from the above.
    const val STOREFRONT_GRAPHQL_URL = "https://www.adikala.in/api/2026-01/graphql.json"
    const val STOREFRONT_PUBLIC_TOKEN = "a8931a13b14b1fdc80e95151a87c6655"
    // NEVER embed the private Storefront token (shpat_...) in the app. It must
    // only ever live server-side, if you build a thin backend proxy for it.
}