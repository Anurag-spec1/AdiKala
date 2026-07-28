package com.devshiv.adikala.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import kotlin.coroutines.resume

class ShopifyAuthService(context: Context) {

    private val appContext = context.applicationContext
    private val authManager = ShopifyAuthManager(appContext)

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(ShopifyAuthConfig.AUTH_ENDPOINT),
        Uri.parse(ShopifyAuthConfig.TOKEN_ENDPOINT),
        null,
        Uri.parse(ShopifyAuthConfig.LOGOUT_ENDPOINT)
    )

    private val authService = AuthorizationService(appContext)
    private val httpClient = OkHttpClient()

    companion object {
        private const val TAG = "ShopifyAuth"
        private const val REFRESH_BUFFER_MS = 5 * 60 * 1000L
    }

    fun buildAuthIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            ShopifyAuthConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(ShopifyAuthConfig.REDIRECT_URI)
        )
            .setScope(ShopifyAuthConfig.SCOPE)
            .setPromptValues("login")
            .build()

        val params = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(android.graphics.Color.parseColor("#0D3238"))
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(params)
            .setShowTitle(true)
            .setInstantAppsEnabled(false)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setUrlBarHidingEnabled(true)
            .build()

        return authService.getAuthorizationRequestIntent(request, customTabsIntent)
    }

    suspend fun handleAuthResponse(data: Intent): Boolean {
        val authResponse = AuthorizationResponse.fromIntent(data)
        val authException = AuthorizationException.fromIntent(data)

        if (authException != null || authResponse == null) {
            Log.e(TAG, "Auth failed: ${authException?.message}")
            return false
        }

        val tokenRequest = authResponse.createTokenExchangeRequest()
        val tokenResponse = performTokenRequest(tokenRequest) ?: return false
        saveTokens(tokenResponse)
        return true
    }

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = authManager.getRefreshToken() ?: return false

        return try {
            val tokenRequest = TokenRequest.Builder(serviceConfig, ShopifyAuthConfig.CLIENT_ID)
                .setGrantType("refresh_token")
                .setRefreshToken(refreshToken)
                .build()

            val tokenResponse = performTokenRequest(tokenRequest) ?: return false
            saveTokens(tokenResponse)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh failed", e)
            false
        }
    }

    suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "signOut() called")

        val idToken = authManager.getIdToken()
        var serverLogoutSuccess = false

        if (!idToken.isNullOrEmpty()) {
            try {
                val encodedIdToken = URLEncoder.encode(idToken, "UTF-8")
                val url = "${ShopifyAuthConfig.LOGOUT_ENDPOINT}?" +
                        "id_token_hint=$encodedIdToken&" +
                        "client_id=${ShopifyAuthConfig.CLIENT_ID}"

                val request = Request.Builder().url(url).get().build()
                httpClient.newCall(request).execute().use { response ->
                    serverLogoutSuccess = response.isSuccessful
                    Log.d(TAG, "Server logout response: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server logout failed", e)
            }
        }

        authManager.clear()
        serverLogoutSuccess
    }

    fun isLoggedIn(): Boolean = !authManager.getRefreshToken().isNullOrEmpty()
    fun getAccessToken(): String? = authManager.getAccessToken()

    fun dispose() {
        authService.dispose()
    }

    private suspend fun performTokenRequest(request: TokenRequest): TokenResponse? =
        suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request) { response, exception ->
                if (!cont.isActive) return@performTokenRequest
                cont.resume(if (exception != null || response == null) null else response)
            }
        }

    private fun saveTokens(tokenResponse: TokenResponse) {
        val accessToken = tokenResponse.accessToken ?: return
        val expiresAt = tokenResponse.accessTokenExpirationTime
            ?: (System.currentTimeMillis() + 55 * 60 * 1000)

        authManager.saveTokens(
            access = accessToken,
            refresh = tokenResponse.refreshToken ?: authManager.getRefreshToken(),
            idToken = tokenResponse.idToken ?: authManager.getIdToken(),
            expiresAt = expiresAt
        )
    }

    suspend fun getValidAccessToken(): String? {
        if (shouldRefreshAccessToken()) {
            Log.d(TAG, "Access token near expiry, refreshing...")
            val refreshed = refreshAccessToken()
            if (!refreshed) {
                Log.e(TAG, "Token refresh failed")
                return null
            }
        }
        return authManager.getAccessToken()
    }

    fun shouldRefreshAccessToken(): Boolean {
        val expiresAt = authManager.getExpiresAt()
        if (expiresAt == 0L) return true
        return System.currentTimeMillis() >= (expiresAt - REFRESH_BUFFER_MS)
    }
}