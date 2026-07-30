package com.devshiv.adikala.view.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.facebook.shimmer.ShimmerFrameLayout
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.databinding.FragmentProfileBinding
import com.devshiv.adikala.model.Address
import com.devshiv.adikala.utils.IndianStates
import com.devshiv.adikala.view.activity.AboutUsActivity
import com.devshiv.adikala.view.activity.PrivacyPolicyActivity
import com.devshiv.adikala.view.activity.RefundsReturnsActivity
import com.devshiv.adikala.view.activity.ShippingPolicyActivity
import com.devshiv.adikala.view.activity.TermsOfUseActivity
import com.devshiv.adikala.view.adapter.AddressAdapter
import com.devshiv.adikala.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authService: ShopifyAuthService
    private var addressListDialog: AlertDialog? = null
    private var isAddressDialogShowing = false

    private var hasUserName = false
    private var hasUserEmail = false
    private var hasUserPhone = false
    private var hasUserInitial = false
    private var hasDefaultAddressName = false
    private var hasDefaultAddress = false
    private var latestInitial: String? = null
    private var latestEmail: String? = null
    private var isUserOperation = false
    private var currentAuthState: ProfileViewModel.AuthState? = null

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
        binding.btnAddAddress.setOnClickListener { showAddAddressDialog(null) }
        binding.btnManageAddress.setOnClickListener {
            isAddressDialogShowing = true
            viewModel.loadAddresses()
        }
        binding.btnAboutUs.setOnClickListener {
            startActivity(Intent(requireContext(), AboutUsActivity::class.java))
        }
        binding.btnRefundsAndReturns.setOnClickListener {
                startActivity(Intent(requireContext(), RefundsReturnsActivity::class.java))
        }
        binding.btnShippingPolicy.setOnClickListener {
            startActivity(Intent(requireContext(), ShippingPolicyActivity::class.java))
        }
        binding.btnPrivacyPolicy.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyPolicyActivity::class.java))
        }
        binding.btnTerms.setOnClickListener {
            startActivity(Intent(requireContext(), TermsOfUseActivity::class.java))
        }
    }

    private val shimmerView: ShimmerFrameLayout get() = binding.shimmerProfile.root

    private fun showShimmer() {
        shimmerView.visibility = View.VISIBLE
        shimmerView.startShimmer()
    }

    private fun hideShimmer() {
        shimmerView.stopShimmer()
        shimmerView.visibility = View.GONE
    }

    private fun resetDataLoadedFlags() {
        hasUserName = false
        hasUserEmail = false
        hasUserPhone = false
        hasUserInitial = false
        hasDefaultAddressName = false
        hasDefaultAddress = false
    }

    private fun isAllProfileDataLoaded() = hasUserName && hasUserEmail && hasUserPhone && hasUserInitial && hasDefaultAddressName && hasDefaultAddress

    private fun revealContentIfReady() {
        when (currentAuthState) {
            ProfileViewModel.AuthState.AUTHENTICATED -> {
                if (isAllProfileDataLoaded()) {
                    hideShimmer()
                    binding.layoutLoggedIn.visibility = View.VISIBLE
                    binding.layoutLoggedOut.visibility = View.GONE
                }
            }
            ProfileViewModel.AuthState.EXPIRED, ProfileViewModel.AuthState.LOGGED_OUT -> {
                hideShimmer()
                binding.layoutLoggedIn.visibility = View.GONE
                binding.layoutLoggedOut.visibility = View.VISIBLE
            }
            ProfileViewModel.AuthState.LOADING, null -> {
                binding.layoutLoggedIn.visibility = View.GONE
                binding.layoutLoggedOut.visibility = View.GONE
                showShimmer()
            }
        }
    }

    private fun loadProfileImage(email: String?, initial: String) {
        val safeInitial = initial.trim().takeIf { it.isNotBlank() }?.first()?.uppercase() ?: "U"
        binding.tvAvatarInitial.setImageBitmap(generateInitialAvatar(safeInitial))
    }

    private fun generateInitialAvatar(initial: String): Bitmap {
        val safeInitial = initial.trim().takeIf { it.isNotBlank() }?.first()?.uppercase() ?: "U"
        val sizePx = (100 * resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val radius = sizePx / 2f

        canvas.drawCircle(radius, radius, radius, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = Color.parseColor("#0D3238")
        })

        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = sizePx * 0.45f
            isFakeBoldText = true
        }
        canvas.drawText(safeInitial, radius, radius - (textPaint.descent() + textPaint.ascent()) / 2f, textPaint)
        return bitmap
    }

    private fun showSignOutDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_signout, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<View>(R.id.btnCancelSignOut).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnConfirmSignOut).setOnClickListener { dialog.dismiss(); performSignOut() }
        dialog.show()
    }

    private fun performSignOut() {
        lifecycleScope.launch {
            try {
                viewModel.logout()
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
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
        etFirstName.setText(profile?.firstName ?: "")
        etLastName.setText(profile?.lastName ?: "")

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<View>(R.id.btnCancelProfile).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnSaveProfile).setOnClickListener {
            val first = etFirstName.text.toString().trim()
            val last = etLastName.text.toString().trim()
            if (first.isEmpty() || last.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter first and last name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isUserOperation = true
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

        val stateAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_state, R.id.tvDropdownItem, IndianStates.names)
        etState.setAdapter(stateAdapter)
        etState.setOnClickListener { etState.showDropDown() }

        existingAddress?.let {
            etFirstName.setText(it.firstName)
            etLastName.setText(it.lastName)
            etAddress.setText(it.address1)
            etApartment.setText(it.address2)
            etCity.setText(it.city)
            etState.setText(IndianStates.nameForCode(it.state ?: "") ?: it.state ?: "", false)
            etZip.setText(it.zip)
            etPhone.setText(it.phone)
            cbDefault.isChecked = it.isDefault
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener { saveAddress(dialog, existingAddress, etFirstName, etLastName, etAddress, etApartment, etCity, etState, etZip, etPhone, cbDefault, isEdit) }
        dialog.show()
    }

    private fun saveAddress(dialog: AlertDialog, existingAddress: Address?, etFirstName: EditText, etLastName: EditText, etAddress: EditText, etApartment: EditText, etCity: EditText, etState: AutoCompleteTextView, etZip: EditText, etPhone: EditText, cbDefault: CheckBox, isEdit: Boolean) {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val address1 = etAddress.text.toString().trim()
        val apartment = etApartment.text.toString().trim()
        val city = etCity.text.toString().trim()
        val stateName = etState.text.toString().trim()
        val zip = etZip.text.toString().trim()
        val phoneInput = etPhone.text.toString().trim()

        if (firstName.isEmpty()) { Toast.makeText(requireContext(), "Please enter first name", Toast.LENGTH_SHORT).show(); return }
        if (lastName.isEmpty()) { Toast.makeText(requireContext(), "Please enter last name", Toast.LENGTH_SHORT).show(); return }
        if (address1.isEmpty()) { Toast.makeText(requireContext(), "Please enter your address", Toast.LENGTH_SHORT).show(); return }
        if (apartment.isEmpty()) { Toast.makeText(requireContext(), "Please enter apartment/floor", Toast.LENGTH_SHORT).show(); return }
        if (city.isEmpty()) { Toast.makeText(requireContext(), "Please enter your city", Toast.LENGTH_SHORT).show(); return }
        if (stateName.isEmpty()) { Toast.makeText(requireContext(), "Please select your state", Toast.LENGTH_SHORT).show(); return }
        if (zip.isEmpty()) { Toast.makeText(requireContext(), "Please enter PIN code", Toast.LENGTH_SHORT).show(); return }
        if (phoneInput.isEmpty()) { Toast.makeText(requireContext(), "Please enter phone number", Toast.LENGTH_SHORT).show(); return }

        val stateCode = IndianStates.codeForName(stateName) ?: run { Toast.makeText(requireContext(), "Please select a valid state", Toast.LENGTH_SHORT).show(); return }
        if (!zip.matches(Regex("^[1-9][0-9]{5}$"))) { Toast.makeText(requireContext(), "Please enter a valid 6-digit PIN code", Toast.LENGTH_SHORT).show(); return }

        val digitsOnly = phoneInput.replace(Regex("[^0-9]"), "")
        val normalizedNumber = when {
            digitsOnly.length == 10 -> digitsOnly
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.substring(2)
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> digitsOnly.substring(1)
            else -> digitsOnly
        }
        if (!normalizedNumber.matches(Regex("^[6-9][0-9]{9}$"))) { Toast.makeText(requireContext(), "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show(); return }

        val address = Address(
            id = existingAddress?.id, firstName = firstName, lastName = lastName,
            address1 = address1, address2 = apartment, city = city, country = "IN",
            state = stateCode, zip = zip, phone = "+91$normalizedNumber", isDefault = cbDefault.isChecked
        )

        isUserOperation = true
        if (isEdit) viewModel.updateAddress(existingAddress!!.id!!, address) else viewModel.addAddress(address)
        dialog.dismiss()
    }

    private fun showAddressListDialog() {
        viewModel.loadAddresses()
    }

    private fun updateAddressListUI(addresses: List<Address>) {
        if (!isAddressDialogShowing) return

        if (addressListDialog?.isShowing == true) {
            val rv = addressListDialog?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddresses)
            val title = addressListDialog?.findViewById<TextView>(R.id.tvAddressListTitle)
            title?.text = "My Addresses (${addresses.size})"
            (rv?.adapter as? AddressAdapter)?.updateList(addresses)
            return
        }

        addressListDialog?.dismiss()
        isAddressDialogShowing = false

        if (addresses.isEmpty()) {
            showEmptyAddressDialog()
            return
        }

        val view = layoutInflater.inflate(R.layout.dialog_address_list, null)
        val tvTitle = view.findViewById<TextView>(R.id.tvAddressListTitle)
        val rvAddresses = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddresses)
        val btnClose = view.findViewById<View>(R.id.btnCloseAddresses)
        val btnAddNew = view.findViewById<View>(R.id.btnAddNewAddress)

        tvTitle.text = "My Addresses (${addresses.size})"
        rvAddresses.layoutManager = LinearLayoutManager(requireContext())

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        rvAddresses.adapter = AddressAdapter(addresses,
            onEdit = { dialog.dismiss(); showAddAddressDialog(it) },
            onDelete = { addr ->
                if (addresses.size <= 1) {
                    Toast.makeText(requireContext(), "Cannot delete your only address", Toast.LENGTH_LONG).show()
                } else if (addr.isDefault) {
                    Toast.makeText(requireContext(), "Set another address as default first", Toast.LENGTH_LONG).show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Address").setMessage("Are you sure?")
                        .setPositiveButton("Delete") { _, _ ->
                            isAddressDialogShowing = true
                            isUserOperation = true
                            viewModel.deleteAddress(addr.id!!)
                        }
                        .setNegativeButton("Cancel", null).show()
                }
            },
            onSetDefault = { addr ->
                isAddressDialogShowing = true
                isUserOperation = true
                viewModel.updateAddress(addr.id!!, addr.copy(isDefault = true))
            }
        )
        btnClose.setOnClickListener { dialog.dismiss() }
        btnAddNew.setOnClickListener { dialog.dismiss(); showAddAddressDialog(null) }
        dialog.setOnDismissListener { addressListDialog = null; isAddressDialogShowing = false }
        addressListDialog = dialog
        dialog.show()
    }

    private fun showEmptyAddressDialog() {
        val emptyView = layoutInflater.inflate(R.layout.dialog_address_empty, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(emptyView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        emptyView.findViewById<View>(R.id.btnCloseEmpty).setOnClickListener { dialog.dismiss() }
        emptyView.findViewById<View>(R.id.btnAddFirstAddress).setOnClickListener { dialog.dismiss(); showAddAddressDialog(null) }
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            currentAuthState = state
            if (state == ProfileViewModel.AuthState.LOADING) resetDataLoadedFlags()
            revealContentIfReady()
        }

        viewModel.userName.observe(viewLifecycleOwner) {
            binding.tvUserName.text = it ?: "User"
            hasUserName = true
            val newInitial = it?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: latestInitial ?: "U"
            if (newInitial != latestInitial) {
                latestInitial = newInitial
                binding.tvAvatarInitial.setImageBitmap(generateInitialAvatar(newInitial))
            }
            revealContentIfReady()
        }

        viewModel.userEmail.observe(viewLifecycleOwner) {
            binding.tvUserEmail.text = it ?: "No email"
            latestEmail = it
            hasUserEmail = true
            revealContentIfReady()
        }

        viewModel.userPhone.observe(viewLifecycleOwner) {
            binding.tvUserPhone.text = it ?: "No phone"
            hasUserPhone = true
            revealContentIfReady()
        }

        viewModel.userInitial.observe(viewLifecycleOwner) {
            latestInitial = it?.takeIf { s -> s.isNotBlank() } ?: "U"
            hasUserInitial = true
            revealContentIfReady()
        }

        viewModel.defaultAddressName.observe(viewLifecycleOwner) { name ->
            if (name.isNullOrEmpty() || name == "Default address") {
                binding.tvDefaultAddressName.text = "Default address"
                binding.tvDefaultBadge.visibility = View.GONE
            } else {
                binding.tvDefaultAddressName.text = name
                binding.tvDefaultBadge.visibility = View.VISIBLE
            }
            hasDefaultAddressName = true
            revealContentIfReady()
        }

        viewModel.defaultAddress.observe(viewLifecycleOwner) { address ->
            binding.tvDefaultAddress.text = if (address.isNullOrEmpty() || address == "No address saved yet") "No address saved yet" else address
            hasDefaultAddress = true
            revealContentIfReady()
        }

        viewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            Log.d("ProfileFragment", "=== addresses observer FIRED ===")
            Log.d("ProfileFragment", "isAddressDialogShowing: $isAddressDialogShowing")
            Log.d("ProfileFragment", "addresses count: ${addresses?.size}")
            addresses?.forEachIndexed { index, addr ->
                Log.d("ProfileFragment", "  addr[$index]: id=${addr.id}, name=${addr.firstName} ${addr.lastName}")
            }
            if (addresses != null) updateAddressListUI(addresses)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && isUserOperation) {
                showShimmer()
                binding.layoutLoggedIn.visibility = View.GONE
            } else if (!isLoading && isUserOperation) {
                hideShimmer()
                binding.layoutLoggedIn.visibility = View.VISIBLE
                isUserOperation = false
            }
        }

        viewModel.isOffline.observe(viewLifecycleOwner) { if (it) Toast.makeText(requireContext(), "You're offline. Showing cached data.", Toast.LENGTH_SHORT).show() }
        viewModel.errorMessage.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() } }
        viewModel.successMessage.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shimmerView.stopShimmer()
        addressListDialog?.dismiss()
        _binding = null
    }
}