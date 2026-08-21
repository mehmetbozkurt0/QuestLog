package com.mehmetbozkurt.questlog.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AuthEffect.NavigateToHome -> onNavigateToHome()
                AuthEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    AuthScreen(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val isSignUp = state.mode == AuthMode.SIGN_UP

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Renown",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = if (isSignUp) "Maceraya katıl" else "Seyir defterine dön",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xxl))

        if (isSignUp) {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { onEvent(AuthEvent.DisplayNameChanged(it)) },
                label = { Text("Kahraman adı") },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.md))
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
            label = { Text("E-posta") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = !state.isEmailValid,
            supportingText = {
                if (!state.isEmailValid) Text("Geçerli bir e-posta gir")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.md))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
            label = { Text("Parola") },
            singleLine = true,
            enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    onEvent(AuthEvent.SubmitClicked)
                }
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        Button(
            onClick = {
                keyboard?.hide()
                onEvent(AuthEvent.SubmitClicked)
            },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = if (isSignUp) "Kayıt Ol" else "Giriş Yap",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        TextButton(
            onClick = { onEvent(AuthEvent.ModeToggled) },
            enabled = !state.isLoading,
        ) {
            Text(
                text = if (isSignUp) "Zaten hesabın var mı? Giriş yap"
                else "Hesabın yok mu? Kayıt ol",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}