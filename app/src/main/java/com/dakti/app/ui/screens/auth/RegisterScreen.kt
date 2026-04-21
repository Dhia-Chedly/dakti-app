@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.auth.RegisterFormState
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.SectionHeader

@Composable
fun RegisterScreen(
    formState: RegisterFormState,
    isLoading: Boolean,
    feedbackMessage: String?,
    feedbackIsError: Boolean,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoToLogin: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Register") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Create your Dakti account",
                subtitle = "Set up your profile to start organizing games."
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = formState.fullName,
                onValueChange = onNameChanged,
                label = { Text("Full name") },
                singleLine = true,
                isError = formState.fullNameError != null,
                supportingText = {
                    formState.fullNameError?.let { Text(text = it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = formState.emailError != null,
                supportingText = {
                    formState.emailError?.let { Text(text = it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.phoneNumber,
                onValueChange = onPhoneChanged,
                label = { Text("Phone number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = formState.phoneNumberError != null,
                supportingText = {
                    formState.phoneNumberError?.let { Text(text = it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = formState.passwordError != null,
                supportingText = {
                    formState.passwordError?.let { Text(text = it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = { Text("Confirm password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = formState.confirmPasswordError != null,
                supportingText = {
                    formState.confirmPasswordError?.let { Text(text = it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            feedbackMessage?.let { message ->
                AppInlineMessage(
                    message = message,
                    isError = feedbackIsError
                )
            }

            Button(
                onClick = onRegisterClick,
                enabled = !isLoading && formState.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .height(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Register")
                }
            }

            TextButton(
                onClick = onGoToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Already have an account? Login")
            }
        }
    }
}
