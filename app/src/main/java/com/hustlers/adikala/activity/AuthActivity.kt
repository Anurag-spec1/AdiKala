package com.hustlers.adikala.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hustlers.adikala.R
import com.hustlers.adikala.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private var newsChecked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateCheckbox()
    }

    private fun setupListeners() {

        binding.btnPrimary.setOnClickListener {
            Toast.makeText(this, "Continue with Adikala", Toast.LENGTH_SHORT).show()
        }
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google sign-in", Toast.LENGTH_SHORT).show()
        }
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook sign-in", Toast.LENGTH_SHORT).show()
        }

        binding.btnEmailSubmit.setOnClickListener { submitEmail() }
        binding.etEmail.setOnEditorActionListener { _, _, _ -> submitEmail(); true }

        binding.checkRow.setOnClickListener {
            newsChecked = !newsChecked
            updateCheckbox()
        }
    }

    private fun submitEmail() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun updateCheckbox() {
        if (newsChecked) {
            binding.ivCheck.visibility = View.VISIBLE
            binding.ivCheck.setBackgroundColor(Color.parseColor("#0D3238"))
        } else {
            binding.ivCheck.setImageResource(0)
            binding.ivCheck.setBackgroundColor(Color.parseColor("#00000000"))
            binding.ivCheck.background =
                resources.getDrawable(R.drawable.bg_check_empty, theme)
        }
    }
}