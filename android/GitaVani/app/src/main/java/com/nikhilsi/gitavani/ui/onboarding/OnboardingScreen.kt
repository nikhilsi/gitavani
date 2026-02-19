package com.nikhilsi.gitavani.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String
)

private val accentOrange = Color(1.0f, 0.42f, 0.0f)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Welcome to GitaVani",
        subtitle = "The Bhagavad Gita, beautifully presented",
        description = "Read all 701 verses across 18 chapters — in Sanskrit, Hindi, and English. No ads, no clutter, just the sacred text."
    ),
    OnboardingPage(
        icon = Icons.Default.Translate,
        title = "Multiple Translations",
        subtitle = "12 scholars, 2 languages",
        description = "Switch between Hindi and English translations. Choose from renowned scholars like Swami Sivananda, Swami Gambirananda, and more."
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Transliteration",
        subtitle = "Sanskrit in Roman script",
        description = "Can't read Devanagari? Toggle transliteration to see Sanskrit verses in Roman letters, making them accessible to everyone."
    ),
    OnboardingPage(
        icon = Icons.Default.Palette,
        title = "Make It Yours",
        subtitle = "4 themes, adjustable font size",
        description = "Choose from Sattva (light), Parchment (warm), Dusk (dark), or Lotus (saffron). Adjust the font size to your comfort."
    )
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // Page indicator dots
        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage
        )

        Spacer(Modifier.height(24.dp))

        // Next / Get Started button
        Button(
            onClick = {
                if (isLastPage) {
                    onComplete()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentOrange)
        ) {
            Text(
                if (isLastPage) "Get Started" else "Next",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        if (!isLastPage) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
            ) {
                Text("Skip", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            page.icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = accentOrange
        )

        Spacer(Modifier.height(24.dp))

        Text(
            page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            page.subtitle,
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            page.description,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(if (index == currentPage) 10.dp else 8.dp)
            ) {
                drawCircle(
                    color = if (index == currentPage) accentOrange else Color.LightGray
                )
            }
        }
    }
}
