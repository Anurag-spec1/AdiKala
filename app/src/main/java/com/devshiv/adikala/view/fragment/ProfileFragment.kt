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
import androidx.recyclerview.widget.LinearLayoutManager
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.databinding.FragmentProfileBinding
import com.devshiv.adikala.model.Address
import com.devshiv.adikala.repository.CustomerRepository
import com.devshiv.adikala.view.adapter.AddressAdapter
import com.devshiv.adikala.viewmodel.ProfileViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authService: ShopifyAuthService
    private var addressListDialog: AlertDialog? = null

    private var userRequestedAddresses = false

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
        binding.btnManageAddress.setOnClickListener {
            userRequestedAddresses = true
            showAddressListDialog()
        }
        binding.btnHelpCenter.setOnClickListener { }
        binding.btnContactUs.setOnClickListener { }
        binding.btnPrivacyPolicy.setOnClickListener { }
        binding.btnTerms.setOnClickListener { }
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
                viewModel.updateProfile(
                    etFirstName.text.toString().trim(),
                    etLastName.text.toString().trim()
                )
            }
            .setNeutralButton("Change Email") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://account.adikala.in/profile")))
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showAddAddressDialog(existingAddress: Address?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_address, null)
        val etCountry = view.findViewById<TextInputEditText>(R.id.etCountry)
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

        AlertDialog.Builder(requireContext())
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

                val address = Address(
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
            .setNegativeButton("Cancel", null).show()
    }

    private fun showAddressListDialog() {
        viewModel.loadAddresses()
    }

    private fun refreshAddressListDialog() {
        addressListDialog?.dismiss()
        val addresses = viewModel.addresses.value ?: emptyList()

        if (addresses.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("My Addresses")
                .setMessage("You haven't added any addresses yet.\n\nDefault location: India")
                .setPositiveButton("Add Address") { _, _ ->
                    showAddAddressDialog(null)
                }
                .setNegativeButton("Close", null)
                .show()
            return
        }

        val view = layoutInflater.inflate(R.layout.dialog_address_list, null)
        val rvAddresses = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddresses)
        rvAddresses.layoutManager = LinearLayoutManager(requireContext())

        val adapter = AddressAdapter(
            addresses,
            onEdit = { address ->
                addressListDialog?.dismiss()
                showAddAddressDialog(address)
            },
            onDelete = { address ->
                if (addresses.size <= 1) {
                    Toast.makeText(requireContext(), "Cannot delete your only address. Add another address first.", Toast.LENGTH_LONG).show()
                } else if (address.isDefault) {
                    Toast.makeText(requireContext(), "Set another address as default first, then delete this one.", Toast.LENGTH_LONG).show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Address")
                        .setMessage("Are you sure you want to delete this address?")
                        .setPositiveButton("Delete") { _, _ -> viewModel.deleteAddress(address.id!!) }
                        .setNegativeButton("Cancel", null).show()
                }
            },
            onSetDefault = { address ->
                val updatedAddress = address.copy(isDefault = true)
                viewModel.updateAddress(address.id!!, updatedAddress)
            }
        )
        rvAddresses.adapter = adapter

        addressListDialog = AlertDialog.Builder(requireContext())
            .setTitle("My Addresses (${addresses.size})")
            .setView(view)
            .setPositiveButton("Add New") { _, _ ->
                addressListDialog?.dismiss()
                showAddAddressDialog(null)
            }
            .setNegativeButton("Close", null)
            .show()
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

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ProfileViewModel.AuthState.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                ProfileViewModel.AuthState.AUTHENTICATED -> {
                    binding.layoutLoggedIn.visibility = View.VISIBLE
                    binding.layoutLoggedOut.visibility = View.GONE
                }
                ProfileViewModel.AuthState.EXPIRED -> {
                    binding.layoutLoggedIn.visibility = View.GONE
                    binding.layoutLoggedOut.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                }
                ProfileViewModel.AuthState.LOGGED_OUT -> {
                    binding.layoutLoggedIn.visibility = View.GONE
                    binding.layoutLoggedOut.visibility = View.VISIBLE
                }
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { binding.tvUserName.text = it ?: "User" }
        viewModel.userEmail.observe(viewLifecycleOwner) { binding.tvUserEmail.text = it ?: "No email" }
        viewModel.userPhone.observe(viewLifecycleOwner) { binding.tvUserPhone.text = it ?: "No phone" }
        viewModel.userInitial.observe(viewLifecycleOwner) { binding.tvAvatarInitial.text = it ?: "U" }

        viewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            if (addresses != null && userRequestedAddresses) {
                userRequestedAddresses = false
                refreshAddressListDialog()
            }
        }

        viewModel.defaultAddress.observe(viewLifecycleOwner) { address ->
            binding.tvDefaultAddress.text = if (address.isNullOrEmpty() || address == "No address saved yet") {
                "India (Default)"
            } else {
                address
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.isOffline.observe(viewLifecycleOwner) { offline ->
            if (offline) {
                Toast.makeText(requireContext(), "You're offline. Showing cached data.", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() } }
        viewModel.successMessage.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addressListDialog?.dismiss()
        _binding = null
    }
}