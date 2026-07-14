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
                        id = "brass_diya_01",
                        name = "Handcrafted Brass Diya Set",
                        description = "Premium handcrafted brass diya set for pooja and festive décor.",
                        imageUrl = "https://picsum.photos/seed/brass_diya_01/800/800",
                        actualPrice = 999,
                        discountedPrice = 799,
                        rating = 4.8,
                        images = listOf(
                            "https://picsum.photos/seed/brass_diya_01/800/800",
                            "https://picsum.photos/seed/brass_diya_01_2/800/800"
                        ),
                        fullDescription = "Beautiful handcrafted brass diya set designed for daily worship, festivals and traditional home décor. Made from premium brass with an elegant finish.",
                        features = listOf(
                            "Premium quality brass",
                            "Traditional handcrafted design",
                            "Perfect for pooja & festivals",
                            "Long-lasting finish",
                            "Easy to clean",
                            "Ideal gifting option"
                        )
                    ),

                    Product(
                        id = "wooden_beads_01",
                        name = "Multi Colored Wooden Beads",
                        description = "Colorful wooden beads for DIY jewelry and craft projects.",
                        imageUrl = "https://picsum.photos/seed/wooden_beads_01/800/800",
                        actualPrice = 499,
                        discountedPrice = 349,
                        rating = 4.7,
                        images = listOf(
                            "https://picsum.photos/seed/wooden_beads_01/800/800",
                            "https://picsum.photos/seed/wooden_beads_01_2/800/800"
                        ),
                        fullDescription = "Bright multi-colored wooden beads suitable for bracelets, necklaces, keychains and DIY decorative craft projects.",
                        features = listOf(
                            "Premium polished wooden beads",
                            "Ideal for DIY jewelry",
                            "Smooth finish",
                            "Reusable",
                            "Lightweight",
                            "Great for hobby crafts"
                        )
                    ),

                    Product(
                        id = "wooden_parrot_01",
                        name = "Wooden Parrot Miniature",
                        description = "Handcrafted colorful wooden parrot decorative miniature.",
                        imageUrl = "https://picsum.photos/seed/wooden_parrot_01/800/800",
                        actualPrice = 699,
                        discountedPrice = 549,
                        rating = 4.6,
                        images = listOf(
                            "https://picsum.photos/seed/wooden_parrot_01/800/800",
                            "https://picsum.photos/seed/wooden_parrot_01_2/800/800"
                        ),
                        fullDescription = "Beautiful hand-painted wooden parrot miniature perfect for showcases and traditional home décor.",
                        features = listOf(
                            "Hand painted",
                            "Premium wood",
                            "Traditional artwork",
                            "Gift worthy",
                            "Home décor"
                        )
                    ),

                    Product(
                        id = "lace_border_01",
                        name = "Golden Drop Lace Border",
                        description = "Decorative golden lace trim for sarees and DIY crafts.",
                        imageUrl = "https://picsum.photos/seed/lace_border_01/800/800",
                        actualPrice = 699,
                        discountedPrice = 549,
                        rating = 4.9,
                        images = listOf(
                            "https://picsum.photos/seed/lace_border_01/800/800",
                            "https://picsum.photos/seed/lace_border_01_2/800/800"
                        ),
                        fullDescription = "Elegant golden bead lace border for ethnic wear and handmade decoration.",
                        features = listOf(
                            "Premium decorative lace",
                            "Golden finish",
                            "DIY compatible",
                            "Easy stitching",
                            "Traditional design"
                        )
                    ),

                    Product(
                        id = "cowrie_lace_01",
                        name = "Cowrie Shell Lace Border",
                        description = "Decorative cowrie shell lace with pearl beads.",
                        imageUrl = "https://picsum.photos/seed/cowrie_lace_01/800/800",
                        actualPrice = 899,
                        discountedPrice = 649,
                        rating = 4.7,
                        images = listOf(
                            "https://picsum.photos/seed/cowrie_lace_01/800/800",
                            "https://picsum.photos/seed/cowrie_lace_01_2/800/800"
                        ),
                        fullDescription = "Beautiful decorative lace with cowrie shells and pearls for festive decoration.",
                        features = listOf(
                            "Cowrie shell design",
                            "Pearl finish",
                            "Traditional décor",
                            "Flexible trim",
                            "Festive use"
                        )
                    ),

                    Product(
                        id = "lotus_button_01",
                        name = "Lotus Decorative Buttons",
                        description = "Round lotus decorative buttons for sewing and DIY.",
                        imageUrl = "https://picsum.photos/seed/lotus_button_01/800/800",
                        actualPrice = 399,
                        discountedPrice = 299,
                        rating = 4.5,
                        images = listOf(
                            "https://picsum.photos/seed/lotus_button_01/800/800",
                            "https://picsum.photos/seed/lotus_button_01_2/800/800"
                        ),
                        fullDescription = "Elegant lotus themed decorative buttons for suits, blouses and handmade craft.",
                        features = listOf(
                            "Metal finish",
                            "Pack of buttons",
                            "DIY sewing",
                            "Durable",
                            "Craft friendly"
                        )
                    ),

                    Product(
                        id = "connector_01",
                        name = "Lotus Jewelry Connector",
                        description = "Decorative metal connectors for jewelry making.",
                        imageUrl = "https://picsum.photos/seed/connector_01/800/800",
                        actualPrice = 599,
                        discountedPrice = 449,
                        rating = 4.6,
                        images = listOf(
                            "https://picsum.photos/seed/connector_01/800/800",
                            "https://picsum.photos/seed/connector_01_2/800/800"
                        ),
                        fullDescription = "High-quality lotus shaped metal connectors for earrings, necklaces and bracelets.",
                        features = listOf(
                            "Metal alloy",
                            "Rust resistant",
                            "Jewelry making",
                            "Premium finish",
                            "Reusable"
                        )
                    ),

                    Product(
                        id = "tassel_01",
                        name = "Decorative Tassel Pack",
                        description = "Traditional handmade tassels for festive decoration.",
                        imageUrl = "https://picsum.photos/seed/tassel_01/800/800",
                        actualPrice = 799,
                        discountedPrice = 599,
                        rating = 4.8,
                        images = listOf(
                            "https://picsum.photos/seed/tassel_01/800/800",
                            "https://picsum.photos/seed/tassel_01_2/800/800"
                        ),
                        fullDescription = "Colorful handmade tassels suitable for torans, bandhanwars and festive decorations.",
                        features = listOf(
                            "Pack of tassels",
                            "Bright colors",
                            "Handmade",
                            "Festival décor",
                            "Easy to attach"
                        )
                    ),

                    Product(
                        id = "wall_hanging_01",
                        name = "Traditional Wall Hanging",
                        description = "Beautiful handcrafted wall hanging for home décor.",
                        imageUrl = "https://picsum.photos/seed/wall_hanging_01/800/800",
                        actualPrice = 1299,
                        discountedPrice = 999,
                        rating = 4.7,
                        images = listOf(
                            "https://picsum.photos/seed/wall_hanging_01/800/800",
                            "https://picsum.photos/seed/wall_hanging_01_2/800/800"
                        ),
                        fullDescription = "Traditional handcrafted wall hanging to enhance your living room or pooja room décor.",
                        features = listOf(
                            "Handcrafted",
                            "Traditional artwork",
                            "Easy hanging",
                            "Premium finish",
                            "Gift option"
                        )
                    ),

                    Product(
                        id = "peacock_cutout_01",
                        name = "Peacock MDF Cutout",
                        description = "Printed MDF cutout for DIY decoration and craft.",
                        imageUrl = "https://picsum.photos/seed/peacock_cutout_01/800/800",
                        actualPrice = 699,
                        discountedPrice = 499,
                        rating = 4.8,
                        images = listOf(
                            "https://picsum.photos/seed/peacock_cutout_01/800/800",
                            "https://picsum.photos/seed/peacock_cutout_01_2/800/800"
                        ),
                        fullDescription = "Decorative peacock printed MDF cutout suitable for toran, bandhanwar, wall hanging and DIY festive projects.",
                        features = listOf(
                            "Pack of 4",
                            "6 inch size",
                            "Printed MDF",
                            "DIY decoration",
                            "Festive décor"
                        )
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