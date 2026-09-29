package com.example.data.auth

import com.google.firebase.auth.FirebaseUser

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(
        val uid: String,
        val email: String,
        val displayName: String,
        val photoUrl: String? = null,
        val provider: String = "password", // "password", "google.com", "guest"
        val isEmailVerified: Boolean = false
    ) : AuthState
}

sealed interface AuthResult {
    data class Success(val user: AuthState.Authenticated? = null, val message: String = "Success") : AuthResult
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult
    data class ConfigurationNotice(val message: String) : AuthResult
}
