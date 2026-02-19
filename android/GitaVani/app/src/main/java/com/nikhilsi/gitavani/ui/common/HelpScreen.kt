package com.nikhilsi.gitavani.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.theme.AppTheme

private data class HelpItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val helpItems = listOf(
    HelpItem(
        Icons.Default.SwipeRight,
        "Navigate Verses",
        "Swipe left or right on the reading screen to move between verses. You can also use the Previous/Next buttons at the bottom."
    ),
    HelpItem(
        Icons.Default.Translate,
        "Switch Language",
        "On the reading screen, tap English or Hindi to switch the translation language. Tap an author's name to see their translation."
    ),
    HelpItem(
        Icons.AutoMirrored.Filled.MenuBook,
        "Transliteration",
        "Tap the book icon on the reading screen to show Sanskrit verses in Roman script. You can also toggle this in Settings."
    ),
    HelpItem(
        Icons.AutoMirrored.Filled.Subject,
        "Commentaries",
        "Scroll below the translation on any verse to find commentaries from 17 scholars in English, Hindi, and Sanskrit. Use the language and author tabs to switch between them."
    ),
    HelpItem(
        Icons.Default.Search,
        "Search Verses",
        "Tap the search icon on the home screen to search across all verses — Sanskrit, transliteration, and translations. You can also search within a specific chapter from its verse list."
    ),
    HelpItem(
        Icons.Default.Favorite,
        "Favorites",
        "Tap the heart icon on any verse to save it as a favorite. Access your saved verses from the heart icon in the toolbar. Sort by recent or chapter order."
    ),
    HelpItem(
        Icons.Default.Share,
        "Share Verses",
        "Tap the share icon on the reading screen to share a verse as text. The shared text includes the Sanskrit verse and translation."
    ),
    HelpItem(
        Icons.Default.Palette,
        "Change Theme",
        "Go to Settings (gear icon) to choose between 4 themes: Sattva (light), Parchment (warm), Dusk (dark), or Lotus (saffron)."
    ),
    HelpItem(
        Icons.Default.FormatSize,
        "Adjust Font Size",
        "In Settings, use the font size slider to make text larger or smaller (14–28pt). The app also responds to your system font size setting."
    ),
    HelpItem(
        Icons.Default.Bookmark,
        "Resume Reading",
        "The app remembers where you left off. When you return, tap the 'Continue Reading' banner on the home screen to pick up where you stopped."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    theme: AppTheme,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help", color = theme.primaryTextColor) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "How to use GitaVani",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primaryTextColor
            )

            helpItems.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryTextColor
                        )
                        Text(
                            item.description,
                            fontSize = 14.sp,
                            color = theme.secondaryTextColor,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
