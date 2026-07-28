package com.devshiv.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.devshiv.adikala.databinding.ActivitySearchBinding
import com.devshiv.adikala.view.adapter.ProductAdapter
import com.devshiv.adikala.viewmodel.SearchViewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        val query = intent.getStringExtra("search_query") ?: ""

        setupRecycler()
        setupObservers()
        setupListeners()

        if (query.isNotEmpty()) {
            binding.etSearch.setText(query)
            binding.etSearch.setSelection(query.length)
            viewModel.searchProducts(query)
        }

        binding.etSearch.requestFocus()
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(
            emptyList(),
            onClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                startActivity(intent)
            },
            onAddToCart = { product ->
                val variantId = product.variants.firstOrNull()?.id ?: return@ProductAdapter
                val intent = Intent(this, CartActivity::class.java)
                intent.putExtra("variant_id", variantId)
                intent.putExtra("quantity", 1)
                startActivity(intent)
            },
            onWishlistToggle = { product ->
                product.isWishlisted = !product.isWishlisted
                productAdapter.notifyDataSetChanged()
            }
        )
        binding.rvSearchResults.layoutManager = GridLayoutManager(this, 2)
        binding.rvSearchResults.adapter = productAdapter
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(this) { products ->
            binding.rvSearchResults.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
            productAdapter.updateList(products)
        }

        viewModel.isEmpty.observe(this) { isEmpty ->
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchProducts(query)
                }
                true
            } else false
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
}