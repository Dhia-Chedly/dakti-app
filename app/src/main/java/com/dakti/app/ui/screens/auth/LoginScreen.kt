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
import com.dakti.app.presentation.auth.LoginFormState
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.SectionHeader

@Composable
fun LoginScreen(
    formState: LoginFormState,
    isLoading: Boolean,
    feedbackMessage: String?,
    feedbackIsError: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Login") },
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
                title = "Welcome back",
                subtitle = "Sign in to continue organizing reservations and matches."
            )

            Spacer(modifier = Modifier.height(4.dp))

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

            feedbackMessage?.let { message ->
                AppInlineMessage(
                    message = message,
                    isError = feedbackIsError
                )
            }

            Button(
                onClick = onLoginClick,
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
                    Text(text = "Login")
                }
            }

            TextButton(
                onClick = onGoToRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Don't have an account? Register")
            }

            Text(
                text = "Demo account: demo@dakti.app / demo123",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
