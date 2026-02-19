package com.nikhilsi.gitavani.ui.chapters

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.data.GitaDataService
import com.nikhilsi.gitavani.model.Verse
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.theme.AppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapterNumber: Int,
    dataService: GitaDataService,
    settings: AppSettings,
    theme: AppTheme,
    onVerseClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onBack: () -> Unit
) {
    val fontSize by settings.fontSize.collectAsState()
    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val favoriteIds by settings.favoriteVerseIds.collectAsState()
    val chapter = dataService.chapter(chapterNumber)
    val verses = dataService.versesForChapter(chapterNumber)
    var showFullSummary by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }
    var debouncedText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    LaunchedEffect(searchText) {
        delay(300)
        debouncedText = searchText
    }

    val isSearching = debouncedText.trim().length >= 2
    val filteredVerses = remember(debouncedText, isSearching, verses) {
        if (!isSearching) verses
        else {
            val query = debouncedText.lowercase()
            verses.filter { verse ->
                verse.slok.lowercase().contains(query) ||
                verse.transliteration.lowercase().contains(query) ||
                verse.translations.any { it.text.lowercase().contains(query) }
            }
        }
    }

    val chapterTitle = if (defaultLanguage == "hindi") chapter?.name ?: "" else chapter?.transliteration ?: ""
    val chapterSubtitle = chapter?.meaning?.oppositeLanguage(defaultLanguage) ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chapter $chapterNumber",
                        color = theme.primaryTextColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.accentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { searchActive = !searchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.accentColor
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = if (favoriteIds.isEmpty()) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = theme.accentColor
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = theme.accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.backgroundColor
                )
            )
        },
        containerColor = theme.backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (searchActive) {
                SearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text("Search this chapter...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = theme.secondaryTextColor)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchText = ""
                            debouncedText = ""
                            searchActive = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search", tint = theme.secondaryTextColor)
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = theme.cardBackgroundColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) { }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Chapter summary card (hide when searching)
                if (!isSearching && chapter != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(theme.cardBackgroundColor, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = chapterTitle,
                                fontSize = (fontSize + 2).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryTextColor
                            )
                            Text(
                                text = chapterSubtitle,
                                fontSize = (fontSize - 2).sp,
                                color = theme.secondaryTextColor
                            )
                            Text(
                                text = chapter.summary.forLanguage(defaultLanguage),
                                fontSize = fontSize.sp,
                                color = theme.primaryTextColor,
                                maxLines = if (showFullSummary) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.animateContentSize()
                            )
                            Text(
                                text = if (showFullSummary) "Show less" else "Read more...",
                                fontSize = (fontSize - 4).sp,
                                color = theme.accentColor,
                                modifier = Modifier.clickable { showFullSummary = !showFullSummary }
                            )
                        }
                    }
                }

                if (isSearching && filteredVerses.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = theme.secondaryTextColor.copy(alpha = 0.5f)
                            )
                            Text(
                                "No results in this chapter",
                                fontSize = 14.sp,
                                color = theme.secondaryTextColor
                            )
                        }
                    }
                }

                // Verse list
                items(filteredVerses, key = { it.id }) { verse ->
                    VerseListRowItem(
                        verse = verse,
                        theme = theme,
                        fontSize = fontSize,
                        modifier = Modifier
                            .clickable { onVerseClick(verse.id) }
                            .padding(horizontal = 16.dp)
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
private fun VerseListRowItem(
    verse: Verse,
    theme: AppTheme,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Verse ${verse.verse}",
            fontSize = (fontSize - 2).sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.accentColor
        )
        Text(
            text = verse.slok.split("\n").firstOrNull() ?: "",
            fontSize = fontSize.sp,
            color = theme.primaryTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
