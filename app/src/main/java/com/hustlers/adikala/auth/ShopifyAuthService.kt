package com.hustlers.adikala.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.NoClientAuthentication
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ShopifyAuthService(context: Context) {

    private val appContext = context.applicationContext
    private val authService = AuthorizationService(appContext)
    private val authManager = ShopifyAuthManager(appContext)

    companion object {
        private const val TAG = "ShopifyAuth"

        const val SHOP_ID = "26970751043"
        private val ISSUER = Uri.parse("https://shopify.com/authentication/$SHOP_ID")

        private const val CLIENT_ID = "YOUR_PUBLIC_CLIENT_ID"
        private val REDIRECT_URI = Uri.parse("shop.$SHOP_ID.app://callback")

        private val SCOPES = listOf("openid", "email", "customer-account-api:full")
        private const val REFRESH_SKEW_MS = 60_000L
    }

    private suspend fun config(): AuthorizationServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(ISSUER) { cfg, ex ->
                if (cfg != null) cont.resume(cfg)
                else cont.resumeWithException(ex ?: RuntimeException("OIDC discovery failed"))
            }
        }

    suspend fun buildAuthIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            config(), CLIENT_ID, ResponseTypeValues.CODE, REDIRECT_URI
        ).setScopes(SCOPES).build()
        return authService.getAuthorizationRequestIntent(request)
    }

    suspend fun handleAuthResponse(data: Intent): Boolean {
        val resp = AuthorizationResponse.fromIntent(data)
        val ex = AuthorizationException.fromIntent(data)
        if (resp == null) {
            Log.e(TAG, "Authorization failed", ex)
            return false
        }
        return try {
            val tokens = performTokenRequest(resp.createTokenExchangeRequest())
            persist(tokens)
        } catch (e: Exception) {
            Log.e(TAG, "Token exchange failed", e)
            false
        }
    }

    suspend fun getValidAccessToken(): String? {
        val access = authManager.getAccessToken()
        val expiresAt = authManager.getExpiresAt()
        if (access != null && System.currentTimeMillis() < expiresAt - REFRESH_SKEW_MS) return access

        val refresh = authManager.getRefreshToken() ?: return null
        return try {
            val request = TokenRequest.Builder(config(), CLIENT_ID)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refresh)
                .setScopes(SCOPES)
                .build()
            val tokens = performTokenRequest(request)
            if (persist(tokens)) tokens.accessToken else null
        } catch (e: Exception) {
            Log.e(TAG, "Refresh failed — session expired", e)
            null
        }
    }

    private suspend fun performTokenRequest(request: TokenRequest): TokenResponse =
        suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request, NoClientAuthentication.INSTANCE) { resp, ex ->
                if (resp != null) cont.resume(resp)
                else cont.resumeWithException(ex ?: RuntimeException("token request returned null"))
            }
        }

    private fun persist(t: TokenResponse): Boolean {
        val access = t.accessToken ?: return false
        authManager.saveTokens(
            access = access,
            refresh = t.refreshToken,
            idToken = t.idToken,
            expiresAt = t.accessTokenExpirationTime ?: 0L
        )
        return true
    }

    fun dispose() = authService.dispose()
}