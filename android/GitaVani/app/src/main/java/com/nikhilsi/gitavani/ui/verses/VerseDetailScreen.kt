package com.nikhilsi.gitavani.ui.verses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.nikhilsi.gitavani.audio.AudioService
import com.nikhilsi.gitavani.data.GitaDataService
import com.nikhilsi.gitavani.model.Verse
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.state.ReadingProgress
import com.nikhilsi.gitavani.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseDetailScreen(
    initialVerseId: String,
    dataService: GitaDataService,
    settings: AppSettings,
    readingProgress: ReadingProgress,
    audioService: AudioService,
    theme: AppTheme,
    onSettingsClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allVerses = dataService.verses
    val initialIndex = allVerses.indexOfFirst { it.id == initialVerseId }.coerceAtLeast(0)
    val fontSize by settings.fontSize.collectAsState()
    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val showTransliteration by settings.showTransliteration.collectAsState()
    val favoriteIds by settings.favoriteVerseIds.collectAsState()
    val isPlaying by audioService.isPlaying.collectAsState()
    val currentAudioVerseId by audioService.currentVerseId.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = initialIndex) { allVerses.size }

    // Stop audio when verse changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val verse = allVerses.getOrNull(page)
            if (verse != null) {
                readingProgress.update(verse.chapter, verse.verse)
                // Stop audio if navigated away from the playing verse
                if (currentAudioVerseId != null && currentAudioVerseId != verse.id) {
                    audioService.stop()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            val currentVerse = allVerses.getOrNull(pagerState.currentPage)
            TopAppBar(
                title = {
                    if (currentVerse != null) {
                        Text(
                            "Chapter ${currentVerse.chapter}, Verse ${currentVerse.verse}",
                            color = theme.primaryTextColor,
                            fontSize = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.accentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
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
        bottomBar = {
            VerseNavigationBar(
                hasPrevious = pagerState.currentPage > 0,
                hasNext = pagerState.currentPage < allVerses.size - 1,
                theme = theme,
                onPrevious = {
                    val target = pagerState.currentPage - 1
                    if (target >= 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(target)
                        }
                    }
                },
                onNext = {
                    val target = pagerState.currentPage + 1
                    if (target < allVerses.size) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(target)
                        }
                    }
                }
            )
        },
        containerColor = theme.backgroundColor
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            key = { allVerses[it].id }
        ) { page ->
            val verse = allVerses[page]
            VerseContent(
                verse = verse,
                theme = theme,
                fontSize = fontSize,
                showTransliteration = showTransliteration,
                isFavorite = favoriteIds.contains(verse.id),
                settings = settings,
                onToggleTransliteration = {
                    settings.setShowTransliteration(!showTransliteration)
                },
                onToggleFavorite = { settings.toggleFavorite(verse.id) },
                onPlayPause = { audioService.play(verse.id) },
                onShare = {
                    val lang = if (defaultLanguage == "hindi") com.nikhilsi.gitavani.model.Language.HINDI else com.nikhilsi.gitavani.model.Language.ENGLISH
                    val translation = verse.translations.firstOrNull { it.language == lang }?.text
                        ?: verse.translations.firstOrNull()?.text ?: ""
                    val shareText = buildString {
                        appendLine("Bhagavad Gita — Chapter ${verse.chapter}, Verse ${verse.verse}")
                        appendLine()
                        appendLine(verse.slok)
                        appendLine()
                        appendLine(translation)
                        appendLine()
                        append("— GitaVani")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Share Verse"))
                    } catch (_: ActivityNotFoundException) { }
                },
                isPlaying = isPlaying && currentAudioVerseId == verse.id
            )
        }
    }
}

@Composable
private fun VerseContent(
    verse: Verse,
    theme: AppTheme,
    fontSize: Float,
    showTransliteration: Boolean,
    isFavorite: Boolean,
    isPlaying: Boolean,
    settings: AppSettings,
    onToggleTransliteration: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Sanskrit shlok
        ShlokSection(
            slok = verse.slok,
            transliteration = verse.transliteration,
            showTransliteration = showTransliteration,
            theme = theme,
            fontSize = fontSize
        )

        // Action bar
        ActionBar(
            isPlaying = isPlaying,
            showTransliteration = showTransliteration,
            isFavorite = isFavorite,
            theme = theme,
            onPlayPause = onPlayPause,
            onToggleTransliteration = onToggleTransliteration,
            onToggleFavorite = onToggleFavorite,
            onShare = onShare
        )

        // Translation
        TranslationSection(
            translations = verse.translations,
            theme = theme,
            fontSize = fontSize,
            settings = settings
        )

        HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

        // Commentary
        CommentarySection(
            commentaries = verse.commentaries,
            theme = theme,
            fontSize = fontSize,
            settings = settings
        )
    }
}

@Composable
private fun VerseNavigationBar(
    hasPrevious: Boolean,
    hasNext: Boolean,
    theme: AppTheme,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onPrevious,
                enabled = hasPrevious
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = if (hasPrevious) theme.accentColor else theme.secondaryTextColor.copy(alpha = 0.4f)
                )
                Text(
                    "Previous",
                    color = if (hasPrevious) theme.accentColor else theme.secondaryTextColor.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
            TextButton(
                onClick = onNext,
                enabled = hasNext
            ) {
                Text(
                    "Next",
                    color = if (hasNext) theme.accentColor else theme.secondaryTextColor.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (hasNext) theme.accentColor else theme.secondaryTextColor.copy(alpha = 0.4f)
                )
            }
        }
    }
}
