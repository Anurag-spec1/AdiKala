package com.hustlers.adikala.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustlers.adikala.auth.ShopifyAuthManager
import com.hustlers.adikala.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: ShopifyAuthManager
    private val accountUrl = "https://account.adikala.in"
    private val clientId = "b620839c-8ee3-4762-a810-b70779a89ce7"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = ShopifyAuthManager(this)

        setupWebView()
        setupListeners()
    }

    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.allowContentAccess = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("LoginActivity", "Page loaded: $url")

                if (url?.contains("oauth/token") == true) {
                    extractTokenFromPage()
                }

                if (url?.contains("profile") == true || url?.contains("orders") == true) {
                    fetchTokenAndFinish()
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                request?.url?.toString()?.let { url ->
                    if (url.contains("oauth/token")) {
                        fetchTokenInBackground(url)
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinue.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter a valid email"
                return@setOnClickListener
            }
            startLogin(email)
        }
    }

    private fun startLogin(email: String) {
        showLoading()
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    sendLoginRequest(email)
                }
                if (result != null) {
                    val loginUrl = "$accountUrl$result"
                    binding.webView.loadUrl(loginUrl)
                } else {
                    Toast.makeText(this@LoginActivity, "Failed to send login request", Toast.LENGTH_SHORT).show()
                    hideLoading()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login error", e)
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                hideLoading()
            }
        }
    }

    private fun sendLoginRequest(email: String): String? {
        return try {
            val url = URL("$accountUrl/authentication/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonBody = JSONObject().apply {
                put("email", email)
                put("client_id", clientId)
                put("redirect_uri", "/authentication/oauth/authorize?client_id=$clientId")
            }

            connection.outputStream.write(jsonBody.toString().toByteArray())

            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)
            jsonResponse.optJSONObject("data")?.optString("redirect_uri")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Send login error", e)
            null
        }
    }

    private fun extractTokenFromPage() {
        binding.webView.evaluateJavascript("""
            (function() {
                try {
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', '/oauth/token', false);
                    xhr.send();
                    var response = JSON.parse(xhr.responseText);
                    return response.access_token || '';
                } catch(e) {
                    return '';
                }
            })()
        """.trimIndent()) { result ->
            val token = result.replace("\"", "").trim()
            if (token.isNotEmpty() && token != "null") {
                onTokenReceived(token)
            }
        }
    }

    private fun fetchTokenAndFinish() {
        lifecycleScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    val url = URL("$accountUrl/oauth/token")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().readText()
                    JSONObject(response).optString("access_token")
                }
                if (token.isNotEmpty()) {
                    onTokenReceived(token)
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Token fetch error", e)
            }
        }
    }

    private fun fetchTokenInBackground(urlString: String) {
        lifecycleScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().readText()
                    JSONObject(response).optString("access_token")
                }
                if (token.isNotEmpty()) {
                    onTokenReceived(token)
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Token fetch error", e)
            }
        }
    }

    private fun onTokenReceived(token: String) {
        Log.d("LoginActivity", "Token received: ${token.take(20)}...")
        authManager.saveLoginData(token, null, null, binding.etEmail.text.toString().trim(), null)
        binding.webView.visibility = View.GONE
        hideLoading()
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }
}