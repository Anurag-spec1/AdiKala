package com.devshiv.adikala.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devshiv.adikala.databinding.ActivityRefundsReturnsBinding

class RefundsReturnsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRefundsReturnsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRefundsReturnsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@adikala.in")
                putExtra(Intent.EXTRA_SUBJECT, "Order Issue - Refund/Replacement Request")
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