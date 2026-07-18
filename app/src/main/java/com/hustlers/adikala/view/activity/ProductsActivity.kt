package com.hustlers.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hustlers.adikala.view.adapter.ProductAdapter
import com.hustlers.adikala.view.adapter.SortAdapter
import com.hustlers.adikala.databinding.ActivityProductsBinding
import com.hustlers.adikala.databinding.SheetSortBinding
import com.hustlers.adikala.model.Product
import com.hustlers.adikala.model.SortOption
import com.hustlers.adikala.viewmodel.ProductsViewModel

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var viewModel: ProductsViewModel
    private lateinit var productAdapter: ProductAdapter

    private var currentSort = SortOption.FEATURED
    private var categoryName = "Products"
    private var collectionHandle = ""
    private var subCategoryName = ""

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_COLLECTION_HANDLE = "extra_collection_handle"
        const val EXTRA_SUBCATEGORY = "extra_subcategory"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsViewModel::class.java]

        categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: "Products"
        collectionHandle = intent.getStringExtra(EXTRA_COLLECTION_HANDLE) ?: ""
        subCategoryName = intent.getStringExtra(EXTRA_SUBCATEGORY) ?: ""

        binding.tvCategoryName.text = categoryName

        setupRecycler()
        setupObservers()
        setupListeners()
        loadProducts()
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(
            list = emptyList(),
            onClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                startActivity(intent)
            },
            onAddToCart = { product ->
                addToCart(product)
            },
            onWishlistToggle = { product ->
                product.isWishlisted = !product.isWishlisted
                productAdapter.notifyDataSetChanged()
            }
        )

        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = productAdapter
    }

    private fun setupObservers() {
        viewModel.filteredProducts.observe(this) { products ->
            if (products.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
                productAdapter = ProductAdapter(
                    list = products,
                    onClick = { product ->
                        val intent = Intent(this, ProductDetailActivity::class.java)
                        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                        startActivity(intent)
                    },
                    onAddToCart = { product ->
                        addToCart(product)
                    },
                    onWishlistToggle = { product ->
                        product.isWishlisted = !product.isWishlisted
                        productAdapter.notifyDataSetChanged()
                    }
                )
                binding.rvProducts.adapter = productAdapter
                binding.tvResultCount.text = "${products.size} products"
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSort.setOnClickListener { openSortSheet() }
        binding.chipActiveSort.setOnClickListener { openSortSheet() }
    }

    private fun loadProducts() {
        if (subCategoryName.isNotEmpty() && collectionHandle.isNotEmpty()) {
            viewModel.loadProductsBySubCategory(collectionHandle, subCategoryName)
        } else if (collectionHandle.isNotEmpty()) {
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

    private fun addToCart(product: Product) {
        val variantId = product.variants.firstOrNull()?.id ?: return
        val intent = Intent(this, CartActivity::class.java)
        intent.putExtra("variant_id", variantId)
        intent.putExtra("quantity", 1)
        startActivity(intent)
    }
}