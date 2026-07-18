package com.hustlers.adikala.view.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hustlers.adikala.view.adapter.CartAdapter
import com.hustlers.adikala.view.adapter.CartSuggestionAdapter
import com.hustlers.adikala.databinding.ActivityCartBinding
import com.hustlers.adikala.model.CartItem
import com.hustlers.adikala.model.Product

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCart()
        setupCart()
        setupSuggestions()
        setupListeners()
        render()
    }

    private fun setupCart() {
        cartAdapter = CartAdapter(
            cartItems,
            onQtyChanged = { updateTotals() },
            onRemove = { item ->
                val index = cartItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    cartItems.removeAt(index)
                    cartAdapter.notifyItemRemoved(index)
                    render()
                }
            }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = cartAdapter
    }

    private fun setupSuggestions() {
        val suggestions = listOf(
            Product("s1", "Peacock printed MDF Cutout Pack of 4", "", "https://www.adikala.in/cdn/shop/collections/pichwai.jpg?v=1737890053&width=1080", 399, 349),
            Product("s2", "Decorative Wooden Beads Assorted", "", "https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080", 299, 249),
            Product("s3", "MDF Base Round Pack of 6", "", "https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080", 499, 429)
        )

        binding.rvSuggest.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSuggest.adapter = CartSuggestionAdapter(
            suggestions,
            onAdd = { product ->
                cartItems.add(CartItem(product.id, product.name, product.imageUrl, product.discountedPrice, 1))
                cartAdapter.notifyItemInserted(cartItems.size - 1)
                render()
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
            },
            onClick = { }
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCheckout.setOnClickListener { }
        binding.btnShopEmpty.setOnClickListener { finish() }
    }

    private fun render() {
        val empty = cartItems.isEmpty()
        binding.scroll.visibility = if (empty) View.GONE else View.VISIBLE
        binding.bottomBar.visibility = if (empty) View.GONE else View.VISIBLE
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        if (!empty) updateTotals()
    }

    private fun updateTotals() {
        val total = cartItems.sumOf { it.price * it.quantity }
        val formatted = "₹%,d".format(total)
        binding.tvItemTotal.text = formatted
        binding.tvSubtotal.text = formatted
    }

    private fun loadCart() {
        cartItems.clear()
        cartItems.addAll(
            listOf(
                CartItem("1", "Sea Shell Mother of Pearl Two Hole Square Buttons Set of 25 For Sewing, suit, shirt and Crafting", "https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080", 150, 5),
                CartItem("2", "White Raffia Ring Pack of 10 | for Wedding & Festive Decoration, DIY Art & Craft", "https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080", 1000, 5, "8IN")
            )
        )
    }
}