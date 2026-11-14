package com.team.taskmanagementapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: FirebaseUser? = null
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(user = auth.currentUser)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _uiState.value = _uiState.value.copy(
                user = firebaseAuth.currentUser
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter both email and password."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        auth.signInWithEmailAndPassword(trimmedEmail, pass)
            .addOnSuccessListener { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.user,
                    errorMessage = null
                )
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Sign in failed. Please try again."
                )
            }
    }

    fun registerWithEmail(
        email: String,
        pass: String,
        confirmPass: String,
        onSuccess: () -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all fields."
            )
            return
        }

        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password must be at least 6 characters long."
            )
            return
        }

        if (pass != confirmPass) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Passwords do not match."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        auth.createUserWithEmailAndPassword(trimmedEmail, pass)
            .addOnSuccessListener { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.user,
                    errorMessage = null
                )
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Registration failed. Please try again."
                )
            }
    }

    fun sendPasswordReset(email: String, onSent: () -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter your email to reset password."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        auth.sendPasswordResetEmail(trimmedEmail)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                onSent()
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Failed to send reset email."
                )
            }
    }

    fun signInWithGoogleIdToken(idToken: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.user,
                    errorMessage = null
                )
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Google sign-in failed."
                )
            }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState(user = null)
    }
}
