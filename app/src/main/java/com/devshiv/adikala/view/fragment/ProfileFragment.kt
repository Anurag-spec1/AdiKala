package com.devshiv.adikala.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.databinding.FragmentProfileBinding
import com.devshiv.adikala.repository.CustomerRepository
import com.devshiv.adikala.viewmodel.ProfileViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authService: ShopifyAuthService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authService = ShopifyAuthService(requireContext())
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginStatus()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener { viewModel.launchLogin() }
        binding.btnCreateAccount.setOnClickListener { viewModel.launchLogin() }
        binding.btnSignOut.setOnClickListener { showSignOutDialog() }
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.btnTrackOrder.setOnClickListener { trackOrder() }
        binding.btnAddAddress.setOnClickListener { showAddAddressDialog(null) }
        binding.btnManageAddress.setOnClickListener { viewModel.loadAddresses() }
        binding.btnHelpCenter.setOnClickListener { showHelpCenter() }
        binding.btnContactUs.setOnClickListener { showContactUs() }
        binding.btnPrivacyPolicy.setOnClickListener { showPrivacyPolicy() }
        binding.btnTerms.setOnClickListener { showTerms() }
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ -> performSignOut() }
            .setNegativeButton("Cancel", null).show()
    }

    private fun performSignOut() {
        lifecycleScope.launch {
            try {
                authService.signOut()
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
                viewModel.checkLoginStatus()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val profile = viewModel.getProfileData()
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etFirstName = view.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = view.findViewById<TextInputEditText>(R.id.etLastName)

        etFirstName.setText(profile?.firstName ?: "")
        etLastName.setText(profile?.lastName ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val firstName = etFirstName.text.toString().trim()
                val lastName = etLastName.text.toString().trim()
                viewModel.updateProfile(firstName, lastName)
            }
            .setNeutralButton("Change Email") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://account.adikala.in/profile")))
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showAddAddressDialog(existingAddress: CustomerRepository.Address?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_address, null)
        val etCountry = view.findViewById<TextInputEditText>(R.id.etCountry)
        val etFirstName = view.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = view.findViewById<TextInputEditText>(R.id.etLastName)
        val etAddress = view.findViewById<TextInputEditText>(R.id.etAddress)
        val etApartment = view.findViewById<TextInputEditText>(R.id.etApartment)
        val etCity = view.findViewById<TextInputEditText>(R.id.etCity)
        val etState = view.findViewById<TextInputEditText>(R.id.etState)
        val etZip = view.findViewById<TextInputEditText>(R.id.etZip)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val cbDefault = view.findViewById<CheckBox>(R.id.cbDefault)

        etCountry.setText("India")

        existingAddress?.let {
            etAddress.setText(it.address1)
            etApartment.setText(it.address2)
            etCity.setText(it.city)
            etState.setText(it.state)
            etZip.setText(it.zip)
            etPhone.setText(it.phone)
            cbDefault.isChecked = it.isDefault
        }

        val isEdit = existingAddress != null
        val title = if (isEdit) "Edit Address" else "Add Address"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val phoneInput = etPhone.text.toString().trim()
                val formattedPhone = if (phoneInput.isNotEmpty()) {
                    if (phoneInput.startsWith("+")) phoneInput else "+91$phoneInput"
                } else ""

                val countryInput = etCountry.text.toString().trim()
                val countryCode = when {
                    countryInput.equals("India", true) -> "IN"
                    countryInput.length == 2 -> countryInput.uppercase()
                    else -> "IN"
                }

                val stateInput = etState.text.toString().trim()
                val stateCode = if (stateInput.length <= 2) stateInput.uppercase() else getZoneCode(stateInput)

                val address = CustomerRepository.Address(
                    id = existingAddress?.id,
                    address1 = etAddress.text.toString().trim(),
                    address2 = etApartment.text.toString().trim(),
                    city = etCity.text.toString().trim(),
                    country = countryCode,
                    state = stateCode,
                    zip = etZip.text.toString().trim(),
                    phone = formattedPhone,
                    isDefault = cbDefault.isChecked
                )
                if (isEdit) {
                    viewModel.updateAddress(existingAddress!!.id!!, address)
                } else {
                    viewModel.addAddress(address)
                }
            }
            .setNegativeButton("Cancel", null)

        if (isEdit) {
            dialog.setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Address")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteAddress(existingAddress!!.id!!)
                    }
                    .setNegativeButton("Cancel", null).show()
            }
        }

        dialog.show()
    }

    private fun getZoneCode(state: String): String {
        return when (state.uppercase().trim()) {
            "UTTAR PRADESH", "UTTARPRADESH", "UP" -> "UP"
            "MAHARASHTRA", "MH" -> "MH"
            "DELHI", "DL" -> "DL"
            "KARNATAKA", "KA" -> "KA"
            "TAMIL NADU", "TN" -> "TN"
            "GUJARAT", "GJ" -> "GJ"
            "RAJASTHAN", "RJ" -> "RJ"
            "WEST BENGAL", "WB" -> "WB"
            "ANDHRA PRADESH", "AP" -> "AP"
            "TELANGANA", "TS" -> "TS"
            "KERALA", "KL" -> "KL"
            "PUNJAB", "PB" -> "PB"
            "HARYANA", "HR" -> "HR"
            "BIHAR", "BR" -> "BR"
            "MADHYA PRADESH", "MP" -> "MP"
            "ODISHA", "OD" -> "OD"
            else -> state.take(2).uppercase()
        }
    }

    private fun trackOrder() {
        val orderId = binding.etOrderId.text.toString().trim()
        if (orderId.isEmpty()) {
            Toast.makeText(requireContext(), "Enter an order ID", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Tracking: $orderId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHelpCenter() {
        Toast.makeText(requireContext(), "Help center coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showContactUs() {
        Toast.makeText(requireContext(), "Contact us coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showPrivacyPolicy() {
        Toast.makeText(requireContext(), "Privacy policy coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showTerms() {
        Toast.makeText(requireContext(), "Terms coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            binding.layoutLoggedIn.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
            binding.layoutLoggedOut.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
        }
        viewModel.userName.observe(viewLifecycleOwner) { binding.tvUserName.text = it ?: "User" }
        viewModel.userEmail.observe(viewLifecycleOwner) { binding.tvUserEmail.text = it ?: "No email" }
        viewModel.userPhone.observe(viewLifecycleOwner) { binding.tvUserPhone.text = it ?: "No phone" }
        viewModel.userInitial.observe(viewLifecycleOwner) { binding.tvAvatarInitial.text = it ?: "U" }
        viewModel.defaultAddress.observe(viewLifecycleOwner) { binding.tvDefaultAddress.text = it ?: "No address saved yet" }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.errorMessage.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() } }
        viewModel.successMessage.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}