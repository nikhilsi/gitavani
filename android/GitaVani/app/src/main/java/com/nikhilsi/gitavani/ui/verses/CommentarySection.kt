package com.nikhilsi.gitavani.ui.verses

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.model.Commentary
import com.nikhilsi.gitavani.model.Language
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.theme.AppTheme

private const val TRUNCATION_LIMIT = 1500

private val Language.displayName: String
    get() = when (this) {
        Language.ENGLISH -> "English"
        Language.HINDI -> "Hindi"
        Language.SANSKRIT -> "Sanskrit"
    }

@Composable
fun CommentarySection(
    commentaries: List<Commentary>,
    theme: AppTheme,
    fontSize: Float,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    if (commentaries.isEmpty()) return

    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val prefCommentaryLang by settings.preferredCommentaryLanguage.collectAsState()
    val prefHindiAuthor by settings.preferredHindiCommentaryAuthor.collectAsState()
    val prefEnglishAuthor by settings.preferredEnglishCommentaryAuthor.collectAsState()
    val prefSanskritAuthor by settings.preferredSanskritCommentaryAuthor.collectAsState()

    var selectedLanguage by remember { mutableStateOf(Language.ENGLISH) }
    var selectedAuthor by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    val availableLanguages = Language.entries.filter { lang ->
        commentaries.any { it.language == lang }
    }
    val filtered = commentaries.filter { it.language == selectedLanguage }
    val authors = filtered.map { it.author }.distinct().sorted()
    val current = filtered.firstOrNull { it.author == selectedAuthor } ?: filtered.firstOrNull()
    val needsTruncation = (current?.text?.length ?: 0) > TRUNCATION_LIMIT
    val displayText = if (needsTruncation && !isExpanded) {
        current?.text?.take(TRUNCATION_LIMIT) + "..."
    } else {
        current?.text ?: ""
    }

    // Setup defaults
    LaunchedEffect(commentaries) {
        selectedLanguage = when {
            prefCommentaryLang.isNotEmpty() -> {
                val lang = Language.entries.firstOrNull { it.name.lowercase() == prefCommentaryLang }
                if (lang != null && availableLanguages.contains(lang)) lang
                else if (defaultLanguage == "hindi" && availableLanguages.contains(Language.HINDI)) Language.HINDI
                else availableLanguages.firstOrNull() ?: Language.ENGLISH
            }
            defaultLanguage == "hindi" && availableLanguages.contains(Language.HINDI) -> Language.HINDI
            availableLanguages.contains(Language.ENGLISH) -> Language.ENGLISH
            else -> availableLanguages.firstOrNull() ?: Language.ENGLISH
        }
    }

    LaunchedEffect(selectedLanguage, commentaries) {
        isExpanded = false
        val preferred = when (selectedLanguage) {
            Language.HINDI -> prefHindiAuthor
            Language.ENGLISH -> prefEnglishAuthor
            Language.SANSKRIT -> prefSanskritAuthor
        }
        val authorsForLang = commentaries.filter { it.language == selectedLanguage }.map { it.author }.distinct().sorted()
        selectedAuthor = if (preferred.isNotEmpty() && authorsForLang.contains(preferred)) preferred
            else authorsForLang.firstOrNull() ?: ""
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Commentary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.primaryTextColor
        )

        // Language toggle
        if (availableLanguages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.cardBackgroundColor, RoundedCornerShape(8.dp))
            ) {
                availableLanguages.forEach { lang ->
                    LanguageToggleBtn(lang.displayName, selectedLanguage == lang, theme) {
                        selectedLanguage = lang
                        settings.setPreferredCommentaryLanguage(lang.name.lowercase())
                    }
                }
            }
        }

        // Author picker
        if (authors.size > 1) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                authors.forEach { author ->
                    val isSelected = author == selectedAuthor
                    Text(
                        text = author,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else theme.secondaryTextColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) theme.accentColor else theme.cardBackgroundColor)
                            .clickable {
                                selectedAuthor = author
                                isExpanded = false
                                when (selectedLanguage) {
                                    Language.HINDI -> settings.setPreferredHindiCommentaryAuthor(author)
                                    Language.ENGLISH -> settings.setPreferredEnglishCommentaryAuthor(author)
                                    Language.SANSKRIT -> settings.setPreferredSanskritCommentaryAuthor(author)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Commentary text
        if (current != null) {
            Text(
                text = displayText,
                fontSize = fontSize.sp,
                color = theme.primaryTextColor,
                lineHeight = (fontSize + 4).sp,
                modifier = Modifier.animateContentSize()
            )

            if (needsTruncation) {
                Text(
                    text = if (isExpanded) "Show less" else "Read more...",
                    fontSize = 14.sp,
                    color = theme.accentColor,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            Text(
                text = "— ${current.author}",
                fontSize = 12.sp,
                color = theme.secondaryTextColor
            )
        } else {
            Text(
                text = "No commentary available for this language.",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = theme.secondaryTextColor
            )
        }
    }
}

@Composable
private fun LanguageToggleBtn(
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
