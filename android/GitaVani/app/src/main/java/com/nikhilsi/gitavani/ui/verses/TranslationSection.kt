package com.nikhilsi.gitavani.ui.verses

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.model.Language
import com.nikhilsi.gitavani.model.Translation
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.theme.AppTheme

@Composable
fun TranslationSection(
    translations: List<Translation>,
    theme: AppTheme,
    fontSize: Float,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val prefHindiAuthor by settings.preferredHindiAuthor.collectAsState()
    val prefEnglishAuthor by settings.preferredEnglishAuthor.collectAsState()

    var selectedLanguage by remember { mutableStateOf(Language.ENGLISH) }
    var selectedAuthor by remember { mutableStateOf("") }

    val filtered = translations.filter { it.language == selectedLanguage }
    val authors = filtered.map { it.author }.distinct().sorted()
    val current = filtered.firstOrNull { it.author == selectedAuthor } ?: filtered.firstOrNull()

    // Setup defaults
    LaunchedEffect(defaultLanguage, translations) {
        selectedLanguage = if (defaultLanguage == "hindi") Language.HINDI else Language.ENGLISH
    }

    LaunchedEffect(selectedLanguage, translations) {
        val preferred = if (selectedLanguage == Language.HINDI) prefHindiAuthor else prefEnglishAuthor
        val authorsForLang = translations.filter { it.language == selectedLanguage }.map { it.author }.distinct().sorted()
        selectedAuthor = if (preferred.isNotEmpty() && authorsForLang.contains(preferred)) preferred
            else authorsForLang.firstOrNull() ?: ""
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Language toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.cardBackgroundColor, RoundedCornerShape(8.dp))
        ) {
            LanguageToggleButton("English", selectedLanguage == Language.ENGLISH, theme) {
                selectedLanguage = Language.ENGLISH
            }
            LanguageToggleButton("Hindi", selectedLanguage == Language.HINDI, theme) {
                selectedLanguage = Language.HINDI
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
                                if (selectedLanguage == Language.HINDI) settings.setPreferredHindiAuthor(author)
                                else settings.setPreferredEnglishAuthor(author)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Translation text
        if (current != null) {
            Text(
                text = current.text,
                fontSize = fontSize.sp,
                color = theme.primaryTextColor,
                lineHeight = (fontSize + 4).sp
            )
            Text(
                text = "— ${current.author}",
                fontSize = 12.sp,
                color = theme.secondaryTextColor
            )
        }
    }
}

@Composable
private fun LanguageToggleButton(
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
