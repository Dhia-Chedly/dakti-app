package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.auth.RegisterFormState
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.theme.DaktiThemeTokens

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
    val hero = DaktiThemeTokens.hero
    SunsetStadiumBackground(gradientIndex = 3) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(text = "Back", color = hero.onHero)
                }
                SportsEmoteBadge(text = "??")
            }

            Text(
                text = "Create your Dakti account",
                style = MaterialTheme.typography.headlineSmall,
                color = hero.onHero
            )
            Text(
                text = "Set up your profile and start organizing games in minutes.",
                style = MaterialTheme.typography.bodyMedium,
                color = hero.onHeroMuted
            )

            AuthGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                feedbackMessage?.let { message ->
                    AppInlineMessage(
                        message = message,
                        isError = feedbackIsError,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Button(
                    onClick = onRegisterClick,
                    enabled = !isLoading && formState.canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 2.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Register")
                    }
                }

                TextButton(
                    onClick = onGoToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(text = "Already have an account? Login")
                }
            }
        }
    }
}
