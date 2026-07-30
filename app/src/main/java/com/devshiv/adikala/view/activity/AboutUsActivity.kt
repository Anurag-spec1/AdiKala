package com.devshiv.adikala.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devshiv.adikala.databinding.ActivityAboutUsBinding

class AboutUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnExploreCollection.setOnClickListener {
            finish()
        }
    }
}