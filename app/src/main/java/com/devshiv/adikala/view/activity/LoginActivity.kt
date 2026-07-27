package com.devshiv.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authService: ShopifyAuthService

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                handleAuthSuccess(result.data!!)
            } else {
                Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
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
        try {
            val intent = authService.buildAuthIntent()
            authLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not start sign-in", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun handleAuthSuccess(data: Intent) {
        lifecycleScope.launch {
            val success = authService.handleAuthResponse(data)
            if (success) {
                Toast.makeText(this@LoginActivity, "Signed in!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
            } else {
                Toast.makeText(this@LoginActivity, "Sign-in failed", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}