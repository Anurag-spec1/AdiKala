package com.devshiv.adikala.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.devshiv.adikala.R
import com.devshiv.adikala.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    private lateinit var sections: List<Triple<View, View, View>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        sections = listOf(
            Triple(binding.headerSection1, binding.bodySection1, binding.chevronSection1),
            Triple(binding.headerSection2, binding.bodySection2, binding.chevronSection2),
            Triple(binding.headerSection3, binding.bodySection3, binding.chevronSection3),
            Triple(binding.headerSection4, binding.bodySection4, binding.chevronSection4),
            Triple(binding.headerSection5, binding.bodySection5, binding.chevronSection5),
            Triple(binding.headerSection6, binding.bodySection6, binding.chevronSection6),
            Triple(binding.headerSection7, binding.bodySection7, binding.chevronSection7),
            Triple(binding.headerSection8, binding.bodySection8, binding.chevronSection8),
            Triple(binding.headerSection9, binding.bodySection9, binding.chevronSection9)
        )

        sections.forEach { (header, body, chevron) ->
            header.setOnClickListener { toggleSection(body, chevron) }
        }

        binding.tvContactEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@adikala.in")
                putExtra(Intent.EXTRA_SUBJECT, "Privacy Policy Query")
            }
            startActivity(intent)
        }
    }

    private fun toggleSection(body: View, chevron: View) {
        TransitionManager.beginDelayedTransition(binding.root as android.view.ViewGroup, AutoTransition())

        val isExpanding = body.visibility != View.VISIBLE
        body.visibility = if (isExpanding) View.VISIBLE else View.GONE
        chevron.animate().rotation(if (isExpanding) 90f else 0f).setDuration(200).start()
    }
}