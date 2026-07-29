package com.devshiv.adikala.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.devshiv.adikala.api.ShopifyApiService
import com.devshiv.adikala.view.adapter.SubCategoryGridAdapter
import com.devshiv.adikala.databinding.ActivitySubcategoriesBinding
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.model.SubCategory
import kotlinx.coroutines.*

class SubCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubcategoriesBinding
    private lateinit var category: Category
    private val apiService = ShopifyApiService()
    private var adapter: SubCategoryGridAdapter? = null

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

        adapter = SubCategoryGridAdapter(subs) { sub ->
            val intent = Intent(this, ProductsActivity::class.java)
            intent.putExtra(ProductsActivity.EXTRA_CATEGORY, "${category.name} · ${sub.name}")
            intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, sub.handle)
            startActivity(intent)
        }
        binding.rvSubCategories.adapter = adapter

        loadImagesInParallel(subs)
    }

    private fun loadImagesInParallel(subs: List<SubCategory>) {
        lifecycleScope.launch {
            val updatedSubs = withContext(Dispatchers.IO) {
                coroutineScope {
                    subs.map { sub ->
                        async {
                            sub.takeIf { it.image.isNotEmpty() } ?: fetchImageForSub(sub)
                        }
                    }.awaitAll()
                }
            }

            adapter?.updateList(updatedSubs)
        }
    }

    private suspend fun fetchImageForSub(sub: SubCategory): SubCategory {
        if (sub.handle.isEmpty()) return sub
        return try {
            val data = apiService.getCollectionByHandle(sub.handle, first = 1)
            val imageUrl = data.collectionByHandle?.products?.edges
                ?.firstOrNull()?.node?.images?.edges
                ?.firstOrNull()?.node?.url ?: ""
            sub.copy(image = imageUrl)
        } catch (e: Exception) {
            sub
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
    }
}