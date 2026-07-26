package com.devshiv.adikala.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devshiv.adikala.databinding.ActivityAuthBinding

/**
 * Entry screen shown to logged-out users. Both buttons lead to the SAME
 * Shopify-hosted login page (LoginActivity) — Shopify's own screen already
 * presents email/OTP, "Continue with Shop", Google, and Facebook options,
 * so there is no separate in-app email/password step to build here.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Sign-in was not completed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnPrimary.setOnClickListener {
            launchLogin()
        }

        binding.btnEmailSubmit.setOnClickListener {
            // Shopify's hosted login screen collects the email itself —
            // this button just starts the same flow. Any text the user typed
            // in etEmail here isn't sent anywhere; consider hiding that field
            // entirely, or repurposing it as a "Continue" trigger only.
            launchLogin()
        }
    }

    private fun launchLogin() {
        loginLauncher.launch(Intent(this, LoginActivity::class.java))
    }
}