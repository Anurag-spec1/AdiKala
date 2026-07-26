package com.devshiv.adikala.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume

/**
 * Handles the full Shopify Customer Account OAuth 2.0 + PKCE flow for a
 * public mobile client, using AppAuth-Android (net.openid:appauth).
 *
 * Flow:
 *  1. buildAuthIntent()      -> Intent that opens a Custom Tab at Shopify's
 *                               hosted login page (email/OTP, Google, Facebook,
 *                               Shop — all handled by Shopify, not this app).
 *  2. handleAuthResponse()   -> called with the Intent AppAuth's redirect
 *                               activity received; exchanges the auth code
 *                               for tokens and saves them.
 *  3. logout() / dispose()   -> session teardown.
 *
 * AppAuth generates and verifies the PKCE code_verifier/code_challenge
 * automatically — nothing to do manually for that part.
 */
class ShopifyAuthService(context: Context) {

    private val appContext = context.applicationContext
    private val authManager = ShopifyAuthManager(appContext)

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(ShopifyAuthConfig.AUTH_ENDPOINT),
        Uri.parse(ShopifyAuthConfig.TOKEN_ENDPOINT),
        /* registrationEndpoint = */ null,
        Uri.parse(ShopifyAuthConfig.LOGOUT_ENDPOINT)
    )

    private val authService = AuthorizationService(appContext)

    /** Builds the Intent to launch (via registerForActivityResult) to start login. */
    fun buildAuthIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            ShopifyAuthConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(ShopifyAuthConfig.REDIRECT_URI)
        )
            .setScope(ShopifyAuthConfig.SCOPE)
            .build() // PKCE code_verifier/code_challenge(S256) auto-generated here

        return authService.getAuthorizationRequestIntent(request)
    }

    /**
     * Call with the result Intent from the auth Custom Tab redirect.
     * Returns true on success (tokens saved), false on failure/cancellation.
     */
    suspend fun handleAuthResponse(data: Intent): Boolean {
        val authResponse = AuthorizationResponse.fromIntent(data)
        val authException = AuthorizationException.fromIntent(data)

        if (authException != null || authResponse == null) {
            return false // user cancelled, or Shopify returned an error
        }

        val tokenRequest = authResponse.createTokenExchangeRequest()
        val tokenResponse = performTokenRequestSuspend(tokenRequest) ?: return false

        saveTokens(tokenResponse)
        return true
    }

    /** Refreshes the access token using the stored refresh token. Call when a
     *  Customer Account API request comes back 401, or proactively before expiry. */
    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = authManager.getRefreshToken() ?: return false

        val tokenRequest = TokenRequest.Builder(
            serviceConfig,
            ShopifyAuthConfig.CLIENT_ID
        )
            .setGrantType("refresh_token")
            .setRefreshToken(refreshToken)
            .build()

        val tokenResponse = performTokenRequestSuspend(tokenRequest) ?: return false
        saveTokens(tokenResponse)
        return true
    }

    private suspend fun performTokenRequestSuspend(request: TokenRequest): TokenResponse? =
        suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request) { response, exception ->
                if (exception != null || response == null) {
                    cont.resume(null)
                } else {
                    cont.resume(response)
                }
            }
        }

    private fun saveTokens(tokenResponse: TokenResponse) {
        val accessToken = tokenResponse.accessToken ?: return
        val refreshToken = tokenResponse.refreshToken
        val idToken = tokenResponse.idToken
        val expiresAt = tokenResponse.accessTokenExpirationTime
            ?: (System.currentTimeMillis() + 55 * 60 * 1000) // fallback ~55 min

        authManager.saveTokens(
            access = accessToken,
            refresh = refreshToken,
            idToken = idToken,
            expiresAt = expiresAt
        )
    }

    /** True if the current access token is expired or about to expire. */
    fun isAccessTokenExpired(): Boolean {
        val expiresAt = authManager.getExpiresAt()
        if (expiresAt == 0L) return true
        val bufferMillis = 60_000L // refresh 1 min early
        return System.currentTimeMillis() >= (expiresAt - bufferMillis)
    }

    /**
     * Ends the session. For mobile public clients, Shopify's logout endpoint
     * returns a plain success response — there is no redirect to handle.
     */
    suspend fun logout(): Boolean {
        authManager.clear()
        return true
    }

    fun dispose() {
        authService.dispose()
    }
}