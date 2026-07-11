package com.hustlers.adikala.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.hustlers.adikala.R

class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : carbon.widget.RelativeLayout(context, attrs, defStyleAttr) {

    enum class Tab { CART, HOME, PROFILE }

    private lateinit var cartContainer: FrameLayout
    private lateinit var homeContainer: FrameLayout
    private lateinit var profileContainer: FrameLayout

    private lateinit var cartIcon: ImageView
    private lateinit var homeIcon: ImageView
    private lateinit var profileIcon: ImageView

    private lateinit var homeHighlight: View

    private var onCartClick: (() -> Unit)? = null
    private var onHomeClick: (() -> Unit)? = null
    private var onProfileClick: (() -> Unit)? = null

    private var activeTab: Tab = Tab.HOME

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_nav_bar, this, true)

        cartContainer = findViewById(R.id.nav_cart)
        homeContainer = findViewById(R.id.nav_home)
        profileContainer = findViewById(R.id.nav_profile)

        cartIcon = findViewById(R.id.nav_cart_icon)
        homeIcon = findViewById(R.id.nav_home_icon)
        profileIcon = findViewById(R.id.nav_profile_icon)

        homeHighlight = findViewById(R.id.nav_home_bg)

        cartContainer.setOnClickListener {
            setActive(Tab.CART)
            onCartClick?.invoke()
        }
        homeContainer.setOnClickListener {
            setActive(Tab.HOME)
            onHomeClick?.invoke()
        }
        profileContainer.setOnClickListener {
            setActive(Tab.PROFILE)
            onProfileClick?.invoke()
        }

        setActive(Tab.HOME)
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

    fun setActive(tab: Tab) {
        activeTab = tab

        homeHighlight.visibility = if (tab == Tab.HOME) View.VISIBLE else View.GONE

        val activeColor = ContextCompat.getColor(context, R.color.nav_icon_active)
        val inactiveColor = ContextCompat.getColor(context, R.color.nav_icon_inactive)

        cartIcon.setColorFilter(if (tab == Tab.CART) activeColor else inactiveColor)
        homeIcon.setColorFilter(if (tab == Tab.HOME) activeColor else inactiveColor)
        profileIcon.setColorFilter(if (tab == Tab.PROFILE) activeColor else inactiveColor)
    }

    fun getActiveTab(): Tab = activeTab
}