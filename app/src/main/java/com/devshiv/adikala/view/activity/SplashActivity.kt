package com.devshiv.adikala.view.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Choreographer
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devshiv.adikala.R
import com.devshiv.adikala.view.adapter.SplashImageAdapter
import com.devshiv.adikala.databinding.ActivitySplashBinding
import kotlin.math.PI
import kotlin.math.sin

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val leftImages = listOf(
        R.drawable.left_1, R.drawable.left_2, R.drawable.left_3, R.drawable.left_4
    )
    private val centerImages = listOf(
        R.drawable.center_2,  R.drawable.center_1,R.drawable.center_3, R.drawable.center_4
    )
    private val rightImages = listOf(
        R.drawable.right_1, R.drawable.right_2, R.drawable.right_3, R.drawable.right_4
    )

    private val columns = mutableListOf<ColumnScroller>()
    private var choreographer: Choreographer? = null
    private var frameCallback: Choreographer.FrameCallback? = null
    private var sweepAnimator: ValueAnimator? = null

    private var lastFrameNanos = 0L
    private var elapsed = 0f
    private var running = false

    private val visibleDuration = 5000L
    private var offlineDialog: Dialog? = null

    private class ColumnScroller(
        val rv: RecyclerView,
        val speedDp: Float,
        val direction: Int,
        val delay: Float
    ) {
        private var acc = 0f

        fun tick(dt: Float, elapsed: Float, density: Float) {
            var factor = 1f
            if (elapsed < delay) {
                factor = sin((elapsed / delay) * (PI / 2)).toFloat()
                if (factor < 0.01f) return
            }
            acc += speedDp * density * dt * factor * direction
            val whole = acc.toInt()
            if (whole != 0) {
                rv.scrollBy(0, whole)
                acc -= whole
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        if (!isNetworkAvailable()) {
            showOfflineDialog()
            return
        }

        binding.root.post { setupWall() }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showOfflineDialog() {
        offlineDialog = Dialog(this)
        offlineDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_offline)
            setCancelable(false)

            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }

            val btnOpenSettings = findViewById<Button>(R.id.btnOpenSettings)
            val btnTryAgain = findViewById<Button>(R.id.btnTryAgain)

            btnOpenSettings.setOnClickListener {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }

            btnTryAgain.setOnClickListener {
                if (isNetworkAvailable()) {
                    dismiss()
                    binding.root.post { setupWall() }
                } else {
                    btnTryAgain.text = "Still no connection... Retry?"
                }
            }

            show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (offlineDialog?.isShowing == true && isNetworkAvailable()) {
            offlineDialog?.dismiss()
            offlineDialog = null
            binding.root.post { setupWall() }
        }

        if (columns.isNotEmpty() && !running) startTicker()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupWall() {
        val density = resources.displayMetrics.density
        val w = binding.root.width.toFloat()
        val h = binding.root.height.toFloat()
        if (w == 0f || h == 0f) return

        val gap = 16f * density
        val cardGap = (18f * density).toInt()
        val hiddenRatio = 0.35f

        val available = w - gap * 2
        val sideFull = (available * 0.25f) / (1 - hiddenRatio)
        val sideVisible = sideFull * (1 - hiddenRatio)
        val centerWidth = w - 2 * (sideVisible + gap)

        val sideH = (sideFull * 1.60f).toInt()
        val centerH = (centerWidth * 1.50f).toInt()

        val extraTop = (h * 0.05f).toInt()
        val extraBottom = (h * 0.10f).toInt()
        val wallHeight = (h + extraTop + extraBottom).toInt()

        Log.d("SplashActivity", "Center images: ${centerImages.map { resources.getResourceEntryName(it) }}")
        Log.d("SplashActivity", "Left images: ${leftImages.map { resources.getResourceEntryName(it) }}")
        Log.d("SplashActivity", "Right images: ${rightImages.map { resources.getResourceEntryName(it) }}")

        applyColumn(
            binding.rvLeft, sideFull.toInt(), wallHeight, extraTop,
            (-(sideFull * hiddenRatio)).toInt(), 0,
            -sideH * 0.45f, leftImages, sideH, cardGap
        )
        applyColumn(
            binding.rvCenter, centerWidth.toInt(), wallHeight, extraTop,
            0, 0,
            -centerH * 0.05f, centerImages, centerH, cardGap
        )
        applyColumn(
            binding.rvRight, sideFull.toInt(), wallHeight, extraTop,
            0, (-(sideFull * hiddenRatio)).toInt(),
            sideH * 0.15f, rightImages, sideH, cardGap
        )

        columns.clear()
        columns.add(ColumnScroller(binding.rvLeft, 30f, 1, 0.01f))
        columns.add(ColumnScroller(binding.rvCenter, 15f, -1, 0.40f))
        columns.add(ColumnScroller(binding.rvRight, 20f, 1, 0.80f))

        startTicker()
        animateContent()
        animateSweep()

        binding.root.postDelayed({ exitToNext() }, visibleDuration)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyColumn(
        rv: RecyclerView,
        width: Int,
        height: Int,
        extraTop: Int,
        startMargin: Int,
        endMargin: Int,
        offsetY: Float,
        images: List<Int>,
        itemHeight: Int,
        gap: Int
    ) {
        val lp = rv.layoutParams as FrameLayout.LayoutParams
        lp.width = width
        lp.height = height
        lp.topMargin = -extraTop
        lp.marginStart = startMargin
        lp.marginEnd = endMargin
        rv.layoutParams = lp
        rv.translationY = offsetY

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = SplashImageAdapter(images, itemHeight, gap)
        rv.setItemViewCacheSize(6)
        rv.setOnTouchListener { _, _ -> true }
        rv.scrollToPosition(Int.MAX_VALUE / 2)
    }

    private fun startTicker() {
        running = true
        val density = resources.displayMetrics.density
        choreographer = Choreographer.getInstance()

        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!running) return
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameTimeNanos
                    choreographer?.postFrameCallback(this)
                    return
                }
                val dt = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = frameTimeNanos
                elapsed += dt

                columns.forEach { it.tick(dt, elapsed, density) }
                choreographer?.postFrameCallback(this)
            }
        }
        choreographer?.postFrameCallback(frameCallback!!)
    }

    private fun animateContent() {
        binding.contentBlock.apply {
            translationY = 60f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(250)
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }

    private fun animateSweep() {
        val density = resources.displayMetrics.density
        val track = 110f * density
        val fill = 38f * density

        sweepAnimator = ValueAnimator.ofFloat(0f, track - fill).apply {
            startDelay = 900
            duration = 750
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                binding.progressFill.translationX = va.animatedValue as Float
            }
            start()
        }
    }

    private fun exitToNext() {
        if (isFinishing) return
        running = false

        binding.contentBlock.animate()
            .alpha(0f)
            .setDuration(320)
            .start()

        binding.wall.animate()
            .alpha(0f)
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(420)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.root.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 380)
    }

    override fun onPause() {
        super.onPause()
        running = false
        frameCallback?.let { choreographer?.removeFrameCallback(it) }
        lastFrameNanos = 0L
    }

    override fun onDestroy() {
        running = false
        frameCallback?.let { choreographer?.removeFrameCallback(it) }
        sweepAnimator?.cancel()
        offlineDialog?.dismiss()
        offlineDialog = null
        super.onDestroy()
    }
}