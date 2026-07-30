package com.devshiv.adikala.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devshiv.adikala.databinding.ActivityShippingPolicyBinding

class ShippingPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShippingPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@adikala.in")
                putExtra(Intent.EXTRA_SUBJECT, "Shipping Query")
            }
            startActivity(intent)
        }

        binding.btnWhatsappSupport.setOnClickListener {
            val phoneNumber = "918130507350"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber")
            }
            startActivity(intent)
        }
    }
}