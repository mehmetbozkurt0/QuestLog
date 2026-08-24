package com.mehmetbozkurt.questlog.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.core.designsystem.component.SealFrame
import com.mehmetbozkurt.questlog.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
)

private val pages = listOf(
    OnboardingPage(
        Icons.AutoMirrored.Filled.MenuBook,
        R.string.onboarding_1_title,
        R.string.onboarding_1_body,
    ),
    OnboardingPage(
        Icons.Default.FitnessCenter,
        R.string.onboarding_2_title,
        R.string.onboarding_2_body,
    ),
    OnboardingPage(
        Icons.Default.Map,
        R.string.onboarding_3_title,
        R.string.onboarding_3_body,
    ),
    OnboardingPage(
        Icons.Default.LocalFireDepartment,
        R.string.onboarding_4_title,
        R.string.onboarding_4_body,
    ),
)

@Composable
fun OnboardingRoute(onFinished: () -> Unit) {
    OnboardingScreen(onFinished = onFinished)
}

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.md)) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onFinished) {
                    Text(
                        stringResource(R.string.onboarding_skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    Modifier.fillMaxSize().padding(horizontal = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    SealFrame(diameter = 104.dp) {
                        Icon(
                            page.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Spacer(Modifier.height(Spacing.xl))
                    Text(
                        stringResource(page.titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        stringResource(page.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(Spacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    repeat(pages.size) { index ->
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(
                                    if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape,
                                )
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        if (isLastPage) onFinished()
                        else scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                ) {
                    Text(
                        stringResource(
                            if (isLastPage) R.string.onboarding_start
                            else R.string.onboarding_next
                        )
                    )
                }
            }
        }
    }
}
