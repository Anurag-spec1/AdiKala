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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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
        val token = authManager.getAccessToken()
        if (token.isNullOrEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = customerRepository.getProfile(token)
                if (profile != null) {
                    authManager.saveProfile(
                        firstName = profile.firstName ?: "",
                        lastName = profile.lastName ?: "",
                        email = profile.email ?: "",
                        phone = profile.phone ?: ""
                    )

                    _userName.value = authManager.getFullName()
                    _userEmail.value = profile.email ?: "No email"
                    _userPhone.value = profile.phone ?: "No phone"
                    _userInitial.value = authManager.getInitial()

                    profile.defaultAddress?.let { addr ->
                        val parts = listOfNotNull(
                            addr.address1,
                            addr.address2,
                            addr.city,
                            addr.country,
                            addr.zip
                        ).filter { it.isNotEmpty() }

                        _defaultAddress.value = if (parts.isNotEmpty()) {
                            parts.joinToString(", ")
                        } else {
                            "No address saved yet"
                        }
                    } ?: run {
                        _defaultAddress.value = "No address saved yet"
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error fetching profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Call server logout
                authService.signOut()

                // Update UI - show logged out state
                _isLoggedIn.value = false
                _userName.value = ""
                _userEmail.value = ""
                _userPhone.value = ""
                _userInitial.value = ""
                _defaultAddress.value = ""

            } catch (e: Exception) {
                Log.e("ProfileVM", "Error during logout", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}