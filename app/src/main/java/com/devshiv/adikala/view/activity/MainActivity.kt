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

        val navigateTo = intent.getStringExtra("navigate_to")

        if (savedInstanceState == null) {
            if (navigateTo == "profile") {
                binding.bottomNavBar.selectTab(BottomNavBar.Tab.PROFILE)
            } else {
                loadFragment(HomeFragment())
            }
        }

        binding.bottomNavBar.setOnHomeClickListener {
            loadFragment(HomeFragment())
        }
        binding.bottomNavBar.setOnCartClickListener {
            loadFragment(OrdersFragment())
        }
        binding.bottomNavBar.setOnProfileClickListener {
            loadFragment(ProfileFragment())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("navigate_to")?.let { tab ->
            if (tab == "profile") {
                binding.bottomNavBar.selectTab(BottomNavBar.Tab.PROFILE)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
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