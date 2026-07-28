package com.devshiv.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.devshiv.adikala.view.adapter.ProductAdapter
import com.devshiv.adikala.view.adapter.SortAdapter
import com.devshiv.adikala.databinding.ActivityProductsBinding
import com.devshiv.adikala.databinding.SheetSortBinding
import com.devshiv.adikala.model.SortOption
import com.devshiv.adikala.viewmodel.CartViewModel
import com.devshiv.adikala.viewmodel.ProductsViewModel

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var viewModel: ProductsViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var productAdapter: ProductAdapter
    private var currentSort = SortOption.FEATURED
    private var collectionHandle = ""

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_COLLECTION_HANDLE = "extra_collection_handle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsViewModel::class.java]
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        val categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: "Products"
        collectionHandle = intent.getStringExtra(EXTRA_COLLECTION_HANDLE) ?: ""

        binding.tvCategoryName.text = categoryName
        binding.tvActiveSort.text = SortOption.FEATURED.label

        setupRecycler()
        setupObservers()
        setupListeners()
        loadProducts()
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(emptyList(),
            onClick = { product ->
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                })
            },
            onAddToCart = { product ->
                val variantId = "gid://shopify/ProductVariant/${product.variants.firstOrNull()?.id ?: product.id}"
                cartViewModel.addItem(variantId, product.name)
                Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            },
            onWishlistToggle = { product ->
                product.isWishlisted = !product.isWishlisted
                productAdapter.notifyDataSetChanged()
            }
        )
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = productAdapter

        binding.rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalItems = (recyclerView.adapter?.itemCount ?: 0) - 1
                if (lastVisible >= totalItems - 5) {
                    viewModel.loadMoreProducts()
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.filteredProducts.observe(this) { products ->
            binding.shimmerProducts.stopShimmer()
            binding.shimmerProducts.visibility = View.GONE

            if (products.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
                binding.tvResultCount.text = "0 products"
            } else {
                binding.emptyState.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
                productAdapter.updateList(products)
                binding.tvResultCount.text = "${products.size} products"
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.shimmerProducts.visibility = View.VISIBLE
                binding.shimmerProducts.startShimmer()
                binding.rvProducts.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
            }
        }
        viewModel.error.observe(this) { error ->
            if (!error.isNullOrEmpty()) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSort.setOnClickListener { openSortSheet() }
        binding.chipActiveSort.setOnClickListener { openSortSheet() }
    }

    private fun loadProducts() {
        if (collectionHandle.isNotEmpty()) {
            viewModel.loadProductsByCollection(collectionHandle)
        }
    }

    private fun openSortSheet() {
        val sheet = BottomSheetDialog(this)
        val sheetBinding = SheetSortBinding.inflate(layoutInflater)
        sheet.setContentView(sheetBinding.root)
        sheetBinding.rvSortOptions.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        sheetBinding.rvSortOptions.adapter = SortAdapter(currentSort) { picked ->
            currentSort = picked
            binding.tvActiveSort.text = picked.label
            viewModel.applySort(picked)
            sheet.dismiss()
        }
        sheet.show()
    }
}