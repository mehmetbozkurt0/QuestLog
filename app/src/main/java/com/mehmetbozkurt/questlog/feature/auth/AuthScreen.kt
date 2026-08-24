package com.mehmetbozkurt.questlog.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.auth.GoogleCredentialProvider
import com.mehmetbozkurt.questlog.core.auth.GoogleIdTokenResult
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AuthRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AuthEffect.NavigateToHome -> onNavigateToHome()
                AuthEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                is AuthEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)

                AuthEffect.LaunchGoogleSignIn -> scope.launch {
                    when (val result = GoogleCredentialProvider.requestIdToken(context)) {
                        is GoogleIdTokenResult.Success ->
                            viewModel.onEvent(AuthEvent.GoogleIdTokenReceived(result.idToken))

                        GoogleIdTokenResult.Cancelled ->
                            viewModel.onEvent(AuthEvent.GoogleSignInFailed(null))

                        is GoogleIdTokenResult.Failed ->
                            viewModel.onEvent(AuthEvent.GoogleSignInFailed(result.message))
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AuthScreen(state = state, onEvent = viewModel::onEvent)
        }
    }
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
                enabled = !state.isBusy,
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
            enabled = !state.isBusy,
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
            enabled = !state.isBusy,
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

        Spacer(Modifier.height(Spacing.lg))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text(
                text = "veya",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
            HorizontalDivider(Modifier.weight(1f))
        }

        Spacer(Modifier.height(Spacing.lg))

        OutlinedButton(
            onClick = {
                keyboard?.hide()
                onEvent(AuthEvent.GoogleSignInClicked)
            },
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isGoogleLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Spacing.md))
                Text(
                    text = "Google ile devam et",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (!isSignUp) {
            TextButton(
                onClick = { onEvent(AuthEvent.ForgotPasswordClicked) },
                enabled = !state.isBusy,
            ) {
                Text(
                    text = "Şifremi unuttum",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Spacer(Modifier.height(Spacing.md))
        }

        TextButton(
            onClick = { onEvent(AuthEvent.ModeToggled) },
            enabled = !state.isBusy,
        ) {
            Text(
                text = if (isSignUp) "Zaten hesabın var mı? Giriş yap"
                else "Hesabın yok mu? Kayıt ol",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}