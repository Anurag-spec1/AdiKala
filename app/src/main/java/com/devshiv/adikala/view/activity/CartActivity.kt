package com.devshiv.adikala.view.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.databinding.ActivityCartBinding
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.view.adapter.CartAdapter
import com.devshiv.adikala.view.adapter.CartSuggestionAdapter
import com.devshiv.adikala.viewmodel.CartViewModel

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var authManager: ShopifyAuthManager

    private var isCartEmpty: Boolean = true
    private var isCurrentlyLoading: Boolean = false
    private var latestSuggestions: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CartViewModel::class.java]
        authManager = ShopifyAuthManager(this)

        setupCart()
        setupListeners()
        observeViewModel()
    }

    private fun setupCart() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { lineId, quantity ->
                viewModel.updateQuantity(lineId, quantity)
            },
            onRemove = { lineId, variantId ->
                viewModel.removeItem(lineId, variantId)
            }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = cartAdapter
        binding.rvSuggest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCheckout.setOnClickListener { handleCheckout() }
        binding.btnShopEmpty.setOnClickListener { finish() }
    }

    private fun handleCheckout() {
        if (!authManager.isLoggedIn()) {
            showLoginRequiredDialog()
            return
        }
        openCheckout()
    }

    private fun showLoginRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_required, null)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<View>(R.id.btnSignIn).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, com.devshiv.adikala.view.activity.MainActivity::class.java)
            intent.putExtra("navigate_to", "profile")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openCheckout() {
        viewModel.getAuthenticatedCheckoutUrl { url ->
            url?.let {
                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setToolbarColor(android.graphics.Color.parseColor("#0D3238"))
                        .setShowTitle(true)
                        .setUrlBarHidingEnabled(true)
                        .setInstantAppsEnabled(false)
                        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                        .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_chevron_left))
                        .build()
                    customTabsIntent.launchUrl(this, Uri.parse(it))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                }
            } ?: run {
                viewModel.getCheckoutUrl()?.let { fallbackUrl ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
                } ?: Toast.makeText(this, "Checkout unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(this) { items ->
            isCartEmpty = items.isEmpty()
            if (items.isNotEmpty()) {
                cartAdapter.submitList(items)
            }
            updateVisibility()
            updateSuggestionsUi()
        }
        viewModel.totalAmount.observe(this) { amount ->
            binding.tvItemTotal.text = amount
            binding.tvSubtotal.text = amount
        }
        viewModel.isLoading.observe(this) { isLoading ->
            isCurrentlyLoading = isLoading
            updateVisibility()
        }
        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
        viewModel.operationMessage.observe(this) { message ->
            message?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
        viewModel.suggestions.observe(this) { suggestions ->
            latestSuggestions = suggestions.map {
                Product(
                    id = it.variantId,
                    name = it.title,
                    description = "",
                    imageUrl = it.imageUrl,
                    actualPrice = it.price.toDoubleOrNull()?.toInt() ?: 0,
                    discountedPrice = it.price.toDoubleOrNull()?.toInt() ?: 0
                )
            }
            updateSuggestionsUi()
        }
    }

    private fun updateSuggestionsUi() {
        val hasItems = !isCartEmpty
        val shouldShow = latestSuggestions.isNotEmpty() && hasItems
        binding.suggestSection.visibility = if (shouldShow) View.VISIBLE else View.GONE
        if (shouldShow) {
            binding.rvSuggest.adapter = CartSuggestionAdapter(
                latestSuggestions,
                onAdd = { product ->
                    viewModel.addItem(product.id, product.name)
                },
                onClick = { }
            )
        }
    }

    private fun updateVisibility() {
        binding.progressBar.visibility = if (isCurrentlyLoading) View.VISIBLE else View.GONE

        if (isCurrentlyLoading) {
            binding.scroll.visibility = View.GONE
            binding.bottomBar.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
            return
        }

        if (isCartEmpty) {
            binding.scroll.visibility = View.GONE
            binding.bottomBar.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.scroll.visibility = View.VISIBLE
            binding.bottomBar.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }
}