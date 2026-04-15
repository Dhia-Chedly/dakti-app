package com.dakti.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.User
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthStatus {
    data object Loading : AuthStatus
    data object Unauthenticated : AuthStatus
    data class Authenticated(val user: User) : AuthStatus
    data class Error(val message: String) : AuthStatus
}

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() &&
            password.isNotBlank() &&
            emailError == null &&
            passwordError == null
}

data class RegisterFormState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
) {
    val canSubmit: Boolean
        get() = fullName.isNotBlank() &&
            email.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            fullNameError == null &&
            emailError == null &&
            phoneNumberError == null &&
            passwordError == null &&
            confirmPasswordError == null
}

data class AuthUiState(
    val authStatus: AuthStatus = AuthStatus.Loading,
    val isLoginLoading: Boolean = false,
    val isRegisterLoading: Boolean = false,
    val loginForm: LoginFormState = LoginFormState(),
    val registerForm: RegisterFormState = RegisterFormState(),
    val feedbackMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthenticatedUser().collect { user ->
                _uiState.update { current ->
                    current.copy(
                        authStatus = user?.let { AuthStatus.Authenticated(it) } ?: AuthStatus.Unauthenticated,
                        isLoginLoading = false,
                        isRegisterLoading = false
                    )
                }
            }
        }
    }

    fun onLoginEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                loginForm = current.loginForm.copy(
                    email = value,
                    emailError = validateEmail(value)
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun onLoginPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                loginForm = current.loginForm.copy(
                    password = value,
                    passwordError = validatePassword(value)
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun submitLogin() {
        val currentForm = _uiState.value.loginForm
        val validatedForm = currentForm.copy(
            emailError = validateEmail(currentForm.email),
            passwordError = validatePassword(currentForm.password)
        )

        _uiState.update { it.copy(loginForm = validatedForm, feedbackMessage = null) }

        if (!validatedForm.canSubmit) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoginLoading = true) }
            when (
                val result = authRepository.login(
                    email = validatedForm.email,
                    password = validatedForm.password
                )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            feedbackMessage = "Welcome ${result.data.displayName}"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            authStatus = AuthStatus.Error(result.message),
                            feedbackMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoginLoading = true) }
                }
            }
        }
    }

    fun onRegisterNameChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                registerForm = current.registerForm.copy(
                    fullName = value,
                    fullNameError = if (value.isBlank()) "Name is required" else null
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun onRegisterEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                registerForm = current.registerForm.copy(
                    email = value,
                    emailError = validateEmail(value)
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun onRegisterPhoneChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                registerForm = current.registerForm.copy(
                    phoneNumber = value,
                    phoneNumberError = validatePhone(value)
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun onRegisterPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                registerForm = current.registerForm.copy(
                    password = value,
                    passwordError = validatePassword(value),
                    confirmPasswordError = validateConfirmPassword(
                        password = value,
                        confirmPassword = current.registerForm.confirmPassword
                    )
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun onRegisterConfirmPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                registerForm = current.registerForm.copy(
                    confirmPassword = value,
                    confirmPasswordError = validateConfirmPassword(
                        password = current.registerForm.password,
                        confirmPassword = value
                    )
                )
            )
        }
        clearAuthErrorIfNeeded()
    }

    fun submitRegistration() {
        val currentForm = _uiState.value.registerForm
        val validatedForm = currentForm.copy(
            fullNameError = if (currentForm.fullName.isBlank()) "Name is required" else null,
            emailError = validateEmail(currentForm.email),
            phoneNumberError = validatePhone(currentForm.phoneNumber),
            passwordError = validatePassword(currentForm.password),
            confirmPasswordError = validateConfirmPassword(
                password = currentForm.password,
                confirmPassword = currentForm.confirmPassword
            )
        )

        _uiState.update { it.copy(registerForm = validatedForm, feedbackMessage = null) }

        if (!validatedForm.canSubmit) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRegisterLoading = true) }
            when (
                val result = authRepository.register(
                    name = validatedForm.fullName,
                    email = validatedForm.email,
                    phoneNumber = validatedForm.phoneNumber,
                    password = validatedForm.password
                )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            feedbackMessage = "Account created for ${result.data.displayName}"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            authStatus = AuthStatus.Error(result.message),
                            feedbackMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isRegisterLoading = true) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    authStatus = AuthStatus.Unauthenticated,
                    feedbackMessage = "Logged out"
                )
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    private fun clearAuthErrorIfNeeded() {
        val status = _uiState.value.authStatus
        if (status is AuthStatus.Error) {
            _uiState.update { it.copy(authStatus = AuthStatus.Unauthenticated, feedbackMessage = null) }
        }
    }

    private fun validateEmail(email: String): String? {
        val normalized = email.trim()
        if (normalized.isBlank()) {
            return "Email is required"
        }
        return if (EMAIL_REGEX.matches(normalized)) null else "Enter a valid email"
    }

    private fun validatePhone(phone: String): String? {
        val normalized = phone.trim()
        if (normalized.isBlank()) {
            return "Phone number is required"
        }
        return if (PHONE_REGEX.matches(normalized)) null else "Enter a valid phone number"
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Password is required"
        }
        return if (password.length >= MIN_PASSWORD_LENGTH) {
            null
        } else {
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        if (confirmPassword.isBlank()) {
            return "Confirm your password"
        }
        return if (password == confirmPassword) null else "Passwords do not match"
    }

    private companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private val PHONE_REGEX = Regex("^[0-9+()\\-\\s]{7,15}$")
        private const val MIN_PASSWORD_LENGTH: Int = 6
    }
}
