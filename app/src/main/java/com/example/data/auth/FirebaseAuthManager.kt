package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthManager(private val context: Context) {

    private val tag = "FirebaseAuthManager"
    private var firebaseAuth: FirebaseAuth? = null
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Default Web Client ID placeholder or configured ID
    // In production, users specify their Web Client ID from Google Cloud Console / Firebase Console
    var googleWebClientId: String = "201232242199-placeholder.apps.googleusercontent.com"

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Initialize default FirebaseApp fallback if google-services.json was not injected yet
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:201232242199:android:colleaguex")
                    .setApiKey("AIzaSyColleagueXFallbackKeyForAppInit12345")
                    .setProjectId("colleaguex-community")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(tag, "FirebaseApp initialized with default configuration")
            }

            firebaseAuth = FirebaseAuth.getInstance()
            checkCurrentUser()

            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    val providerId = user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId
                        ?: "password"
                    _authState.value = AuthState.Authenticated(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Professional",
                        photoUrl = user.photoUrl?.toString(),
                        provider = providerId,
                        isEmailVerified = user.isEmailVerified
                    )
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase initialization warning: ${e.message}")
        }
    }

    fun isFirebaseReady(): Boolean {
        return firebaseAuth != null && FirebaseApp.getApps(context).isNotEmpty()
    }

    private fun checkCurrentUser() {
        val user = firebaseAuth?.currentUser
        if (user != null) {
            val providerId = user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId
                ?: "password"
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Professional",
                photoUrl = user.photoUrl?.toString(),
                provider = providerId,
                isEmailVerified = user.isEmailVerified
            )
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Sign up a new user with Email and Password using Firebase Authentication.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String
    ): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error(
            "Firebase is not yet initialized. Please verify your Firebase project setup."
        )

        return try {
            _authState.value = AuthState.Loading
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user

            if (user != null) {
                // Update profile display name
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName.trim())
                        .build()
                    user.updateProfile(profileUpdates).awaitTask()
                } catch (e: Exception) {
                    Log.w(tag, "Profile update warning: ${e.message}")
                }

                val authenticated = AuthState.Authenticated(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = fullName.ifBlank { user.displayName ?: "Professional" },
                    photoUrl = null,
                    provider = "password",
                    isEmailVerified = user.isEmailVerified
                )
                _authState.value = authenticated
                AuthResult.Success(authenticated, "Account created successfully!")
            } else {
                _authState.value = AuthState.Unauthenticated
                AuthResult.Error("Failed to retrieve created user from Firebase.")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Log.w(tag, "Sign up notice: ${e.message}")
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    /**
     * Sign in an existing user with Email and Password using Firebase Authentication.
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error(
            "Firebase is not yet initialized. Please check network and configuration."
        )

        return try {
            _authState.value = AuthState.Loading
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user

            if (user != null) {
                val authenticated = AuthState.Authenticated(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Professional",
                    photoUrl = user.photoUrl?.toString(),
                    provider = "password",
                    isEmailVerified = user.isEmailVerified
                )
                _authState.value = authenticated
                AuthResult.Success(authenticated, "Signed in successfully!")
            } else {
                _authState.value = AuthState.Unauthenticated
                AuthResult.Error("Unable to retrieve user credentials.")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Log.w(tag, "Sign in notice: ${e.message}")
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    /**
     * Sign in with Google using Android Credential Manager and Firebase Authentication.
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        customServerClientId: String? = null
    ): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error(
            "Firebase Authentication is not ready. Verify Firebase setup."
        )

        val clientId = customServerClientId?.takeIf { it.isNotBlank() } ?: googleWebClientId

        return try {
            _authState.value = AuthState.Loading

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Exchange Google ID token with Firebase Auth Credential
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val firebaseResult = auth.signInWithCredential(authCredential).awaitTask()
                val user = firebaseResult.user

                if (user != null) {
                    val authenticated = AuthState.Authenticated(
                        uid = user.uid,
                        email = user.email ?: googleIdTokenCredential.id,
                        displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Google User",
                        photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                        provider = "google.com",
                        isEmailVerified = user.isEmailVerified
                    )
                    _authState.value = authenticated
                    AuthResult.Success(authenticated, "Signed in with Google successfully!")
                } else {
                    _authState.value = AuthState.Unauthenticated
                    AuthResult.Error("Failed to link Google credential with Firebase.")
                }
            } else {
                _authState.value = AuthState.Unauthenticated
                AuthResult.Error("Unexpected credential type received: ${credential::class.java.simpleName}")
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = AuthState.Unauthenticated
            Log.d(tag, "Google Sign-In was cancelled by user")
            AuthResult.Error("Google Sign-In was cancelled.")
        } catch (e: NoCredentialException) {
            _authState.value = AuthState.Unauthenticated
            Log.i(tag, "No Google accounts available on device: ${e.message}")
            AuthResult.Error("No credentials available: Google Sign-In requires an account on this device.")
        } catch (e: GetCredentialException) {
            _authState.value = AuthState.Unauthenticated
            Log.w(tag, "Credential Manager: ${e.message}")
            AuthResult.Error(
                "Google Sign-In: ${e.message ?: "Could not complete credential request."}\n" +
                "Tip: Ensure your Web Client ID from Google Cloud / Firebase console is configured."
            )
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Log.w(tag, "Google sign in notice: ${e.message}")
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    /**
     * Send password reset email.
     */
    suspend fun sendPasswordReset(email: String): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error("Firebase is not initialized.")
        return try {
            auth.sendPasswordResetEmail(email.trim()).awaitTask()
            AuthResult.Success(
                user = null,
                message = "Password reset email sent to $email. Please check your inbox."
            )
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    /**
     * Sign out current user.
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Sign out error: ${e.message}")
        }
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Helper to set simulated/demo authentication for offline development testing.
     */
    fun setDemoAuthenticatedUser(
        email: String = "demo.engineer@company.com",
        name: String = "Demo Professional",
        provider: String = "google.com"
    ): AuthState.Authenticated {
        val user = AuthState.Authenticated(
            uid = "demo_uid_${System.currentTimeMillis()}",
            email = email,
            displayName = name,
            photoUrl = null,
            provider = provider,
            isEmailVerified = true
        )
        _authState.value = user
        return user
    }

    private fun mapFirebaseError(exception: Throwable): String {
        val msg = exception.message ?: ""
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ||
            msg.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) ->
                "An account with this email already exists. Please log in instead."

            msg.contains("The password is invalid", ignoreCase = true) ||
            msg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "Password should be at least 6 characters long."

            msg.contains("badly formatted", ignoreCase = true) ||
            msg.contains("INVALID_EMAIL", ignoreCase = true) ->
                "Please enter a valid email address."

            msg.contains("no user record", ignoreCase = true) ||
            msg.contains("USER_NOT_FOUND", ignoreCase = true) ||
            msg.contains("WRONG_PASSWORD", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Invalid email or password. Please verify and try again."

            msg.contains("network error", ignoreCase = true) ||
            msg.contains("NETWORK_ERROR", ignoreCase = true) ->
                "Network connection problem. Please check your internet connection."

            msg.contains("blocked all requests", ignoreCase = true) ||
            msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ->
                "Too many attempts. Access has been temporarily restricted. Please try again later."

            msg.contains("Default FirebaseApp is not initialized", ignoreCase = true) ->
                "Firebase is initializing or requires a google-services.json file."

            else -> exception.localizedMessage ?: "Authentication failed. Please try again."
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
        addOnCanceledListener {
            if (continuation.isActive) {
                continuation.cancel()
            }
        }
    }
}
