package com.hustlers.adikala.fragment

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
        binding.rvPopular.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val categories = listOf(
            Category(1, "Temple"),
            Category(2, "Beach"),
            Category(3, "Hill"),
            Category(4, "Lake"),
            Category(5, "Fort")
        )

        categoryAdapter = CategoryAdapter(categories) {

            Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()

        }

        binding.rvCategories.adapter = categoryAdapter


        val popular = listOf(

            PopularCollection(
                1,
                "Goa",
                "https://picsum.photos/400/500"
            ),

            PopularCollection(
                2,
                "Varanasi",
                "https://picsum.photos/400/650"
            ),

            PopularCollection(
                3,
                "Jaipur",
                "https://picsum.photos/400/550"
            ),

            PopularCollection(
                4,
                "Rishikesh",
                "https://picsum.photos/400/700"
            )
        )

        popularAdapter = PopularCollectionAdapter(popular) {

            Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()

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


}