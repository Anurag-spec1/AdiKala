package com.hustlers.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hustlers.adikala.view.adapter.CategoryGridAdapter
import com.hustlers.adikala.databinding.ActivityCategoriesBinding
import com.hustlers.adikala.viewmodel.CategoriesViewModel

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var viewModel: CategoriesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]
        binding.rvCategories.layoutManager = LinearLayoutManager(this)

        viewModel.categories.observe(this) { categories ->
            if (categories.isEmpty()) {
                binding.rvCategories.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.rvCategories.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                binding.rvCategories.adapter = CategoryGridAdapter(
                    categories,
                    onCategoryClick = { category ->
                        if (category.subCategories.isNotEmpty()) {
                            val intent = Intent(this, SubCategoriesActivity::class.java)
                            intent.putExtra(SubCategoriesActivity.EXTRA_CATEGORY, category)
                            startActivity(intent)
                        } else {
                            navigateToProducts(category.name, category.handle)
                        }
                    },
                    onSubCategoryClick = { _, sub ->
                        navigateToProducts(sub.name, sub.handle)
                    }
                )
            }
        }

        viewModel.loadCategories()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun navigateToProducts(name: String, handle: String) {
        val intent = Intent(this, ProductsActivity::class.java)
        intent.putExtra(ProductsActivity.EXTRA_CATEGORY, name)
        intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, handle)
        startActivity(intent)
    }
}