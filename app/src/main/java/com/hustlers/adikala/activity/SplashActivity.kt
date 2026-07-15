package com.hustlers.adikala.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.hustlers.adikala.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var sweepAnimator: ValueAnimator? = null

    private val totalDuration = 2600L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post { runSequence() }
    }

    private fun runSequence() {
        animateRings()
        animateMark()
        animateBrand()
        animateAccentLine()
        animateProgress()
        scheduleExit()
    }

    private fun animateRings() {
        binding.ringOuter.apply {
            scaleX = 0.55f
            scaleY = 0.55f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(120)
                .setDuration(900)
                .setInterpolator(DecelerateInterpolator(1.6f))
                .start()
        }
        binding.ringInner.apply {
            scaleX = 0.55f
            scaleY = 0.55f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(220)
                .setDuration(900)
                .setInterpolator(DecelerateInterpolator(1.6f))
                .start()
        }
    }

    private fun animateMark() {
        binding.logoMark.apply {
            scaleX = 0.4f
            scaleY = 0.4f
            rotation = -14f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f).rotation(0f)
                .setStartDelay(180)
                .setDuration(760)
                .setInterpolator(OvershootInterpolator(1.5f))
                .withEndAction { breathe() }
                .start()
        }
    }

    private fun breathe() {
        binding.logoMark.animate()
            .scaleX(1.045f).scaleY(1.045f)
            .setDuration(900)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.logoMark.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(900)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun animateBrand() {
        binding.tvBrand.apply {
            translationY = 26f
            animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(620)
                .setDuration(620)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
        binding.tvTagline.apply {
            translationY = 18f
            animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(780)
                .setDuration(620)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
        binding.tvFooter.animate()
            .alpha(1f)
            .setStartDelay(1100)
            .setDuration(700)
            .start()
    }

    private fun animateAccentLine() {
        val line = binding.accentLine
        val target = (140 * resources.displayMetrics.density).toInt()

        ValueAnimator.ofInt(0, target).apply {
            startDelay = 900
            duration = 700
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { va ->
                val lp = line.layoutParams
                lp.width = va.animatedValue as Int
                line.layoutParams = lp
            }
            start()
        }
    }

    private fun animateProgress() {
        binding.progressTrack.animate()
            .alpha(1f)
            .setStartDelay(1150)
            .setDuration(400)
            .start()

        val trackWidth = (120 * resources.displayMetrics.density)
        val fillWidth = (40 * resources.displayMetrics.density)

        sweepAnimator = ValueAnimator.ofFloat(0f, trackWidth - fillWidth).apply {
            startDelay = 1200
            duration = 700
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                binding.progressFill.translationX = va.animatedValue as Float
            }
            start()
        }
    }

    private fun scheduleExit() {
        binding.root.postDelayed({ exitToNext() }, totalDuration)
    }

    private fun exitToNext() {
        if (isFinishing) return

        binding.centerBlock.animate()
            .alpha(0f).scaleX(1.08f).scaleY(1.08f)
            .setDuration(420)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.progressTrack.animate().alpha(0f).setDuration(280).start()
        binding.tvFooter.animate().alpha(0f).setDuration(280).start()

        binding.root.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 380)
    }

    override fun onDestroy() {
        sweepAnimator?.cancel()
        sweepAnimator = null
        super.onDestroy()
    }
}