// HomeFragment.kt - UPDATED
package com.hustlers.adikala.view.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hustlers.adikala.view.activity.CartActivity
import com.hustlers.adikala.view.activity.CategoriesActivity
import com.hustlers.adikala.view.activity.ProductsActivity
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
    private lateinit var popularAdapter: PopularCollectionAdapter

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

        binding.rvPopular.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            openProducts(category.name)
        }
        binding.rvCategories.adapter = categoryAdapter

        popularAdapter = PopularCollectionAdapter(emptyList()) { collection ->
            openProducts(collection.name)
        }
        binding.rvPopular.adapter = popularAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (categories.isNotEmpty()) {
                categoryAdapter = CategoryAdapter(categories) { category ->
                    openProducts(category.name)
                }
                binding.rvCategories.adapter = categoryAdapter
            }
        }

        viewModel.popularCollections.observe(viewLifecycleOwner) { collections ->
            if (collections.isNotEmpty()) {
                popularAdapter = PopularCollectionAdapter(collections) { collection ->
                    openProducts(collection.name)
                }
                binding.rvPopular.adapter = popularAdapter
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openProducts(categoryName: String) {
        val intent = Intent(requireContext(), ProductsActivity::class.java)
        intent.putExtra(ProductsActivity.EXTRA_CATEGORY, categoryName)
        startActivity(intent)
    }

    private fun String.toDisplayName(maxLength: Int = 12): String {
        if (isBlank()) return ""
        val firstName = trim().split("\\s+".toRegex()).first()
        return if (firstName.length > maxLength) {
            firstName.take(maxLength) + "..."
        } else {
            firstName
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}