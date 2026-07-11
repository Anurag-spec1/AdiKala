package com.hustlers.adikala.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.hustlers.adikala.R

class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : carbon.widget.RelativeLayout(context, attrs, defStyleAttr) {

    enum class Tab { CART, HOME, PROFILE }

    private inner class TabHolder(
        val pill: LinearLayout,
        val icon: ImageView,
        val text: TextView
    ) {
        var animator: ValueAnimator? = null
        val pillBackground: Drawable = pill.background.mutate().also { pill.background = it }
        var fraction: Float = 0f
    }

    private lateinit var holders: Map<Tab, TabHolder>

    private var onCartClick: (() -> Unit)? = null
    private var onHomeClick: (() -> Unit)? = null
    private var onProfileClick: (() -> Unit)? = null

    private var activeTab: Tab = Tab.HOME

    private val activeColor by lazy { ContextCompat.getColor(context, R.color.nav_icon_active) }
    private val inactiveColor by lazy { ContextCompat.getColor(context, R.color.nav_icon_inactive) }
    private val argbEvaluator = ArgbEvaluator()

    private val easing = PathInterpolator(0.4f, 0f, 0.2f, 1f)
    private val animDuration = 260L

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_nav_bar, this, true)

        holders = mapOf(
            Tab.CART to TabHolder(
                findViewById(R.id.nav_cart_pill),
                findViewById(R.id.nav_cart_icon),
                findViewById(R.id.nav_cart_text)
            ),
            Tab.HOME to TabHolder(
                findViewById(R.id.nav_home_pill),
                findViewById(R.id.nav_home_icon),
                findViewById(R.id.nav_home_text)
            ),
            Tab.PROFILE to TabHolder(
                findViewById(R.id.nav_profile_pill),
                findViewById(R.id.nav_profile_icon),
                findViewById(R.id.nav_profile_text)
            )
        )

        findViewById<View>(R.id.nav_cart).setOnClickListener {
            setActive(Tab.CART, animate = true)
            onCartClick?.invoke()
        }
        findViewById<View>(R.id.nav_home).setOnClickListener {
            setActive(Tab.HOME, animate = true)
            onHomeClick?.invoke()
        }
        findViewById<View>(R.id.nav_profile).setOnClickListener {
            setActive(Tab.PROFILE, animate = true)
            onProfileClick?.invoke()
        }

        holders.forEach { (tab, holder) ->
            applyState(
                holder,
                fraction = if (tab == Tab.HOME) 1f else 0f
            )
        }
    }

    fun setOnCartClickListener(listener: () -> Unit) {
        onCartClick = listener
    }

    fun setOnHomeClickListener(listener: () -> Unit) {
        onHomeClick = listener
    }

    fun setOnProfileClickListener(listener: () -> Unit) {
        onProfileClick = listener
    }

    fun getActiveTab(): Tab = activeTab

    fun setActive(tab: Tab, animate: Boolean = true) {
        if (tab == activeTab) return
        val previous = activeTab
        activeTab = tab

        if (!animate) {
            holders.forEach { (t, holder) ->
                applyState(
                    holder,
                    fraction = if (t == tab) 1f else 0f
                )
            }
            return
        }

        holders[previous]?.let { animateTab(it, selecting = false) }
        holders[tab]?.let { animateTab(it, selecting = true) }
    }

    private fun applyState(holder: TabHolder, fraction: Float) {
        holder.fraction = fraction
        holder.pillBackground.alpha = (255 * fraction).toInt()
        holder.text.alpha = fraction
        val lp = holder.text.layoutParams
        lp.width = if (fraction <= 0f) 0 else measureTextWidth(holder.text)
        holder.text.layoutParams = lp
        holder.text.marginStart(if (fraction <= 0f) 0 else dp(6))
        holder.text.requestLayout()
        ImageViewCompat.setImageTintList(
            holder.icon,
            android.content.res.ColorStateList.valueOf(
                argbEvaluator.evaluate(fraction, inactiveColor, activeColor) as Int
            )
        )
    }

    private fun animateTab(holder: TabHolder, selecting: Boolean) {
        holder.animator?.cancel()

        val targetTextWidth = measureTextWidth(holder.text)
        val targetMargin = dp(6)

        val startFraction = holder.fraction
        val endFraction = if (selecting) 1f else 0f

        val animator = ValueAnimator.ofFloat(startFraction, endFraction).apply {
            duration = animDuration
            interpolator = easing
            addUpdateListener { anim ->
                val f = anim.animatedValue as Float
                holder.fraction = f

                holder.pillBackground.alpha = (255 * f).toInt()
                holder.text.alpha = f

                val lp = holder.text.layoutParams
                lp.width = (targetTextWidth * f).toInt()
                holder.text.layoutParams = lp
                holder.text.marginStart((targetMargin * f).toInt())

                ImageViewCompat.setImageTintList(
                    holder.icon,
                    android.content.res.ColorStateList.valueOf(
                        argbEvaluator.evaluate(f, inactiveColor, activeColor) as Int
                    )
                )
            }
            if (!selecting) {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        val endLp = holder.text.layoutParams
                        endLp.width = 0
                        holder.text.layoutParams = endLp
                        holder.text.marginStart(0)
                    }
                })
            }
        }
        holder.animator = animator
        animator.start()
    }

    private fun measureTextWidth(text: TextView): Int {
        text.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        return text.measuredWidth
    }

    private fun TextView.marginStart(px: Int) {
        val lp = layoutParams as LinearLayout.LayoutParams
        lp.marginStart = px
        layoutParams = lp
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}