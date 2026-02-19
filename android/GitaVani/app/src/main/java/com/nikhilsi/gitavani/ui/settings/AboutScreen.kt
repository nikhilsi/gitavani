package com.nikhilsi.gitavani.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.BuildConfig
import com.nikhilsi.gitavani.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    theme: AppTheme,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", color = theme.primaryTextColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.backgroundColor)
            )
        },
        containerColor = theme.backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "GitaVani",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = theme.primaryTextColor
                )
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    color = theme.secondaryTextColor
                )
            }

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            AboutSection(
                title = "About",
                text = "GitaVani is a clean, ad-free reader for the Bhagavad Gita. All 701 verses across 18 chapters, with Sanskrit text, transliteration, and translations from 12 scholars in Hindi and English.",
                theme = theme
            )

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            AboutSection(
                title = "Data Source",
                text = "Verse data sourced from the Vedic Scriptures Bhagavad Gita project by Pt. Prashant Tripathi. Includes translations and commentaries from renowned scholars including Swami Sivananda, Swami Ramsukhdas, Sri Shankaracharya, and others.",
                theme = theme
            )

            LinkRow("Vedic Scriptures Project", "https://github.com/vedicscriptures/bhagavad-gita", theme) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vedicscriptures/bhagavad-gita")))
                } catch (_: ActivityNotFoundException) { }
            }

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            AboutSection(
                title = "License",
                text = "Verse data is used under the LGPL-3.0 license. The original Sanskrit shlokas of the Bhagavad Gita are ancient public domain text.",
                theme = theme
            )

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            AboutSection(
                title = "Privacy",
                text = "GitaVani collects no personal data. All data is stored locally on your device. No analytics, no tracking, no network calls. Your reading is entirely private.",
                theme = theme
            )

            LinkRow("Privacy Policy", "https://gitavani.app/privacy", theme) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://gitavani.app/privacy")))
                } catch (_: ActivityNotFoundException) { }
            }

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            LinkRow("Support & FAQ", "https://gitavani.app/support", theme) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://gitavani.app/support")))
                } catch (_: ActivityNotFoundException) { }
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, text: String, theme: AppTheme) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.primaryTextColor
        )
        Text(
            text,
            fontSize = 14.sp,
            color = theme.secondaryTextColor,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun LinkRow(title: String, url: String, theme: AppTheme, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 14.sp,
            color = theme.accentColor
        )
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = theme.accentColor,
            modifier = Modifier.padding(4.dp)
        )
    }
}
