package com.devshiv.adikala.view.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
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
import com.devshiv.adikala.utils.IndianStates
import com.devshiv.adikala.view.adapter.AddressAdapter
import com.devshiv.adikala.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authService: ShopifyAuthService
    private var addressListDialog: AlertDialog? = null

    private var userRequestedAddresses = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                viewModel.logout()
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT)
                    .show()
                viewModel.checkLoginStatus()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val profile = viewModel.getProfileData()
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etFirstName = view.findViewById<EditText>(R.id.etFirstName)
        val etLastName = view.findViewById<EditText>(R.id.etLastName)
        val btnCancel = view.findViewById<View>(R.id.btnCancelProfile)
        val btnSave = view.findViewById<View>(R.id.btnSaveProfile)

        etFirstName.setText(profile?.firstName ?: "")
        etLastName.setText(profile?.lastName ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val first = etFirstName.text.toString().trim()
            val last = etLastName.text.toString().trim()
            if (first.isEmpty() || last.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter first and last name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.updateProfile(first, last)
            dialog.dismiss()
        }

        dialog.show()
    }

   private fun showAddAddressDialog(existingAddress: Address?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_address, null)
        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val etFirstName = view.findViewById<EditText>(R.id.etFirstName)
        val etLastName = view.findViewById<EditText>(R.id.etLastName)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etApartment = view.findViewById<EditText>(R.id.etApartment)
        val etCity = view.findViewById<EditText>(R.id.etCity)
        val etState = view.findViewById<AutoCompleteTextView>(R.id.etState)
        val etZip = view.findViewById<EditText>(R.id.etZip)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val cbDefault = view.findViewById<CheckBox>(R.id.cbDefault)
        val btnCancel = view.findViewById<View>(R.id.btnCancelAddress)
        val btnSave = view.findViewById<View>(R.id.btnSaveAddress)

        val isEdit = existingAddress != null
        tvTitle.text = if (isEdit) "Edit Address" else "Add Address"

        val stateAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown_state,
            R.id.tvDropdownItem,
            IndianStates.names
        )
        etState.setAdapter(stateAdapter)
        etState.setOnClickListener { etState.showDropDown() }

        existingAddress?.let {
            etFirstName.setText(it.firstName)
            etLastName.setText(it.lastName)
            etAddress.setText(it.address1)
            etApartment.setText(it.address2)
            etCity.setText(it.city)
            val savedState = it.state ?: ""
            etState.setText(IndianStates.nameForCode(savedState) ?: savedState, false)
            etZip.setText(it.zip)
            etPhone.setText(it.phone)
            cbDefault.isChecked = it.isDefault
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val address1 = etAddress.text.toString().trim()
            val city = etCity.text.toString().trim()
            val stateName = etState.text.toString().trim()
            val zip = etZip.text.toString().trim()
            val phoneInput = etPhone.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter first and last name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (address1.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (city.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your city", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val stateCode = IndianStates.codeForName(stateName)
            if (stateCode == null) {
                Toast.makeText(
                    requireContext(),
                    "Please select a valid state from the list",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!zip.matches(Regex("^[1-9][0-9]{5}$"))) {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid 6-digit PIN code",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val digitsOnly = phoneInput.replace(Regex("[^0-9]"), "")
            val normalizedNumber = when {
                digitsOnly.length == 10 -> digitsOnly
                digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.substring(2)
                digitsOnly.length == 11 && digitsOnly.startsWith("0") -> digitsOnly.substring(1)
                else -> digitsOnly
            }
            if (!normalizedNumber.matches(Regex("^[6-9][0-9]{9}$"))) {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid 10-digit Indian mobile number",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val formattedPhone = "+91$normalizedNumber"

            val address = Address(
                id = existingAddress?.id,
                firstName = firstName,
                lastName = lastName,
                address1 = address1,
                address2 = etApartment.text.toString().trim(),
                city = city,
                country = "IN",
                state = stateCode,
                zip = zip,
                phone = formattedPhone,
                isDefault = cbDefault.isChecked
            )

            if (isEdit) {
                viewModel.updateAddress(existingAddress!!.id!!, address)
            } else {
                viewModel.addAddress(address)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddressListDialog() {
        viewModel.loadAddresses()
    }

    private fun refreshAddressListDialog() {
        addressListDialog?.dismiss()
        val addresses = viewModel.addresses.value ?: emptyList()

        if (addresses.isEmpty()) {
            val emptyView = layoutInflater.inflate(R.layout.dialog_address_empty, null)
            val emptyDialog = AlertDialog.Builder(requireContext())
                .setView(emptyView)
                .create()
            emptyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            emptyView.findViewById<View>(R.id.btnCloseEmpty)
                .setOnClickListener { emptyDialog.dismiss() }
            emptyView.findViewById<View>(R.id.btnAddFirstAddress).setOnClickListener {
                emptyDialog.dismiss()
                showAddAddressDialog(null)
            }

            addressListDialog = emptyDialog
            emptyDialog.show()
            return
        }

        val view = layoutInflater.inflate(R.layout.dialog_address_list, null)
        val tvTitle = view.findViewById<TextView>(R.id.tvAddressListTitle)
        val rvAddresses =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddresses)
        val btnClose = view.findViewById<View>(R.id.btnCloseAddresses)
        val btnAddNew = view.findViewById<View>(R.id.btnAddNewAddress)

        tvTitle.text = "My Addresses (${addresses.size})"
        rvAddresses.layoutManager = LinearLayoutManager(requireContext())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val adapter = AddressAdapter(
            addresses,
            onEdit = { address ->
                dialog.dismiss()
                showAddAddressDialog(address)
            },
            onDelete = { address ->
                if (addresses.size <= 1) {
                    Toast.makeText(
                        requireContext(),
                        "Cannot delete your only address. Add another address first.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (address.isDefault) {
                    Toast.makeText(
                        requireContext(),
                        "Set another address as default first, then delete this one.",
                        Toast.LENGTH_LONG
                    ).show()
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

        btnClose.setOnClickListener { dialog.dismiss() }
        btnAddNew.setOnClickListener {
            dialog.dismiss()
            showAddAddressDialog(null)
        }

        addressListDialog = dialog
        dialog.show()
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
                    Toast.makeText(
                        requireContext(),
                        "Session expired. Please login again.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ProfileViewModel.AuthState.LOGGED_OUT -> {
                    binding.layoutLoggedIn.visibility = View.GONE
                    binding.layoutLoggedOut.visibility = View.VISIBLE
                }
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { binding.tvUserName.text = it ?: "User" }
        viewModel.userEmail.observe(viewLifecycleOwner) {
            binding.tvUserEmail.text = it ?: "No email"
        }
        viewModel.userPhone.observe(viewLifecycleOwner) {
            binding.tvUserPhone.text = it ?: "No phone"
        }
        viewModel.userInitial.observe(viewLifecycleOwner) {
            binding.tvAvatarInitial.text = it ?: "U"
        }

        viewModel.defaultAddressName.observe(viewLifecycleOwner) { name ->
            when {
                name.isNullOrEmpty() -> {
                    binding.tvDefaultAddressName.text = "Default address"
                    binding.tvDefaultBadge.visibility = View.GONE
                }

                name == "Default address" -> {
                    binding.tvDefaultAddressName.text = "Default address"
                    binding.tvDefaultBadge.visibility = View.GONE
                }

                else -> {
                    binding.tvDefaultAddressName.text = name
                    binding.tvDefaultBadge.visibility = View.VISIBLE
                }
            }
        }

        viewModel.defaultAddress.observe(viewLifecycleOwner) { address ->
            binding.tvDefaultAddress.text =
                if (address.isNullOrEmpty() || address == "No address saved yet") {
                    "No address saved yet"
                } else {
                    address
                }
        }

        viewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            if (addresses != null && userRequestedAddresses) {
                userRequestedAddresses = false
                refreshAddressListDialog()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.isOffline.observe(viewLifecycleOwner) { offline ->
            if (offline) {
                Toast.makeText(
                    requireContext(),
                    "You're offline. Showing cached data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.successMessage.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addressListDialog?.dismiss()
        _binding = null
    }
}