package com.hustlers.adikala.activity

import android.content.Intent
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
                val i = Intent(this, ProductDetailActivity::class.java)
                i.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                startActivity(i)
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
                Product(
                    "1",
                    "Brass Diya Set",
                    "Handcrafted Brass Diya Set with Intricate Detailing",
                    "https://picsum.photos/seed/brass-diya/600/600",
                    999,
                    799,
                    4.8,
                    320,
                    1_720_000_000_000,
                    false
                ),

                Product(
                    "2",
                    "Wooden Temple",
                    "Solid Sheesham Wood Home Temple with Elegant Carving",
                    "https://picsum.photos/seed/wooden-temple/600/600",
                    5499,
                    4299,
                    4.7,
                    148,
                    1_719_000_000_000,
                    false
                ),

                Product(
                    "3",
                    "Decorative Wooden Beads",
                    "Premium Multi-Colored Wooden Beads for DIY Crafts",
                    "https://picsum.photos/seed/wooden-beads/600/600",
                    499,
                    349,
                    4.6,
                    189,
                    1_718_000_000_000,
                    false
                ),

                Product(
                    "4",
                    "Brass Pooja Bell",
                    "Traditional Brass Hand Bell for Daily Worship",
                    "https://picsum.photos/seed/brass-bell/600/600",
                    699,
                    549,
                    4.9,
                    278,
                    1_717_000_000_000,
                    false
                ),

                Product(
                    "5",
                    "Wooden Elephant",
                    "Handcrafted Wooden Elephant Decorative Showpiece",
                    "https://picsum.photos/seed/wooden-elephant/600/600",
                    899,
                    649,
                    4.5,
                    135,
                    1_716_000_000_000,
                    false
                ),

                Product(
                    "6",
                    "Marble Incense Holder",
                    "Elegant Marble Agarbatti Stand for Home Temple",
                    "https://picsum.photos/seed/incense-holder/600/600",
                    399,
                    299,
                    4.4,
                    102,
                    1_715_500_000_000,
                    false
                ),

                Product(
                    "7",
                    "Wooden Jewelry Box",
                    "Decorative Wooden Jewelry Storage Box",
                    "https://picsum.photos/seed/jewelry-box/600/600",
                    1499,
                    1199,
                    4.8,
                    256,
                    1_715_000_000_000,
                    false
                ),

                Product(
                    "8",
                    "Brass Candle Holder",
                    "Vintage Brass Candle Holder for Home Decor",
                    "https://picsum.photos/seed/candle-holder/600/600",
                    799,
                    599,
                    4.7,
                    176,
                    1_714_500_000_000,
                    false
                ),

                Product(
                    "9",
                    "Decorative Wall Hanging",
                    "Traditional Handmade Wooden Wall Hanging",
                    "https://picsum.photos/seed/wall-hanging/600/600",
                    1299,
                    999,
                    4.6,
                    211,
                    1_714_000_000_000,
                    false
                ),

                Product(
                    "10",
                    "Wooden Key Holder",
                    "Elegant Wooden Key Holder with Hooks",
                    "https://picsum.photos/seed/key-holder/600/600",
                    599,
                    449,
                    4.5,
                    95,
                    1_713_500_000_000,
                    false
                ),

                Product(
                    "11",
                    "Brass Urli Bowl",
                    "Decorative Brass Urli Bowl for Floating Flowers",
                    "https://picsum.photos/seed/urli-bowl/600/600",
                    1999,
                    1599,
                    4.9,
                    342,
                    1_713_000_000_000,
                    false
                ),

                Product(
                    "12",
                    "Wooden Photo Frame",
                    "Classic Wooden Photo Frame for Home Decor",
                    "https://picsum.photos/seed/photo-frame/600/600",
                    699,
                    499,
                    4.6,
                    163,
                    1_712_500_000_000,
                    false
                )
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