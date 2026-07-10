package com.hustlers.adikala.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hustlers.adikala.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding?=null
    private val binding get()=_binding!!

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
        binding.userName.text = "Christopherkumar".toDisplayName()
        binding.rvPopular.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

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