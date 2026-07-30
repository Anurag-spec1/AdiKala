package com.devshiv.adikala.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.devshiv.adikala.R
import com.devshiv.adikala.databinding.BottomNavBarBinding

class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : carbon.widget.RelativeLayout(context, attrs, defStyleAttr) {

    enum class Tab { ORDERS, HOME, PROFILE }

    private val binding: BottomNavBarBinding

    private var onCartClick: (() -> Unit)? = null
    private var onHomeClick: (() -> Unit)? = null
    private var onProfileClick: (() -> Unit)? = null

    private var activeTab: Tab = Tab.HOME

    private val activeColor by lazy { ContextCompat.getColor(context, R.color.nav_icon_active) }
    private val inactiveColor by lazy { ContextCompat.getColor(context, R.color.nav_icon_inactive) }

    init {
        binding = BottomNavBarBinding.inflate(LayoutInflater.from(context), this, true)
        setupTouchListeners()
        setInitialState()
    }

    private fun setupTouchListeners() {
        binding.navCart.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setActive(Tab.ORDERS)
                onCartClick?.invoke()
                true
            } else false
        }
        binding.navHome.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setActive(Tab.HOME)
                onHomeClick?.invoke()
                true
            } else false
        }
        binding.navProfile.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setActive(Tab.PROFILE)
                onProfileClick?.invoke()
                true
            } else false
        }
    }

    private fun setInitialState() {
        updateTab(binding.navCartPill, binding.navCartIcon, binding.navCartText, Tab.ORDERS)
        updateTab(binding.navHomePill, binding.navHomeIcon, binding.navHomeText, Tab.HOME)
        updateTab(binding.navProfilePill, binding.navProfileIcon, binding.navProfileText, Tab.PROFILE)
    }

    fun setOnCartClickListener(listener: () -> Unit) { onCartClick = listener }
    fun setOnHomeClickListener(listener: () -> Unit) { onHomeClick = listener }
    fun setOnProfileClickListener(listener: () -> Unit) { onProfileClick = listener }
    fun getActiveTab(): Tab = activeTab

    fun setActive(tab: Tab) {
        if (tab == activeTab) return
        activeTab = tab
        setInitialState()
    }

    fun selectTab(tab: Tab) {
        setActive(tab)
        when (tab) {
            Tab.ORDERS -> onCartClick?.invoke()
            Tab.HOME -> onHomeClick?.invoke()
            Tab.PROFILE -> onProfileClick?.invoke()
        }
    }

    private fun updateTab(pill: LinearLayout, icon: ImageView, text: TextView, tab: Tab) {
        val isActive = tab == activeTab
        pill.background.alpha = if (isActive) 255 else 0
        text.alpha = if (isActive) 1f else 0f

        val lp = text.layoutParams as LinearLayout.LayoutParams
        if (isActive) {
            text.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            lp.width = text.measuredWidth
            lp.marginStart = dp(6)
        } else {
            lp.width = 0
            lp.marginStart = 0
        }
        text.layoutParams = lp

        ImageViewCompat.setImageTintList(
            icon,
            ColorStateList.valueOf(if (isActive) activeColor else inactiveColor)
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}