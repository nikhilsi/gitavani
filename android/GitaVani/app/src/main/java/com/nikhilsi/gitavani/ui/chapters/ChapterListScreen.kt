package com.nikhilsi.gitavani.ui.chapters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.data.GitaDataService
import com.nikhilsi.gitavani.model.Language
import com.nikhilsi.gitavani.model.Verse
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.state.ReadingProgress
import com.nikhilsi.gitavani.theme.AppTheme
import com.nikhilsi.gitavani.ui.common.ResumeReadingBanner
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    dataService: GitaDataService,
    settings: AppSettings,
    readingProgress: ReadingProgress,
    theme: AppTheme,
    onChapterClick: (Int) -> Unit,
    onVerseClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val fontSize by settings.fontSize.collectAsState()
    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val favoriteIds by settings.favoriteVerseIds.collectAsState()
    val lastChapter by readingProgress.lastReadChapter.collectAsState()
    val lastVerse by readingProgress.lastReadVerse.collectAsState()
    val hasProgress = lastChapter > 0 && lastVerse > 0

    var searchText by remember { mutableStateOf("") }
    var debouncedText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    // Debounce search text
    LaunchedEffect(searchText) {
        delay(300)
        debouncedText = searchText
    }

    val isSearching = debouncedText.trim().length >= 2
    val searchResults = remember(debouncedText, isSearching) {
        if (!isSearching) emptyList()
        else {
            val query = debouncedText.lowercase()
            dataService.verses.filter { verse ->
                verse.slok.lowercase().contains(query) ||
                verse.transliteration.lowercase().contains(query) ||
                verse.translations.any { it.text.lowercase().contains(query) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onHelpClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Help",
                            tint = theme.accentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { searchActive = true }) {
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
            // Search bar
            if (searchActive) {
                SearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text("Search verses...") },
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
                if (isSearching) {
                    // Search results
                    if (searchResults.isEmpty()) {
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
                                    "No results for \"$debouncedText\"",
                                    fontSize = 14.sp,
                                    color = theme.secondaryTextColor
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                "${searchResults.size} result${if (searchResults.size == 1) "" else "s"}",
                                fontSize = 12.sp,
                                color = theme.secondaryTextColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchResults, key = { it.id }) { verse ->
                            SearchResultRow(
                                verse = verse,
                                query = debouncedText,
                                theme = theme,
                                fontSize = fontSize,
                                defaultLanguage = defaultLanguage,
                                onClick = { onVerseClick(verse.id) }
                            )
                            HorizontalDivider(
                                color = theme.cardBackgroundColor,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // Book cover header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "GitaVani",
                                fontSize = (fontSize + 10).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = theme.primaryTextColor
                            )
                            Text(
                                text = "The Bhagavad Gita",
                                fontSize = fontSize.sp,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                color = theme.secondaryTextColor
                            )
                            Text(
                                text = "${dataService.chapters.size} chapters  ·  ${dataService.verses.size} verses",
                                fontSize = (fontSize - 4).sp,
                                color = theme.secondaryTextColor
                            )
                        }
                    }

                    // Resume reading banner
                    if (hasProgress) {
                        val chapter = dataService.chapter(lastChapter)
                        if (chapter != null) {
                            item {
                                ResumeReadingBanner(
                                    chapter = lastChapter,
                                    verse = lastVerse,
                                    chapterName = chapter.meaning.oppositeLanguage(defaultLanguage),
                                    theme = theme,
                                    onTap = {
                                        onVerseClick("BG${lastChapter}.${lastVerse}")
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Chapter list
                    items(dataService.chapters, key = { it.chapterNumber }) { chapter ->
                        ChapterRowItem(
                            chapter = chapter,
                            theme = theme,
                            fontSize = fontSize,
                            defaultLanguage = defaultLanguage,
                            modifier = Modifier
                                .clickable { onChapterClick(chapter.chapterNumber) }
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
}

@Composable
private fun SearchResultRow(
    verse: Verse,
    query: String,
    theme: AppTheme,
    fontSize: Float,
    defaultLanguage: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Chapter ${verse.chapter}, Verse ${verse.verse}",
            fontSize = 12.sp,
            color = theme.accentColor
        )
        val snippet = matchSnippet(verse, query, defaultLanguage)
        if (snippet != null) {
            Text(
                snippet,
                fontSize = (fontSize - 2).sp,
                color = theme.primaryTextColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun matchSnippet(verse: Verse, query: String, defaultLanguage: String): String? {
    val q = query.lowercase()
    val lang = if (defaultLanguage == "hindi") Language.HINDI else Language.ENGLISH

    // Preferred language translation
    verse.translations
        .firstOrNull { it.language == lang && it.text.lowercase().contains(q) }
        ?.let { return snippetAround(it.text, q) }

    // Any translation
    verse.translations
        .firstOrNull { it.text.lowercase().contains(q) }
        ?.let { return snippetAround(it.text, q) }

    // Transliteration
    if (verse.transliteration.lowercase().contains(q)) {
        return snippetAround(verse.transliteration, q)
    }

    // Sanskrit
    if (verse.slok.lowercase().contains(q)) {
        return verse.slok.split("\n").firstOrNull()
    }

    return null
}

private fun snippetAround(text: String, query: String): String {
    val lowerText = text.lowercase()
    val matchIndex = lowerText.indexOf(query)
    if (matchIndex < 0) return text.take(120)

    val snippetStart = maxOf(0, matchIndex - 40)
    val snippetEnd = minOf(text.length, snippetStart + 120)
    var snippet = text.substring(snippetStart, snippetEnd)
    if (snippetStart > 0) snippet = "...$snippet"
    if (snippetEnd < text.length) snippet = "$snippet..."
    return snippet
}
