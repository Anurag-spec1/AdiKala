package com.devshiv.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.devshiv.adikala.view.adapter.SubCategoryGridAdapter
import com.devshiv.adikala.databinding.ActivitySubcategoriesBinding
import com.devshiv.adikala.model.Category

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

        showShimmer()
        bindHeader()
        setupGrid()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun showShimmer() {
        binding.shimmerSubCategories.visibility = View.VISIBLE
        binding.shimmerSubCategories.startShimmer()

        binding.ivBanner.postDelayed({
            binding.shimmerSubCategories.stopShimmer()
            binding.shimmerSubCategories.visibility = View.GONE
        }, 800)
    }

    private fun bindHeader() {
        binding.tvCategoryName.text = category.name
        val subCount = category.subCategories.size
        val itemCount = category.itemCount
        val subText = if (subCount == 1) "1 subcategory" else "$subCount subcategories"
        binding.tvSubtitle.text = if (itemCount > 0) "$subText · $itemCount items" else subText
        Glide.with(binding.ivBanner).load(category.image).centerCrop().into(binding.ivBanner)
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
            intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, sub.handle)
            startActivity(intent)
        }
    }
}