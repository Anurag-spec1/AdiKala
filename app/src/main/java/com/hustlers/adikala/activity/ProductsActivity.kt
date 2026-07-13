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
                Product(
                    "1",
                    "Brass Diya Set",
                    "Handcrafted Brass Diya Set with Intricate Detailing",
                    "https://www.adikala.in/collections/buy-best-quality-imported-wooden-beads-for-jewelery-bracelet-amp-making-decorative-diy/products/multi-colored-wooden-beads-big-size-1cm",
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
                    "https://www.adikala.in/collections/wooden-miniatures/products/multi-color-parrot-wooden-miniature-small-size",
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
                    "https://www.adikala.in/collections/wooden-miniatures/products/raja-rani-wooden-miniature",
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
                    "https://www.adikala.in/collections/laces-borders/products/golden-drop-shape-bead-lace-border-decorative-trim-for-saree-blouse-dupatta-lehenga-diy-craft-work",
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
                    "https://www.adikala.in/collections/laces-borders/products/multicolor-plastic-cowrie-shell-lace-border-with-white-pearl-beads-latkan-decorative-trim-for-blouse-dupatta-lehenga-saree-diy-craft-work",
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
                    "https://www.adikala.in/collections/fancy-buttons/products/round-rani-lotus-buttons-set-of-10-for-suit-saree-diy-decorative-buttons",
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
                    "https://www.adikala.in/collections/fancy-buttons/products/lotus-shape-metal-jewelry-connector-pack-of-10-diy-jewelry-making-craft-supplies",
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
                    "https://www.adikala.in/collections/traditional-indian-latkan-tassels/products/neon-blue-beads-long-tassel-pack-of-10-diy-art-craft-festive-decoration-tassels",
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
                    "https://www.adikala.in/collections/traditional-indian-latkan-tassels/products/adikala-cowrie-parrot-lattu-decorative-tassel-pack-of-10-diy-craft-festive-decoration-tassels",
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
                    "https://www.adikala.in/collections/decoration-essentials/products/acrylic-flower-d2-pack-of-10-for-wedding-festive-decoration-diy-art-craft",
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
                    "https://www.adikala.in/collections/decoration-essentials/products/white-raffia-ring-pack-of-10-for-wedding-festive-decoration-diy-art-craft",
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
                    "https://www.adikala.in/collections/pichwai-art/products/peacock-printed-mdf-cutout-pack-of-4-diy-decoration-toran-bandhanwar-6-inch",
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