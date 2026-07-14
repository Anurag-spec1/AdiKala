package com.hustlers.adikala.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hustlers.adikala.activity.CartActivity
import com.hustlers.adikala.activity.CategoriesActivity
import com.hustlers.adikala.activity.ProductsActivity
import com.hustlers.adikala.adapter.CategoryAdapter
import com.hustlers.adikala.adapter.PopularCollectionAdapter
import com.hustlers.adikala.databinding.FragmentHomeBinding
import com.hustlers.adikala.dataclass.Category
import com.hustlers.adikala.dataclass.PopularCollection

class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding?=null
    private val binding get()=_binding!!
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var popularAdapter: PopularCollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding= FragmentHomeBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val categories = listOf(
            Category(1, "Temple","https://www.adikala.in/cdn/shop/collections/pichwai.jpg?v=1737890053&width=1080"),
            Category(2, "Beach","https://www.adikala.in/cdn/shop/collections/decoration.jpg?v=1674844832&width=1080"),
            Category(3, "Hill","https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080"),
            Category(4, "Lake","https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080")
        )

        categoryAdapter = CategoryAdapter(categories) {

            Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()

        }

        binding.rvCategories.adapter = categoryAdapter


        val popular = listOf(

            PopularCollection(
                1,
                "Pichwai Art",
                "https://www.adikala.in/cdn/shop/collections/pichwai.jpg?v=1737890053&width=1080"
            ),

            PopularCollection(
                2,
                "Decorative Essentials",
                "https://www.adikala.in/cdn/shop/collections/decoration.jpg?v=1674844832&width=1080"
            ),

            PopularCollection(
                3,
                "Wooden Beads",
                "https://www.adikala.in/cdn/shop/products/657_cdb4596c-0ceb-47dc-9652-09be375b15c0.jpg?v=1657903140&width=1080"
            ),

            PopularCollection(
                4,
                "MDF Bases",
                "https://www.adikala.in/cdn/shop/collections/for-DIY.jpg?v=1674845390&width=1080"
            )
        )

        popularAdapter = PopularCollectionAdapter(popular) { collection ->
            openProducts(collection.name)
        }

        binding.rvPopular.adapter = popularAdapter

    }

    fun String.toDisplayName(maxLength: Int = 12): String {
        if (isBlank()) return ""

        val firstName = trim().split("\\s+".toRegex()).first()

        return if (firstName.length > maxLength) {
            firstName.take(maxLength) + "..."
        } else {
            firstName
        }
    }

    private fun openProducts(categoryName: String) {
        val intent = Intent(requireContext(), ProductsActivity::class.java)
        intent.putExtra(ProductsActivity.EXTRA_CATEGORY, categoryName)
        startActivity(intent)
    }


}