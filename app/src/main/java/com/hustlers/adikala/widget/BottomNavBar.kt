package com.hustlers.adikala.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.hustlers.adikala.R
import com.hustlers.adikala.databinding.BottomNavBarBinding

class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : carbon.widget.RelativeLayout(context, attrs, defStyleAttr) {

    enum class Tab { CART, HOME, PROFILE }

    private inner class TabData(
        val container: View,
        val pill: LinearLayout,
        val icon: ImageView,
        val text: TextView
    ) {
        var transitionAnimator: ValueAnimator? = null
        val pillBackground: Drawable = pill.background.mutate().also { pill.background = it }
        var fraction: Float = 0f
        var cachedTextWidth: Int = -1
        var cachedMargin: Int = -1
    }

    private val binding: BottomNavBarBinding
    private lateinit var tabs: Map<Tab, TabData>

    private var onCartClick: (() -> Unit)? = null
    private var onHomeClick: (() -> Unit)? = null
    private var onProfileClick: (() -> Unit)? = null

    private var activeTab: Tab = Tab.HOME
    private var isTransitioning: Boolean = false

    private val activeColor by lazy { ContextCompat.getColor(context, R.color.nav_icon_active) }
    private val inactiveColor by lazy { ContextCompat.getColor(context, R.color.nav_icon_inactive) }
    private val argbEvaluator = ArgbEvaluator()

    private val standardEasing = PathInterpolator(0.4f, 0f, 0.2f, 1f)
    private val bouncyEasing = OvershootInterpolator(0.6f)
    private val shakeEasing = PathInterpolator(0.34f, 1.56f, 0.64f, 1f)

    private val transitionDuration = 260L
    private val touchDuration = 130L
    private val shakeDuration = 260L
    private val iconBounceDuration = 260L

    private var barShakeAnimator: AnimatorSet? = null
    private var iconBounceAnimator: AnimatorSet? = null
    private var pendingResetRunnable: Runnable? = null

    init {
        binding = BottomNavBarBinding.inflate(LayoutInflater.from(context), this, true)
        initTabs()
        setupClickListeners()
        setInitialState()
    }

    private fun initTabs() {
        tabs = mapOf(
            Tab.CART to TabData(binding.navCart, binding.navCartPill, binding.navCartIcon, binding.navCartText),
            Tab.HOME to TabData(binding.navHome, binding.navHomePill, binding.navHomeIcon, binding.navHomeText),
            Tab.PROFILE to TabData(binding.navProfile, binding.navProfilePill, binding.navProfileIcon, binding.navProfileText)
        )
    }

    private fun setupClickListeners() {
        setupTabTouch(binding.navCart, Tab.CART) { onCartClick?.invoke() }
        setupTabTouch(binding.navHome, Tab.HOME) { onHomeClick?.invoke() }
        setupTabTouch(binding.navProfile, Tab.PROFILE) { onProfileClick?.invoke() }
    }

    private fun setupTabTouch(container: View, tab: Tab, onClick: () -> Unit) {
        container.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (tab != activeTab && !isTransitioning) animateTouchDown(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (tab != activeTab && !isTransitioning) {
                        animateTouchUp(view) {
                            setActive(tab, animate = true)
                            onClick()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (tab != activeTab && !isTransitioning) animateTouchCancel(view)
                    true
                }
                else -> false
            }
        }
    }

    private fun animateTouchDown(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .alpha(0.7f)
            .setDuration(touchDuration)
            .setInterpolator(standardEasing)
            .start()
    }

    private fun animateTouchUp(view: View, onComplete: () -> Unit) {
        view.animate().cancel()
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(touchDuration)
            .setInterpolator(bouncyEasing)
            .withEndAction { onComplete() }
            .start()
    }

    private fun animateTouchCancel(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(touchDuration)
            .setInterpolator(standardEasing)
            .start()
    }

    private fun setInitialState() {
        tabs.forEach { (tab, data) ->
            cacheTextMetrics(data)
            applyTabState(data, if (tab == Tab.HOME) 1f else 0f)
        }
    }

    private fun cacheTextMetrics(data: TabData) {
        if (data.cachedTextWidth == -1) {
            val originalAlpha = data.text.alpha
            val originalWidth = (data.text.layoutParams as LinearLayout.LayoutParams).width
            val originalMargin = (data.text.layoutParams as LinearLayout.LayoutParams).marginStart

            val lp = data.text.layoutParams as LinearLayout.LayoutParams
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
            lp.marginStart = dp(6)
            data.text.layoutParams = lp
            data.text.alpha = 1f

            data.text.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            data.cachedTextWidth = data.text.measuredWidth
            data.cachedMargin = dp(6)

            data.text.alpha = originalAlpha
            lp.width = originalWidth
            lp.marginStart = originalMargin
            data.text.layoutParams = lp
        }
    }

    fun setOnCartClickListener(listener: () -> Unit) { onCartClick = listener }
    fun setOnHomeClickListener(listener: () -> Unit) { onHomeClick = listener }
    fun setOnProfileClickListener(listener: () -> Unit) { onProfileClick = listener }
    fun getActiveTab(): Tab = activeTab

