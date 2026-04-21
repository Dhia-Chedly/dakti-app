package com.dakti.app.presentation.auth

import com.dakti.app.testutil.FakeAuthRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeAuthRepository()

    @Test
    fun submitLogin_withInvalidEmail_setsValidationError() = runTest {
        val viewModel = AuthViewModel(fakeRepository)

        viewModel.onLoginEmailChanged("invalid-email")
        viewModel.onLoginPasswordChanged("123456")
        viewModel.submitLogin()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Enter a valid email", state.loginForm.emailError)
        assertFalse(state.isLoginLoading)
    }

    @Test
    fun submitLogin_success_updatesAuthenticatedState() = runTest {
        val user = TestData.user()
        fakeRepository.loginResult = Resource.Success(user)
        val viewModel = AuthViewModel(fakeRepository)

        viewModel.onLoginEmailChanged(user.email)
        viewModel.onLoginPasswordChanged("demo123")
        viewModel.submitLogin()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authStatus is AuthStatus.Authenticated)
        assertEquals("Welcome ${user.displayName}", state.feedbackMessage)
        assertFalse(state.feedbackIsError)
    }

    @Test
    fun submitRegistration_whenPasswordsMismatch_showsConfirmPasswordError() = runTest {
        val viewModel = AuthViewModel(fakeRepository)

        viewModel.onRegisterNameChanged("Demo User")
        viewModel.onRegisterEmailChanged("demo@dakti.app")
        viewModel.onRegisterPhoneChanged("+2348011111111")
        viewModel.onRegisterPasswordChanged("password1")
        viewModel.onRegisterConfirmPasswordChanged("password2")
        viewModel.submitRegistration()
        advanceUntilIdle()

        val form = viewModel.uiState.value.registerForm
        assertEquals("Passwords do not match", form.confirmPasswordError)
        assertFalse(viewModel.uiState.value.isRegisterLoading)
    }
}
