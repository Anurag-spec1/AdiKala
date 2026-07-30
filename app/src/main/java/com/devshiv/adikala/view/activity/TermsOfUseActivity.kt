package com.devshiv.adikala.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.devshiv.adikala.databinding.ActivityTermsOfUseBinding

class TermsOfUseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsOfUseBinding
    private lateinit var sections: List<Triple<View, View, View>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsOfUseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        sections = listOf(
            Triple(binding.header1, binding.body1, binding.chevron1),
            Triple(binding.header2, binding.body2, binding.chevron2),
            Triple(binding.header3, binding.body3, binding.chevron3),
            Triple(binding.header4, binding.body4, binding.chevron4),
            Triple(binding.header5, binding.body5, binding.chevron5),
            Triple(binding.header6, binding.body6, binding.chevron6),
            Triple(binding.header7, binding.body7, binding.chevron7),
            Triple(binding.header8, binding.body8, binding.chevron8),
            Triple(binding.header9, binding.body9, binding.chevron9),
            Triple(binding.header10, binding.body10, binding.chevron10),
            Triple(binding.header11, binding.body11, binding.chevron11),
            Triple(binding.header12, binding.body12, binding.chevron12),
            Triple(binding.header13, binding.body13, binding.chevron13),
            Triple(binding.header14, binding.body14, binding.chevron14),
            Triple(binding.header15, binding.body15, binding.chevron15),
            Triple(binding.header16, binding.body16, binding.chevron16),
            Triple(binding.header17, binding.body17, binding.chevron17),
            Triple(binding.header18, binding.body18, binding.chevron18),
            Triple(binding.header19, binding.body19, binding.chevron19)
        )

        sections.forEach { (header, body, chevron) ->
            header.setOnClickListener { toggleSection(body, chevron) }
        }

        binding.tvContactEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@adikala.in")
                putExtra(Intent.EXTRA_SUBJECT, "Terms of Use Query")
            }
            startActivity(intent)
        }
    }

    private fun toggleSection(body: View, chevron: View) {
        TransitionManager.beginDelayedTransition(binding.accordionContainer, AutoTransition())

        val isExpanding = body.visibility != View.VISIBLE
        body.visibility = if (isExpanding) View.VISIBLE else View.GONE
        chevron.animate().rotation(if (isExpanding) 90f else 0f).setDuration(200).start()
    }
}