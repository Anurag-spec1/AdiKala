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
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.databinding.ActivityMainBinding
import com.devshiv.adikala.view.fragment.HomeFragment
import com.devshiv.adikala.view.fragment.OrdersFragment
import com.devshiv.adikala.view.fragment.ProfileFragment
import com.devshiv.adikala.widget.BottomNavBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableFullScreen()

        val token = ShopifyAuthManager(this).getAccessToken()
        Log.d("ACCESS_TOKEN", token ?: "NO TOKEN FOUND")

        binding.bottomNavBar.setOnHomeClickListener {
            navigateTo(BottomNavBar.Tab.HOME)
        }

        binding.bottomNavBar.setOnCartClickListener {
            navigateTo(BottomNavBar.Tab.ORDERS)
        }

        binding.bottomNavBar.setOnProfileClickListener {
            navigateTo(BottomNavBar.Tab.PROFILE)
        }

        if (savedInstanceState == null) {
            val navigateTo = intent.getStringExtra("navigate_to")
            when (navigateTo) {
                "profile" -> navigateTo(BottomNavBar.Tab.PROFILE)
                "orders" -> navigateTo(BottomNavBar.Tab.ORDERS)
                else -> navigateTo(BottomNavBar.Tab.HOME)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        when (intent.getStringExtra("navigate_to")) {
            "profile" -> navigateTo(BottomNavBar.Tab.PROFILE)
            "orders" -> navigateTo(BottomNavBar.Tab.ORDERS)
            "home" -> navigateTo(BottomNavBar.Tab.HOME)
        }
    }

    private fun navigateTo(tab: BottomNavBar.Tab) {
        binding.bottomNavBar.setActive(tab)

        val fragment = when (tab) {
            BottomNavBar.Tab.HOME -> HomeFragment()
            BottomNavBar.Tab.ORDERS -> OrdersFragment()
            BottomNavBar.Tab.PROFILE -> ProfileFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commitNow()
    }

    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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