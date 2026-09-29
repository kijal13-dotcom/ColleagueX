package com.example

import com.example.data.auth.AuthResult
import com.example.data.auth.AuthState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUnitTest {

    @Test
    fun `test initial auth state is unauthenticated`() {
        val state: AuthState = AuthState.Unauthenticated
        assertTrue(state is AuthState.Unauthenticated)
    }

    @Test
    fun `test authenticated user model fields`() {
        val user = AuthState.Authenticated(
            uid = "firebase_test_123",
            email = "alex.chen@workcircle.io",
            displayName = "Alex Chen",
            photoUrl = "https://example.com/avatar.jpg",
            provider = "google.com",
            isEmailVerified = true
        )
        assertEquals("firebase_test_123", user.uid)
        assertEquals("alex.chen@workcircle.io", user.email)
        assertEquals("Alex Chen", user.displayName)
        assertEquals("google.com", user.provider)
        assertTrue(user.isEmailVerified)
    }

    @Test
    fun `test auth result success and error types`() {
        val success = AuthResult.Success(
            user = AuthState.Authenticated(
                uid = "uid_456",
                email = "priya@example.com",
                displayName = "Priya Sharma",
                photoUrl = null,
                provider = "password"
            ),
            message = "Signed in successfully"
        )
        assertNotNull(success.user)
        assertEquals("uid_456", success.user?.uid)

        val error = AuthResult.Error("Invalid credentials")
        assertEquals("Invalid credentials", error.message)
    }
}
