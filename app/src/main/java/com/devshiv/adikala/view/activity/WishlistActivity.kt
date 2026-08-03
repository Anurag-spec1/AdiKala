package com.devshiv.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devshiv.adikala.databinding.ActivityWishlistBinding
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.utils.WishlistManager
import com.devshiv.adikala.view.adapter.ProductAdapter
import com.devshiv.adikala.viewmodel.CartViewModel

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private lateinit var productAdapter: ProductAdapter

    private lateinit var cartviewmodel : CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartviewmodel = ViewModelProvider(this)[CartViewModel::class.java]

        setupRecycler()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(
            emptyList(),
            onClick = { product ->
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                })
            },
            onAddToCart = { product ->
                val variantId = "gid://shopify/ProductVariant/${product.variants.firstOrNull()?.id ?: product.id}"
                cartviewmodel.addItem(variantId, product.name)
                Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            },
            onWishlistToggle = { product ->
                loadWishlist()
            }
        )
        binding.rvWishlist.layoutManager = GridLayoutManager(this, 2)
        binding.rvWishlist.adapter = productAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadWishlist() {
        val wishlistItems = WishlistManager.getWishlist(this)
        productAdapter.updateList(wishlistItems)

        if (wishlistItems.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvWishlist.visibility = View.GONE
            binding.tvCount.text = "0 items"
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvWishlist.visibility = View.VISIBLE
            binding.tvCount.text = "${wishlistItems.size} items"
        }
    }
}