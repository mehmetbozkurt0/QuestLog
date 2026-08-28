package com.mehmetbozkurt.questlog.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.designsystem.component.Eyebrow
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.theme.ContentHero
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
        Icons.Default.Repeat,
        R.string.onboarding_2_title,
        R.string.onboarding_2_body,
    ),
    OnboardingPage(
        Icons.AutoMirrored.Filled.LibraryBooks,
        R.string.onboarding_3_title,
        R.string.onboarding_3_body,
    ),
    OnboardingPage(
        Icons.Default.Map,
        R.string.onboarding_4_title,
        R.string.onboarding_4_body,
    ),
    OnboardingPage(
        Icons.Default.LocalFireDepartment,
        R.string.onboarding_5_title,
        R.string.onboarding_5_body,
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm)
            ) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onFinished) {
                    Eyebrow(stringResource(R.string.onboarding_skip))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .size(120.dp)
                            .background(wellColor(), CircleShape)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            page.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                    Spacer(Modifier.height(Spacing.xxl))
                    Eyebrow(
                        text = "${pageIndex + 1} / ${pages.size}",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        stringResource(page.titleRes),
                        style = ContentHero,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        stringResource(page.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.screen),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    repeat(pages.size) { index ->
                        val active = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (active) 22.dp else 8.dp,
                            label = "dotWidth",
                        )
                        Box(
                            Modifier
                                .height(4.dp)
                                .width(width)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(2.dp),
                                )
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    shape = MaterialTheme.shapes.large,
                    onClick = {
                        if (isLastPage) onFinished()
                        else scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.height(48.dp),
                ) {
                    Text(
                        stringResource(
                            if (isLastPage) R.string.onboarding_start
                            else R.string.onboarding_next
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
