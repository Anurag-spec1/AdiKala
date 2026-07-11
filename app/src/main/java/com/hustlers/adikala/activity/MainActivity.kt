package com.hustlers.adikala.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hustlers.adikala.fragment.HomeFragment
import com.hustlers.adikala.R
import com.hustlers.adikala.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.bottomNavBar.setOnHomeClickListener {
            loadFragment(HomeFragment())
        }
        binding.bottomNavBar.setOnCartClickListener {
//            loadFragment(CartFragment())
        }
        binding.bottomNavBar.setOnProfileClickListener {
//            loadFragment(ProfileFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}