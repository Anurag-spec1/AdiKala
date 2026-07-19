package com.hustlers.adikala.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hustlers.adikala.databinding.FragmentProfileBinding
import com.hustlers.adikala.view.activity.LoginActivity
import com.hustlers.adikala.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginStatus()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnTrackOrder.setOnClickListener {
            val orderId = binding.etOrderId.text.toString().trim()
            if (orderId.isNotEmpty()) {
                trackOrder(orderId)
            } else {
                Toast.makeText(requireContext(), "Enter an order ID", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddAddress.setOnClickListener {
            Toast.makeText(requireContext(), "Add address coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnManageAddress.setOnClickListener {
            Toast.makeText(requireContext(), "Manage addresses coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelpCenter.setOnClickListener {
            Toast.makeText(requireContext(), "Help center coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnContactUs.setOnClickListener {
            Toast.makeText(requireContext(), "Contact us coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy policy coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnTerms.setOnClickListener {
            Toast.makeText(requireContext(), "Terms coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                binding.layoutLoggedIn.visibility = View.VISIBLE
                binding.layoutLoggedOut.visibility = View.GONE
            } else {
                binding.layoutLoggedIn.visibility = View.GONE
                binding.layoutLoggedOut.visibility = View.VISIBLE
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = name ?: "User"
        }

        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.tvUserEmail.text = email ?: "No email"
        }

        viewModel.userPhone.observe(viewLifecycleOwner) { phone ->
            binding.tvUserPhone.text = phone ?: "No phone"
        }

        viewModel.userInitial.observe(viewLifecycleOwner) { initial ->
            binding.tvAvatarInitial.text = initial ?: "U"
        }

        viewModel.defaultAddress.observe(viewLifecycleOwner) { address ->
            binding.tvDefaultAddress.text = address ?: "No address saved yet"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun trackOrder(orderId: String) {
        Toast.makeText(requireContext(), "Tracking: $orderId", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}