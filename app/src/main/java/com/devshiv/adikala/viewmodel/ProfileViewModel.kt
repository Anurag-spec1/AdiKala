package com.devshiv.adikala.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.model.Address
import com.devshiv.adikala.model.Profile
import com.devshiv.adikala.repository.CustomerRepository
import com.devshiv.adikala.utils.ProfileCacheManager
import com.devshiv.adikala.view.activity.LoginActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = ShopifyAuthManager(application)
    private val authService = ShopifyAuthService(application)
    private val customerRepository = CustomerRepository()

    private val app = application

    enum class AuthState { LOADING, AUTHENTICATED, EXPIRED, LOGGED_OUT }

    private val _authState = MutableLiveData(AuthState.LOADING)
    val authState: LiveData<AuthState> = _authState

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userPhone = MutableLiveData<String>()
    val userPhone: LiveData<String> = _userPhone

    private val _userInitial = MutableLiveData<String>()
    val userInitial: LiveData<String> = _userInitial

    private val _defaultAddress = MutableLiveData<String>()
    val defaultAddress: LiveData<String> = _defaultAddress

    private val _addresses = MutableLiveData<List<Address>>()
    val addresses: LiveData<List<Address>> = _addresses

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _defaultAddressName = MutableLiveData<String>()
    val defaultAddressName: LiveData<String> = _defaultAddressName

    private val _isOffline = MutableLiveData(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private var profileData: Profile? = null
    private var fetchJob: Job? = null
    private var skipValidationUntil = 0L
    private var fetchRequestId = 0

    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        val hasToken = authManager.isLoggedIn()
        if (!hasToken) {
            _authState.value = AuthState.LOGGED_OUT
            clearProfileData()
            _isOffline.value = false
            return
        }

        val cachedProfile = ProfileCacheManager.getProfile(app)
        if (cachedProfile != null) {
            profileData = cachedProfile
            populateProfileData(cachedProfile)

            ProfileCacheManager.getPhone(app)?.let { _userPhone.value = it }
            ProfileCacheManager.getAddressName(app)?.let { _defaultAddressName.value = it }
            ProfileCacheManager.getAddress(app)?.let { _defaultAddress.value = it }

            _authState.value = AuthState.AUTHENTICATED
            _isOffline.value = false

            if (fetchJob?.isActive != true) silentRefresh()
            loadAddresses()
            return
        }

        loadSavedProfile()
        _authState.value = AuthState.AUTHENTICATED
        if (fetchJob?.isActive != true) fetchProfileFromServer()
    }

    private fun silentRefresh() {
        fetchJob?.cancel()
        val thisRequestId = ++fetchRequestId

        fetchJob = viewModelScope.launch {
            try {
                val token = authService.getValidAccessToken() ?: return@launch
                val profile = customerRepository.getProfile(token)
                if (thisRequestId != fetchRequestId) return@launch

                if (profile != null && profile != profileData) {
                    profileData = profile
                    ProfileCacheManager.saveProfile(app, profile)
                    populateProfileData(profile)
                    loadAddresses()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun onLoginSuccess() {
        fetchJob?.cancel()
        fetchRequestId++
        skipValidationUntil = System.currentTimeMillis() + 15_000L
        _authState.value = AuthState.AUTHENTICATED
        _isOffline.value = false
        loadSavedProfile()
        fetchProfileFromServer()
    }

    private fun fetchProfileFromServer() {
        fetchJob?.cancel()
        val thisRequestId = ++fetchRequestId

        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = authService.getValidAccessToken()
                if (token == null) {
                    if (System.currentTimeMillis() >= skipValidationUntil) {
                        handleAuthExpired("Token unavailable")
                    }
                    return@launch
                }

                val profile = customerRepository.getProfile(token)
                if (thisRequestId != fetchRequestId) return@launch

                if (profile != null) {
                    profileData = profile
                    ProfileCacheManager.saveProfile(app, profile)
                    populateProfileData(profile)
                    _isOffline.value = false
                    _authState.value = AuthState.AUTHENTICATED

                    Log.d("ProfileVM", "=== Calling loadAddresses after profile fetch ===")
                    loadAddresses()
                }else {
                    if (System.currentTimeMillis() >= skipValidationUntil) {
                        handleAuthExpired("Profile fetch returned null")
                    }
                }
            } catch (e: Exception) {
                if (thisRequestId == fetchRequestId) {
                    Log.e("ProfileVM", "Network error", e)
                    if (profileData == null) _isOffline.value = true
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun populateProfileData(profile: Profile) {
        val firstName = profile.firstName?.takeIf { it != "null" } ?: ""
        val lastName = profile.lastName?.takeIf { it != "null" } ?: ""
        val email = profile.email?.takeIf { it != "null" } ?: ""
        val phone = profile.phone?.takeIf { it != "null" && it.isNotEmpty() } ?: ""

        Log.d("ProfileVM", "=== populateProfileData ===")
        Log.d("ProfileVM", "firstName: '$firstName'")
        Log.d("ProfileVM", "lastName: '$lastName'")
        Log.d("ProfileVM", "email: '$email'")
        Log.d("ProfileVM", "phone from profile: '$phone'")
        Log.d("ProfileVM", "defaultAddress phone: '${profile.defaultAddress?.phone}'")

        authManager.saveProfile(firstName, lastName, email, phone)

        _userName.value = listOfNotNull(
            firstName.takeIf { it.isNotEmpty() },
            lastName.takeIf { it.isNotEmpty() }
        ).joinToString(" ").ifEmpty { "User" }

        _userEmail.value = email.ifEmpty { "No email" }
        _userInitial.value = firstName.firstOrNull()?.uppercase() ?: email.firstOrNull()?.uppercase() ?: "U"
        _userPhone.value = phone.ifEmpty { "No phone" }

        Log.d("ProfileVM", "_userPhone set to: '${_userPhone.value}'")

        profile.defaultAddress?.let { addr ->
            val parts = listOfNotNull(addr.address1, addr.address2, addr.city, addr.country, addr.zip)
                .filter { it.isNotEmpty() && it != "null" }
            _defaultAddress.value = if (parts.isNotEmpty()) parts.joinToString(", ") else "India (Default)"
        } ?: run {
            _defaultAddress.value = "India (Default)"
        }
    }

    private fun handleAuthExpired(reason: String) {
        Log.d("ProfileVM", "Auth expired: $reason")
        _authState.value = AuthState.EXPIRED
        authManager.clear()
        skipValidationUntil = 0L
        _isOffline.value = false
        clearProfileData()
    }

    fun launchLogin() {
        val intent = Intent(getApplication(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    private suspend fun getValidToken(): String? = authService.getValidAccessToken()

    private fun loadSavedProfile() {
        _userName.value = authManager.getFullName()
        _userEmail.value = authManager.getEmail() ?: "No email"
        _userPhone.value = authManager.getPhone() ?: "No phone"
        _userInitial.value = authManager.getInitial()
    }

    fun updateProfile(firstName: String, lastName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = getValidToken() ?: return@launch
                val success = customerRepository.updateProfile(token, firstName, lastName)
                if (success) {
                    authManager.saveProfile(firstName, lastName, authManager.getEmail() ?: "", authManager.getPhone() ?: "")
                    loadSavedProfile()
                    _successMessage.value = "Profile updated"
                    fetchProfileFromServer() // Update local cache immediately
                } else {
                    _errorMessage.value = "Failed to update profile"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = getValidToken() ?: return@launch
                val addresses = customerRepository.getAddresses(token)
                _addresses.value = addresses

                val defaultAddr = addresses.find { it.isDefault }
                Log.d("ProfileVM", "=== loadAddresses ===")
                Log.d("ProfileVM", "Default addr firstName: '${defaultAddr?.firstName}'")
                Log.d("ProfileVM", "Default addr lastName: '${defaultAddr?.lastName}'")

                if (defaultAddr != null) {
                    val name = listOfNotNull(defaultAddr.firstName, defaultAddr.lastName)
                        .filter { it.isNotEmpty() }
                        .joinToString(" ")

                    Log.d("ProfileVM", "Constructed name: '$name'")

                    if (name.isNotEmpty()) {
                        _defaultAddressName.value = name
                        ProfileCacheManager.saveAddressName(app, name)
                    } else {
                        _defaultAddressName.value = "Default address"
                    }

                    if (defaultAddr.phone != null && defaultAddr.phone.isNotEmpty()) {
                        _userPhone.value = defaultAddr.phone
                        ProfileCacheManager.savePhone(app, defaultAddr.phone)
                    }

                    val fullAddress = listOfNotNull(
                        defaultAddr.address1, defaultAddr.address2,
                        defaultAddr.city, defaultAddr.state,
                        defaultAddr.country, defaultAddr.zip
                    ).filter { it.isNotEmpty() }.joinToString(", ")
                    _defaultAddress.value = fullAddress.ifEmpty { "No address saved yet" }
                    ProfileCacheManager.saveAddress(app, fullAddress)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load addresses"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAddress(address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = getValidToken() ?: return@launch
                val success = customerRepository.addAddress(token, address)
                if (success) {
                    _successMessage.value = "Address added successfully"
                    fetchProfileFromServer() // Refresh cache
                    loadAddresses()
                } else {
                    _errorMessage.value = "Failed to add address"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add address"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAddress(addressId: String, address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = getValidToken() ?: return@launch
                val success = customerRepository.updateAddress(token, addressId, address)
                if (success) {
                    _successMessage.value = "Address updated"
                    fetchProfileFromServer() // Refresh cache
                    loadAddresses()
                } else {
                    _errorMessage.value = "Failed to update address"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update address"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = getValidToken() ?: return@launch
                val success = customerRepository.deleteAddress(token, addressId)
                if (success) {
                    _successMessage.value = "Address deleted"
                    fetchProfileFromServer() // Refresh cache
                    loadAddresses()
                } else {
                    _errorMessage.value = "Failed to delete address"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete address"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProfileData(): Profile? = profileData

    fun logout() {
        fetchJob?.cancel()
        viewModelScope.launch {
            try {
                _isLoading.value = true
                authService.signOut()
                ProfileCacheManager.clear(app)
                _authState.value = AuthState.LOGGED_OUT
                _isOffline.value = false
                skipValidationUntil = 0L
                clearProfileData()
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error during logout", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun clearProfileData() {
        _userName.value = ""
        _userEmail.value = ""
        _userPhone.value = ""
        _userInitial.value = ""
        _defaultAddress.value = "India (Default)"
        _addresses.value = emptyList()
        profileData = null
    }
}