package com.devshiv.adikala.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.databinding.ActivityMainBinding
import com.devshiv.adikala.view.fragment.HomeFragment
import com.devshiv.adikala.view.fragment.OrdersFragment
import com.devshiv.adikala.view.fragment.ProfileFragment
import com.devshiv.adikala.widget.BottomNavBar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG_HOME = "home"
    private val TAG_ORDERS = "orders"
    private val TAG_PROFILE = "profile"

    private lateinit var homeFragment: Fragment
    private lateinit var ordersFragment: Fragment
    private lateinit var profileFragment: Fragment
    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableFullScreen()

        val token = ShopifyAuthManager(this).getAccessToken()
        Log.d("ACCESS_TOKEN", token ?: "NO TOKEN FOUND")

        setupFragments(savedInstanceState)

        val navigateTo = intent.getStringExtra("navigate_to")

        if (savedInstanceState == null) {
            if (navigateTo == "profile") {
                binding.bottomNavBar.selectTab(BottomNavBar.Tab.PROFILE)
            }

        }

        binding.bottomNavBar.setOnHomeClickListener {
            switchTo(homeFragment)
        }
        binding.bottomNavBar.setOnCartClickListener {
            switchTo(ordersFragment)
        }
        binding.bottomNavBar.setOnProfileClickListener {
            switchTo(profileFragment)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("navigate_to")?.let { tab ->
            if (tab == "profile") {
                binding.bottomNavBar.selectTab(BottomNavBar.Tab.PROFILE)
            }
            if (tab == "home") {
                binding.bottomNavBar.selectTab(BottomNavBar.Tab.HOME)
            }
        }
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        val fm = supportFragmentManager

        if (savedInstanceState != null) {
            homeFragment = fm.findFragmentByTag(TAG_HOME) ?: HomeFragment()
            ordersFragment = fm.findFragmentByTag(TAG_ORDERS) ?: OrdersFragment()
            profileFragment = fm.findFragmentByTag(TAG_PROFILE) ?: ProfileFragment()
            activeFragment = listOf(homeFragment, ordersFragment, profileFragment)
                .firstOrNull { it.isVisible } ?: homeFragment
            return
        }

        homeFragment = HomeFragment()
        ordersFragment = OrdersFragment()
        profileFragment = ProfileFragment()
        activeFragment = homeFragment

        fm.beginTransaction()
            .add(R.id.fragmentContainer, profileFragment, TAG_PROFILE).hide(profileFragment)
            .add(R.id.fragmentContainer, ordersFragment, TAG_ORDERS).hide(ordersFragment)
            .add(R.id.fragmentContainer, homeFragment, TAG_HOME)
            .commitNow()
    }

    private fun switchTo(fragment: Fragment) {
        if (fragment === activeFragment) return

        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_NONE)
            .hide(activeFragment)
            .show(fragment)
            .commitNow()

        activeFragment = fragment
    }

    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}