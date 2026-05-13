package com.example.nammaraste.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun signup(email: String, password: String, confirmPassword: String) {
        when {
            email.isBlank() || password.isBlank() -> {
                _authState.value = AuthState(errorMessage = "All fields are required")
                return
            }
            password != confirmPassword -> {
                _authState.value = AuthState(errorMessage = "Passwords do not match")
                return
            }
            password.length < 6 -> {
                _authState.value = AuthState(errorMessage = "Password must be at least 6 characters")
                return
            }
        }
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
                _authState.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.message ?: "Signup failed")
            }
        }
    }
}