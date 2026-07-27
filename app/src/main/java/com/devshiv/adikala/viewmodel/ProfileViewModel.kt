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
import com.devshiv.adikala.repository.CustomerRepository
import com.devshiv.adikala.view.activity.LoginActivity
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = ShopifyAuthManager(application)
    private val authService = ShopifyAuthService(application)
    private val customerRepository = CustomerRepository()

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

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

    private val _addresses = MutableLiveData<List<CustomerRepository.Address>>()
    val addresses: LiveData<List<CustomerRepository.Address>> = _addresses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private var profileData: CustomerRepository.Profile? = null

    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        val loggedIn = authManager.isLoggedIn()
        _isLoggedIn.value = loggedIn
        if (loggedIn) {
            loadSavedProfile()
            fetchProfileFromServer()
        }
    }

    fun launchLogin() {
        val intent = Intent(getApplication(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    private fun loadSavedProfile() {
        _userName.value = authManager.getFullName()
        _userEmail.value = authManager.getEmail() ?: "No email"
        _userPhone.value = authManager.getPhone() ?: "No phone"
        _userInitial.value = authManager.getInitial()
    }

    private fun fetchProfileFromServer() {
        val token = authManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                profileData = customerRepository.getProfile(token)
                profileData?.let { profile ->
                    authManager.saveProfile(
                        profile.firstName ?: "",
                        profile.lastName ?: "",
                        profile.email ?: "",
                        profile.phone ?: ""
                    )
                    _userName.value = authManager.getFullName()
                    _userEmail.value = profile.email ?: "No email"
                    _userPhone.value = profile.phone ?: "No phone"
                    _userInitial.value = authManager.getInitial()
                    profile.defaultAddress?.let { addr ->
                        val parts = listOfNotNull(addr.address1, addr.city, addr.state, addr.country, addr.zip)
                            .filter { it.isNotEmpty() }
                        _defaultAddress.value = if (parts.isNotEmpty()) parts.joinToString(", ") else "No address saved yet"
                    } ?: run {
                        _defaultAddress.value = "No address saved yet"
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error fetching profile", e)
                _errorMessage.value = "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String) {
        val token = authManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = customerRepository.updateProfile(token, firstName, lastName)
                if (success) {
                    authManager.saveProfile(firstName, lastName, authManager.getEmail() ?: "", authManager.getPhone() ?: "")
                    loadSavedProfile()
                    _successMessage.value = "Profile updated"
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
        val token = authManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _addresses.value = customerRepository.getAddresses(token)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load addresses"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAddress(address: CustomerRepository.Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val refreshed = authService.refreshAccessToken()
                if (!refreshed) {
                    _errorMessage.value = "Session expired. Please login again."
                    _isLoading.value = false
                    return@launch
                }

                val token = authManager.getAccessToken() ?: run {
                    _errorMessage.value = "Session expired. Please login again."
                    _isLoading.value = false
                    return@launch
                }

                val success = customerRepository.addAddress(token, address)
                if (success) {
                    _successMessage.value = "Address added successfully"
                    fetchProfileFromServer()
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

    fun updateAddress(addressId: String, address: CustomerRepository.Address) {
        val token = authManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = customerRepository.updateAddress(token, addressId, address)
                if (success) {
                    _successMessage.value = "Address updated"
                    fetchProfileFromServer()
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
        val token = authManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = customerRepository.deleteAddress(token, addressId)
                if (success) {
                    _successMessage.value = "Address deleted"
                    fetchProfileFromServer()
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

    fun getProfileData(): CustomerRepository.Profile? = profileData

    fun logout() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                authService.signOut()
                _isLoggedIn.value = false
                _userName.value = ""
                _userEmail.value = ""
                _userPhone.value = ""
                _userInitial.value = ""
                _defaultAddress.value = ""
                _addresses.value = emptyList()
                profileData = null
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error during logout", e)
                _errorMessage.value = "Sign out failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}