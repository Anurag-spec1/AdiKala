package com.devshiv.adikala.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devshiv.adikala.databinding.ActivityCartBinding
import com.devshiv.adikala.repository.CartRepository.CartLineItem
import com.devshiv.adikala.view.adapter.CartAdapter
import com.devshiv.adikala.view.adapter.CartSuggestionAdapter
import com.devshiv.adikala.viewmodel.CartViewModel
import com.devshiv.adikala.model.Product

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CartViewModel::class.java]

        setupCart()
        setupSuggestions()
        setupListeners()
        observeViewModel()
    }

    private fun setupCart() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { lineId, quantity ->
                viewModel.updateQuantity(lineId, quantity)
            },
            onRemove = { lineId ->
                viewModel.removeItem(lineId)
            }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = cartAdapter
    }

    private fun setupSuggestions() {
        val suggestions = listOf(
            Product("s1", "Peacock printed MDF Cutout Pack of 4", "", "", 399, 349),
            Product("s2", "Decorative Wooden Beads Assorted", "", "", 299, 249),
            Product("s3", "MDF Base Round Pack of 6", "", "", 499, 429)
        )

        binding.rvSuggest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSuggest.adapter = CartSuggestionAdapter(
            suggestions,
            onAdd = { product ->
                viewModel.addItem(product.id)
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
            },
            onClick = { /* Navigate to product detail */ }
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCheckout.setOnClickListener {
            viewModel.getCheckoutUrl()?.let { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } ?: Toast.makeText(this, "Checkout unavailable", Toast.LENGTH_SHORT).show()
        }
        binding.btnShopEmpty.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.cartData.observe(this) { cart ->
            if (cart != null) {
                cartAdapter.submitList(cart.items)
                binding.tvItemTotal.text = cart.totalAmount
                binding.tvSubtotal.text = cart.totalAmount
                render(false)
            } else {
                render(true)
            }
        }

        viewModel.isEmpty.observe(this) { empty -> render(empty) }
//        viewModel.isLoading.observe(this) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.error.observe(this) { it?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show(); viewModel.clearError() } }
        viewModel.itemAdded.observe(this) { if (it == true) viewModel.clearItemAdded() }
    }

    private fun render(empty: Boolean) {
        binding.scroll.visibility = if (empty) View.GONE else View.VISIBLE
        binding.bottomBar.visibility = if (empty) View.GONE else View.VISIBLE
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
    }
}