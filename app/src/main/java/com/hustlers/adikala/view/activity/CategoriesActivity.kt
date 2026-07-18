package com.hustlers.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hustlers.adikala.view.adapter.CategoryGridAdapter
import com.hustlers.adikala.databinding.ActivityCategoriesBinding
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.model.SubCategory
import com.hustlers.adikala.viewmodel.CategoriesViewModel

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var adapter: CategoryGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]

        binding.rvCategories.layoutManager = LinearLayoutManager(this)

        setupObservers()
        viewModel.loadCategories()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.categories.observe(this) { categories ->
            if (categories.isNotEmpty()) {
                renderCategories(categories)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvCategories.visibility = View.GONE
            }
        }
    }

    private fun renderCategories(categories: List<Category>) {
        if (categories.isEmpty()) {
            binding.rvCategories.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            return
        }

        binding.rvCategories.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        adapter = CategoryGridAdapter(
            categories,
            onCategoryClick = { category ->
                viewModel.loadCategoryWithSubCategories(category.handle)
                category.handle.let { handle ->
                    viewModel.selectedCategory.observe(this) { categoryWithSubs ->
                        if (categoryWithSubs.subCategories.isNotEmpty()) {
                            val intent = Intent(this, SubCategoriesActivity::class.java)
                            intent.putExtra(SubCategoriesActivity.EXTRA_CATEGORY, categoryWithSubs)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, ProductsActivity::class.java)
                            intent.putExtra(ProductsActivity.EXTRA_CATEGORY, category.name)
                            intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, category.handle)
                            startActivity(intent)
                        }
                    }
                }
            },
            onSubCategoryClick = { category, sub ->
                val intent = Intent(this, ProductsActivity::class.java)
                intent.putExtra(ProductsActivity.EXTRA_CATEGORY, "${category.name} · ${sub.name}")
                intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, category.handle)
                intent.putExtra(ProductsActivity.EXTRA_SUBCATEGORY, sub.name)
                startActivity(intent)
            }
        )

        binding.rvCategories.adapter = adapter
    }
}