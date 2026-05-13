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

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val resetEmailSent: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState(errorMessage = "Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
                _authState.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.message ?: "Login failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState(errorMessage = "Enter your email address first")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                firebaseAuth.sendPasswordResetEmail(email.trim()).await()
                _authState.value = AuthState(resetEmailSent = true)
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.message ?: "Failed to send email")
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState()
    }
}