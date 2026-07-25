package com.devshiv.adikala.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

/**
 * Starts the Shopify Customer Account OAuth flow. Shopify hosts the actual
 * email/OTP/passkey step in a Custom Tab; we just launch it and catch the redirect.
 *
 * The old WebView + token-scraping approach is gone — the shcat_ token is never
 * exposed to JS/cookies, so it could never be read that way.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authService: ShopifyAuthService

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (data == null) {
                Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                finish()
                return@registerForActivityResult
            }
            lifecycleScope.launch {
                binding.progressBar?.visibility = android.view.View.VISIBLE
                val ok = authService.handleAuthResponse(data)
                if (ok) {
                    Toast.makeText(this@LoginActivity, "Signed in", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                } else {
                    Toast.makeText(this@LoginActivity, "Sign-in failed. Please try again.", Toast.LENGTH_LONG).show()
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authService = ShopifyAuthService(this)

        startAuthFlow()
    }

    private fun startAuthFlow() {
        lifecycleScope.launch {
            try {
                val intent = authService.buildAuthIntent()
                authLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Couldn't start sign-in: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        authService.dispose()
        super.onDestroy()
    }
}