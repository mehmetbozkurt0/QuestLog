package com.mehmetbozkurt.questlog.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.auth.GoogleCredentialProvider
import com.mehmetbozkurt.questlog.core.auth.GoogleIdTokenResult
import com.mehmetbozkurt.questlog.core.common.asString
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
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
            .padding(horizontal = Spacing.screen)
            .padding(vertical = Spacing.xl)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = stringResource(R.string.app_name).uppercaseLocalized(),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(Spacing.xl))

        GlassPanel(
            contentPadding = PaddingValues(Spacing.xl),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(56.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        RoundedCornerShape(2.dp),
                    )
            )

            Spacer(Modifier.height(Spacing.xl))

            Text(
                text = stringResource(
                    if (isSignUp) R.string.auth_panel_title_sign_up
                    else R.string.auth_panel_title_sign_in
                ).uppercaseLocalized(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.xs))

            Text(
                text = stringResource(
                    if (isSignUp) R.string.auth_tagline_sign_up
                    else R.string.auth_tagline_sign_in
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.xl))

            if (isSignUp) {
                FieldLabel(stringResource(R.string.auth_hero_name))
                AuthField(
                    value = state.displayName,
                    onValueChange = { onEvent(AuthEvent.DisplayNameChanged(it)) },
                    enabled = !state.isBusy,
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(Spacing.md))
            }

            FieldLabel(stringResource(R.string.auth_email))
            AuthField(
                value = state.email,
                onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                enabled = !state.isBusy,
                leadingIcon = Icons.Default.MailOutline,
                isError = !state.isEmailValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            if (!state.isEmailValid) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.auth_email_invalid),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(Spacing.md))

            FieldLabel(stringResource(R.string.auth_password))
            AuthField(
                value = state.password,
                onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
                enabled = !state.isBusy,
                leadingIcon = Icons.Default.VpnKey,
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
                shape = MaterialTheme.shapes.large,
                onClick = {
                    keyboard?.hide()
                    onEvent(AuthEvent.SubmitClicked)
                },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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
                HorizontalDivider(
                    Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                )
                Text(
                    text = stringResource(R.string.auth_or).uppercaseLocalized(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.md),
                )
                HorizontalDivider(
                    Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            OutlinedButton(
                shape = MaterialTheme.shapes.large,
                onClick = {
                    keyboard?.hide()
                    onEvent(AuthEvent.GoogleSignInClicked)
                },
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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
        }

        Spacer(Modifier.height(Spacing.md))

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

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercaseLocalized(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(Spacing.xs))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    leadingIcon: ImageVector,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        },
        textStyle = MaterialTheme.typography.titleMedium,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = wellColor(),
            focusedContainerColor = wellColor(),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