    fun setActive(tab: Tab, animate: Boolean = true) {
        if (tab == activeTab || isTransitioning) return
        val previousTab = activeTab
        activeTab = tab

        if (!animate) {
            tabs.forEach { (t, data) -> applyTabState(data, if (t == tab) 1f else 0f) }
            return
        }

        isTransitioning = true
        pendingResetRunnable?.let { removeCallbacks(it) }

        tabs[previousTab]?.let { animateTabTransition(it, selecting = false) }
        tabs[tab]?.let { data ->
            animateTabTransition(data, selecting = true)
            playWholeBarShake()
            playIconBounce(data.icon)
        }

        val runnable = Runnable { isTransitioning = false }
        pendingResetRunnable = runnable
        postDelayed(runnable, maxOf(transitionDuration, shakeDuration, iconBounceDuration))
    }

    private fun playWholeBarShake() {
        barShakeAnimator?.cancel()
        translationX = 0f
        translationY = 0f
        setLayerType(LAYER_TYPE_HARDWARE, null)

        val shakeX = ObjectAnimator.ofFloat(this, "translationX", 0f, dp(4f), -dp(3f), dp(1.5f), 0f)
        val shakeY = ObjectAnimator.ofFloat(this, "translationY", 0f, -dp(2f), dp(1.5f), 0f)

        barShakeAnimator = AnimatorSet().apply {
            playTogether(shakeX, shakeY)
            duration = shakeDuration
            interpolator = shakeEasing
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    translationX = 0f
                    translationY = 0f
                    setLayerType(LAYER_TYPE_NONE, null)
                }
                override fun onAnimationCancel(animation: Animator) {
                    translationX = 0f
                    translationY = 0f
                    setLayerType(LAYER_TYPE_NONE, null)
                }
            })
            start()
        }
    }

    private fun playIconBounce(icon: ImageView) {
        iconBounceAnimator?.cancel()
        icon.scaleX = 1f
        icon.scaleY = 1f
        icon.setLayerType(LAYER_TYPE_HARDWARE, null)

        val scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.22f, 1f)
        val scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.22f, 1f)

        iconBounceAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = iconBounceDuration
            interpolator = bouncyEasing
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    icon.scaleX = 1f
                    icon.scaleY = 1f
                    icon.setLayerType(LAYER_TYPE_NONE, null)
                }
                override fun onAnimationCancel(animation: Animator) {
                    icon.scaleX = 1f
                    icon.scaleY = 1f
                    icon.setLayerType(LAYER_TYPE_NONE, null)
                }
            })
            start()
        }
    }

    private fun applyTabState(data: TabData, fraction: Float) {
        data.fraction = fraction
        data.pillBackground.alpha = (255 * fraction).toInt()
        data.text.alpha = fraction

        val targetWidth = if (fraction <= 0f) 0 else data.cachedTextWidth
        val targetMargin = if (fraction <= 0f) 0 else data.cachedMargin

        val lp = data.text.layoutParams as LinearLayout.LayoutParams
        if (lp.width != targetWidth || lp.marginStart != targetMargin) {
            lp.width = targetWidth
            lp.marginStart = targetMargin
            data.text.layoutParams = lp
        }

        ImageViewCompat.setImageTintList(
            data.icon,
            ColorStateList.valueOf(argbEvaluator.evaluate(fraction, inactiveColor, activeColor) as Int)
        )
    }

    private fun animateTabTransition(data: TabData, selecting: Boolean) {
        data.transitionAnimator?.cancel()

        val targetTextWidth = data.cachedTextWidth
        val targetMargin = data.cachedMargin
        val startFraction = data.fraction
        val endFraction = if (selecting) 1f else 0f

        data.pill.setLayerType(LAYER_TYPE_HARDWARE, null)

        var lastWidth = -1
        var lastMargin = -1

        val animator = ValueAnimator.ofFloat(startFraction, endFraction).apply {
            duration = transitionDuration
            interpolator = standardEasing
            addUpdateListener { anim ->
                val f = anim.animatedValue as Float
                data.fraction = f
                data.pillBackground.alpha = (255 * f).toInt()
                data.text.alpha = f

                val width = (targetTextWidth * f).toInt()
                val margin = (targetMargin * f).toInt()

                if (width != lastWidth || margin != lastMargin) {
                    lastWidth = width
                    lastMargin = margin
                    val lp = data.text.layoutParams as LinearLayout.LayoutParams
                    lp.width = width
                    lp.marginStart = margin
                    data.text.layoutParams = lp
                }

                ImageViewCompat.setImageTintList(
                    data.icon,
                    ColorStateList.valueOf(argbEvaluator.evaluate(f, inactiveColor, activeColor) as Int)
                )
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    data.pill.setLayerType(LAYER_TYPE_NONE, null)
                    if (!selecting) {
                        val endLp = data.text.layoutParams as LinearLayout.LayoutParams
                        endLp.width = 0
                        endLp.marginStart = 0
                        data.text.layoutParams = endLp
                    }
                }
                override fun onAnimationCancel(animation: Animator) {
                    data.pill.setLayerType(LAYER_TYPE_NONE, null)
                }
            })
        }
        data.transitionAnimator = animator
        animator.start()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tabs.values.forEach { it.transitionAnimator?.cancel() }
        barShakeAnimator?.cancel()
        iconBounceAnimator?.cancel()
        pendingResetRunnable?.let { removeCallbacks(it) }
    }
}