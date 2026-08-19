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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        Icons.AutoMirrored.Filled.MenuBook,
        "Günlüğüne hoş geldin, maceracı",
        "Bu bir yapılacaklar listesi değil. Burada gerçek hayatta yaptığın her iş " +
                "karakterini inşa eder. Spor, kitap, ders, sosyalleşme... hepsi birer görev.",
    ),
    OnboardingPage(
        Icons.Default.FitnessCenter,
        "Görevler yetenek kazandırır",
        "Görev oluştururken hangi yeteneği geliştirdiğini seçersin: spor Güç'e, " +
                "okumak Zeka'ya işler. Zorluk XP'yi belirler. Ama dikkat — sistem cömert " +
                "değildir, günlük sınırlar vardır.",
    ),
    OnboardingPage(
        Icons.Default.Map,
        "Yollar seni sınar",
        "Yol, aşamalardan oluşan uzun bir maceradır. Yoldayken kazandığın XP'nin bir " +
                "kısmı emanete yazılır: bitirirsen emanet ve bonus senindir, yarıda " +
                "bırakırsan emanet yanar.",
    ),
    OnboardingPage(
        Icons.Default.LocalFireDepartment,
        "Efsane olmak kolay değil",
        "Seviye atladıkça yetenek hakları kazanırsın, her gün görev yaparsan alevin " +
                "büyür. Seviye 20'ye ulaşan henüz görülmedi. Belki ilk sen olursun.",
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
                    Text("Atla", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Box(
                        Modifier
                            .size(96.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            page.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Spacer(Modifier.height(Spacing.xl))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        page.body,
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
                    Text(if (isLastPage) "Maceraya Başla" else "İleri")
                }
            }
        }
    }
}
