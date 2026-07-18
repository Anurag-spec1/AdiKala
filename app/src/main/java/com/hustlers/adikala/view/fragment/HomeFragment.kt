package com.hustlers.adikala.view.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hustlers.adikala.view.activity.CartActivity
import com.hustlers.adikala.view.activity.CategoriesActivity
import com.hustlers.adikala.view.activity.ProductsActivity
import com.hustlers.adikala.view.activity.SearchActivity
import com.hustlers.adikala.view.activity.SubCategoriesActivity
import com.hustlers.adikala.view.adapter.CategoryAdapter
import com.hustlers.adikala.view.adapter.PopularCollectionAdapter
import com.hustlers.adikala.databinding.FragmentHomeBinding
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.model.PopularCollection
import com.hustlers.adikala.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var adikalaStoreAdapter: PopularCollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupUI()
        observeViewModel()
        viewModel.loadHomeData()
    }

    private fun setupUI() {
        binding.userName.setTypeface(null, Typeface.BOLD)
        binding.userName.text = "Anurag".toDisplayName()

        binding.viewallCategories.setOnClickListener {
            startActivity(Intent(requireContext(), CategoriesActivity::class.java))
        }
        binding.cart.setOnClickListener {
            startActivity(Intent(requireContext(), CartActivity::class.java))
        }

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvPopular.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            onCategoryClick(category)
        }
        binding.rvCategories.adapter = categoryAdapter

        adikalaStoreAdapter = PopularCollectionAdapter(emptyList()) { collection ->
            navigateToProducts(collection.name, collection.handle)
        }
        binding.rvPopular.adapter = adikalaStoreAdapter

        binding.etSearchBar.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("search_query", binding.etSearchBar.text.toString().trim())
            startActivity(intent)
        }

        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra("search_query", query)
                    startActivity(intent)
                }
                true
            } else false
        }
    }

    private fun onCategoryClick(category: Category) {
        if (category.subCategories.isNotEmpty()) {
            val intent = Intent(requireContext(), SubCategoriesActivity::class.java)
            intent.putExtra(SubCategoriesActivity.EXTRA_CATEGORY, category)
            startActivity(intent)
        } else {
            navigateToProducts(category.name, category.handle)
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (categories.isNotEmpty()) {
                categoryAdapter = CategoryAdapter(categories) { category ->
                    onCategoryClick(category)
                }
                binding.rvCategories.adapter = categoryAdapter
            }
        }

        viewModel.adikalaStoreCollections.observe(viewLifecycleOwner) { collections ->
            if (collections.isNotEmpty()) {
                val popularList = collections.map { cat ->
                    PopularCollection(id = cat.id, name = cat.name, image = cat.image, handle = cat.handle)
                }
                adikalaStoreAdapter = PopularCollectionAdapter(popularList) { collection ->
                    navigateToProducts(collection.name, collection.handle)
                }
                binding.rvPopular.adapter = adikalaStoreAdapter
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToProducts(name: String, handle: String) {
        val intent = Intent(requireContext(), ProductsActivity::class.java)
        intent.putExtra(ProductsActivity.EXTRA_CATEGORY, name)
        intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, handle)
        startActivity(intent)
    }

    private fun String.toDisplayName(maxLength: Int = 12): String {
        if (isBlank()) return ""
        val firstName = trim().split("\\s+".toRegex()).first()
        return if (firstName.length > maxLength) firstName.take(maxLength) + "..." else firstName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}