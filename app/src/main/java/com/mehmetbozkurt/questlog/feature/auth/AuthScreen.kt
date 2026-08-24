package com.mehmetbozkurt.questlog.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.auth.GoogleCredentialProvider
import com.mehmetbozkurt.questlog.core.common.asString
import com.mehmetbozkurt.questlog.core.common.resolve
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
                is AuthEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl)
            .padding(vertical = Spacing.xxl)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_icon),
            contentDescription = null,
            modifier = Modifier.size(88.dp),
        )

        Spacer(Modifier.height(Spacing.md))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = stringResource(
                if (isSignUp) R.string.auth_tagline_sign_up else R.string.auth_tagline_sign_in
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xxl))

        if (isSignUp) {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { onEvent(AuthEvent.DisplayNameChanged(it)) },
                label = { Text(stringResource(R.string.auth_hero_name)) },
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
            label = { Text(stringResource(R.string.auth_email)) },
            singleLine = true,
            enabled = !state.isBusy,
            isError = !state.isEmailValid,
            supportingText = {
                if (!state.isEmailValid) Text(stringResource(R.string.auth_email_invalid))
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
            label = { Text(stringResource(R.string.auth_password)) },
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
                text = state.errorMessage.asString(),
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
                    text = stringResource(
                        if (isSignUp) R.string.auth_sign_up else R.string.auth_sign_in
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.auth_or),
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
                    text = stringResource(R.string.auth_continue_with_google),
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
                    text = stringResource(R.string.auth_forgot_password),
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
                text = stringResource(
                    if (isSignUp) R.string.auth_switch_to_sign_in
                    else R.string.auth_switch_to_sign_up
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}