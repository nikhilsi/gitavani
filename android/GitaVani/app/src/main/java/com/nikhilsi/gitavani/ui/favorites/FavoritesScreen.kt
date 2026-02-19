package com.nikhilsi.gitavani.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.data.GitaDataService
import com.nikhilsi.gitavani.model.Language
import com.nikhilsi.gitavani.model.Verse
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.theme.AppTheme

private enum class SortOrder { RECENT, CHAPTER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    dataService: GitaDataService,
    settings: AppSettings,
    theme: AppTheme,
    onVerseClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val favoriteIds by settings.favoriteVerseIds.collectAsState()
    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val fontSize by settings.fontSize.collectAsState()
    var sortOrder by remember { mutableStateOf(SortOrder.RECENT) }

    val favoriteVerses = remember(favoriteIds, sortOrder) {
        val verses = favoriteIds.mapNotNull { dataService.verse(it) }
        when (sortOrder) {
            SortOrder.RECENT -> verses
            SortOrder.CHAPTER -> verses.sortedWith(compareBy({ it.chapter }, { it.verse }))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = theme.primaryTextColor) },
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
        if (favoriteIds.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = theme.secondaryTextColor.copy(alpha = 0.5f)
                )
                Text(
                    "No Favorites Yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primaryTextColor,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    "Tap the heart icon on any verse to save it here.",
                    fontSize = 14.sp,
                    color = theme.secondaryTextColor,
                    modifier = Modifier.padding(top = 8.dp, start = 40.dp, end = 40.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Sort toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(theme.cardBackgroundColor, RoundedCornerShape(8.dp))
                    ) {
                        SortToggleButton("Recent", sortOrder == SortOrder.RECENT, theme) {
                            sortOrder = SortOrder.RECENT
                        }
                        SortToggleButton("Chapter Order", sortOrder == SortOrder.CHAPTER, theme) {
                            sortOrder = SortOrder.CHAPTER
                        }
                    }
                }

                items(favoriteVerses, key = { it.id }) { verse ->
                    FavoriteVerseRow(
                        verse = verse,
                        theme = theme,
                        fontSize = fontSize,
                        defaultLanguage = defaultLanguage,
                        onClick = { onVerseClick(verse.id) },
                        onRemove = { settings.toggleFavorite(verse.id) }
                    )
                    HorizontalDivider(
                        color = theme.cardBackgroundColor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteVerseRow(
    verse: Verse,
    theme: AppTheme,
    fontSize: Float,
    defaultLanguage: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val lang = if (defaultLanguage == "hindi") Language.HINDI else Language.ENGLISH
    val snippet = verse.translations
        .firstOrNull { it.language == lang }?.text
        ?.take(120)
        ?.let { if (it.length >= 120) "$it..." else it }
        ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Chapter ${verse.chapter}, Verse ${verse.verse}",
                fontSize = 12.sp,
                color = theme.accentColor
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            verse.slok.split("\n").firstOrNull() ?: "",
            fontSize = (fontSize - 2).sp,
            color = theme.primaryTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            snippet,
            fontSize = (fontSize - 4).sp,
            color = theme.secondaryTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SortToggleButton(
    label: String,
    isSelected: Boolean,
    theme: AppTheme,
    onClick: () -> Unit
) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (isSelected) Color.White else theme.secondaryTextColor,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) theme.accentColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
}
