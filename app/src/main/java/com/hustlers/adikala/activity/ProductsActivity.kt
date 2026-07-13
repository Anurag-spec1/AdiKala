package com.hustlers.adikala.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hustlers.adikala.adapter.ProductAdapter
import com.hustlers.adikala.adapter.SortAdapter
import com.hustlers.adikala.databinding.ActivityProductsBinding
import com.hustlers.adikala.databinding.SheetSortBinding
import com.hustlers.adikala.dataclass.Product
import com.hustlers.adikala.dataclass.SortOption

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var productAdapter: ProductAdapter

    private val productList = mutableListOf<Product>()
    private val displayList = mutableListOf<Product>()

    private var currentSort = SortOption.FEATURED
    private var categoryName = "Products"

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: "Products"
        binding.tvCategoryName.text = categoryName

        setupRecycler()
        setupListeners()

        loadProducts()
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(
            list = displayList,
            onClick = { product ->

            },
            onAddToCart = { product ->

            },
            onWishlistToggle = { product ->
                product.isWishlisted = !product.isWishlisted
                val index = productList.indexOfFirst { it.id == product.id }
                if (index != -1) productList[index].isWishlisted = product.isWishlisted
                productAdapter.notifyDataSetChanged()
            }
        )
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = productAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSort.setOnClickListener { openSortSheet() }
        binding.chipActiveSort.setOnClickListener { openSortSheet() }
    }

    private fun loadProducts() {
        productList.clear()
        productList.addAll(
            listOf(
                Product("1", "Brass Diya Set", "Handcrafted Brass Diya Set with Intricate Detailing",
                    "", 999, 799, 4.8, 320, 1_720_000_000_000, false),
                Product("2", "Wooden Temple", "Solid Sheesham Wood Home Temple with Carved Pillars",
                    "", 5499, 4299, 4.6, 140, 1_715_000_000_000, false)
            )
        )
        applySort(currentSort)
    }

    private fun openSortSheet() {
        val sheet = BottomSheetDialog(this)
        val sheetBinding = SheetSortBinding.inflate(layoutInflater)
        sheet.setContentView(sheetBinding.root)

        sheetBinding.rvSortOptions.layoutManager = LinearLayoutManager(this)
        sheetBinding.rvSortOptions.adapter = SortAdapter(currentSort) { picked ->
            currentSort = picked
            binding.tvActiveSort.text = picked.label
            applySort(picked)
            sheet.dismiss()
        }
        sheet.show()
    }

    private fun applySort(sort: SortOption) {
        val sorted = when (sort) {
            SortOption.NAME_ASC       -> productList.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC      -> productList.sortedByDescending { it.name.lowercase() }
            SortOption.PRICE_LOW_HIGH -> productList.sortedBy { it.discountedPrice }
            SortOption.PRICE_HIGH_LOW -> productList.sortedByDescending { it.discountedPrice }
            SortOption.DATE_NEW_OLD   -> productList.sortedByDescending { it.createdAt }
            SortOption.DATE_OLD_NEW   -> productList.sortedBy { it.createdAt }
            SortOption.BEST_SELLING   -> productList.sortedByDescending { it.salesCount }
            SortOption.FEATURED,
            SortOption.MOST_RELEVANT  -> productList.toList()
        }

        displayList.clear()
        displayList.addAll(sorted)
        productAdapter.notifyDataSetChanged()

        binding.tvResultCount.text =
            if (sorted.size == 1) "1 product" else "${sorted.size} products"
        binding.emptyState.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
        binding.rvProducts.visibility = if (sorted.isEmpty()) View.GONE else View.VISIBLE
    }
}