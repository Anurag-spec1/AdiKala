package com.hustlers.adikala.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hustlers.adikala.adapter.CategoryGridAdapter
import com.hustlers.adikala.databinding.ActivityCategoriesBinding
import com.hustlers.adikala.dataclass.Category
import com.hustlers.adikala.dataclass.SubCategory

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvCategories.layoutManager = LinearLayoutManager(this)

        loadCategories()
        render()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(
            listOf(
                Category(
                    1, "Temple Décor",
                    "https://www.adikala.in/cdn/shop/collections/pichwai.jpg?v=1737890053&width=1080",
                    24,
                    listOf(
                        SubCategory(101, "Toran","https://www.adikala.in/cdn/shop/collections/decoration.jpg?v=1674844832&width=1080",3),
                        SubCategory(102, "Bandhanwar","https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080",5),
                        SubCategory(103, "Pooja Thali","https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080",7),
                        SubCategory(104, "Diya","https://www.adikala.in/cdn/shop/collections/pichwai.jpg?v=1737890053&width=1080",9),
                        SubCategory(105, "Mandir","https://www.adikala.in/cdn/shop/collections/decoration.jpg?v=1674844832&width=1080",11)
                    )
                ),
                Category(
                    2, "Decoration",
                    "https://www.adikala.in/cdn/shop/collections/decoration.jpg?v=1674844832&width=1080",
                    38,
                    listOf(
                        SubCategory(201, "Wall Hanging"),
                        SubCategory(202, "Backdrop"),
                        SubCategory(203, "Rangoli")
                    )
                ),
                Category(
                    3, "Wooden Beads",
                    "https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080",
                    16,
                    listOf(
                        SubCategory(301, "Round Beads"),
                        SubCategory(302, "Flat Beads")
                    )
                ),
                Category(
                    4, "DIY Bases",
                    "https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080",
                    42,
                    listOf(
                        SubCategory(401, "MDF Cutouts"),
                        SubCategory(402, "Round Base"),
                        SubCategory(403, "Square Base"),
                        SubCategory(404, "Keychain Base")
                    )
                )
            )
        )
    }

    private fun render() {
        if (categories.isEmpty()) {
            binding.rvCategories.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            return
        }
        binding.rvCategories.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        binding.rvCategories.adapter = CategoryGridAdapter(
            categories,
            onCategoryClick = { category ->
                if (category.subCategories.isNotEmpty()) {
                    val i = Intent(this, SubCategoriesActivity::class.java)
                    i.putExtra(SubCategoriesActivity.EXTRA_CATEGORY, category)
                    startActivity(i)
                } else {
                    val i = Intent(this, ProductsActivity::class.java)
                    i.putExtra(ProductsActivity.EXTRA_CATEGORY, category.name)
                    startActivity(i)
                }
            },
            onSubCategoryClick = { category, sub ->
                val i = Intent(this, ProductsActivity::class.java)
                i.putExtra(ProductsActivity.EXTRA_CATEGORY, "${category.name} · ${sub.name}")
                startActivity(i)
            }
        )
    }
}