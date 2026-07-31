package com.devshiv.adikala.view.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.devshiv.adikala.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding
    private lateinit var sections: List<Triple<View, View, View>>
    private var currentAnimator: ValueAnimator? = null
    private var expandedSectionBody: View? = null

    companion object {
        private const val TAG = "PrivacyPolicy"
    }

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

        sections.forEachIndexed { _, (header, body, chevron) ->
            header.setOnClickListener { toggleSection(body, chevron) }
        }

        binding.tvContactEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@adikala.in")
                putExtra(Intent.EXTRA_SUBJECT, "Privacy Policy Query")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun toggleSection(body: View, chevron: View) {
        currentAnimator?.cancel()

        if (body.isVisible) {
            collapseSection(body, chevron)
            expandedSectionBody = null
            return
        }

        expandedSectionBody?.let { previous ->
            if (previous != body) {
                val index = sections.indexOfFirst { it.second == previous }
                if (index >= 0) {
                    val (_, oldBody, oldChevron) = sections[index]
                    collapseSection(oldBody, oldChevron)
                }
            }
        }

        expandSection(body, chevron)
        expandedSectionBody = body
    }

    private fun expandSection(body: View, chevron: View) {
        body.visibility = View.VISIBLE
        body.alpha = 1f

        body.post {
            val parentWidth = (body.parent as? View)?.width ?: body.width
            if (parentWidth <= 0) return@post

            val widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)

            body.measure(widthSpec, heightSpec)
            val targetHeight = body.measuredHeight

            Log.d(TAG, "expandSection: targetHeight=$targetHeight width=$parentWidth")

            body.layoutParams.height = 0
            body.requestLayout()

            currentAnimator = ValueAnimator.ofInt(0, targetHeight).apply {
                duration = 280
                addUpdateListener { animator ->
                    val value = animator.animatedValue as Int
                    body.layoutParams.height = value
                    body.requestLayout()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        body.layoutParams.height = targetHeight
                        body.requestLayout()
                    }
                })
                start()
            }
        }

        chevron.animate()
            .rotation(90f)
            .setDuration(280)
            .start()
    }

    private fun collapseSection(body: View, chevron: View) {
        val startHeight = if (body.height > 0) body.height else body.measuredHeight
        if (startHeight <= 0) {
            body.visibility = View.GONE
            return
        }

        currentAnimator = ValueAnimator.ofInt(startHeight, 0).apply {
            duration = 280
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                body.layoutParams.height = value
                body.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    body.visibility = View.GONE
                    body.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    body.requestLayout()
                }
            })
            start()
        }

        chevron.animate()
            .rotation(0f)
            .setDuration(280)
            .start()
    }

    override fun onDestroy() {
        currentAnimator?.cancel()
        super.onDestroy()
    }
}