package com.hustlers.adikala.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.hustlers.adikala.adapter.SubCategoryGridAdapter
import com.hustlers.adikala.databinding.ActivitySubcategoriesBinding
import com.hustlers.adikala.dataclass.Category
import com.hustlers.adikala.dataclass.SubCategory

class SubCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubcategoriesBinding
    private lateinit var category: Category

    companion object {
        const val EXTRA_CATEGORY = "extra_category_obj"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubcategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = (intent.getSerializableExtra(EXTRA_CATEGORY) as? Category) ?: run {
            finish()
            return
        }

        bindHeader()
        setupGrid()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun bindHeader() {
        binding.tvCategoryName.text = category.name

        val subCount = category.subCategories.size
        val subText = if (subCount == 1) "1 subcategory" else "$subCount subcategories"
        binding.tvSubtitle.text =
            if (category.itemCount > 0) "$subText · ${category.itemCount} items" else subText

        Glide.with(binding.ivBanner)
            .load(category.image)
            .centerCrop()
            .into(binding.ivBanner)
    }

    private fun setupGrid() {
        val subs = category.subCategories
        if (subs.isEmpty()) {
            binding.rvSubCategories.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            return
        }
        binding.rvSubCategories.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        binding.rvSubCategories.layoutManager = GridLayoutManager(this, 2)
        binding.rvSubCategories.adapter = SubCategoryGridAdapter(subs) { sub ->
            val intent = Intent(this, ProductsActivity::class.java)
            intent.putExtra(ProductsActivity.EXTRA_CATEGORY, "${category.name} · ${sub.name}")
            startActivity(intent)
        }
    }
}