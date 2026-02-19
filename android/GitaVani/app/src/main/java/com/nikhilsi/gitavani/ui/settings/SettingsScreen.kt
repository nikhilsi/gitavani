package com.nikhilsi.gitavani.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    theme: AppTheme,
    onAboutClick: () -> Unit,
    onBack: () -> Unit
) {
    val fontSize by settings.fontSize.collectAsState()
    val defaultLanguage by settings.defaultLanguage.collectAsState()
    val showTransliteration by settings.showTransliteration.collectAsState()
    val selectedTheme by settings.selectedTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = theme.primaryTextColor) },
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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Theme picker
            ThemePickerSection(
                currentTheme = selectedTheme,
                displayTheme = theme,
                onThemeSelect = { settings.setSelectedTheme(it) }
            )

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            // Font size
            FontSizeSection(
                fontSize = fontSize,
                theme = theme,
                onFontSizeChange = { settings.setFontSize(it) }
            )

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            // Default language
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Default Language",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primaryTextColor
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.cardBackgroundColor, RoundedCornerShape(8.dp))
                ) {
                    ToggleButton("English", defaultLanguage == "english", theme, Modifier.weight(1f)) {
                        settings.setDefaultLanguage("english")
                    }
                    ToggleButton("Hindi", defaultLanguage == "hindi", theme, Modifier.weight(1f)) {
                        settings.setDefaultLanguage("hindi")
                    }
                }
            }

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            // Transliteration toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Transliteration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryTextColor
                    )
                    Text(
                        "Show romanized Sanskrit text",
                        fontSize = 12.sp,
                        color = theme.secondaryTextColor
                    )
                }
                Switch(
                    checked = showTransliteration,
                    onCheckedChange = { settings.setShowTransliteration(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = theme.accentColor
                    )
                )
            }

            HorizontalDivider(color = theme.secondaryTextColor.copy(alpha = 0.3f))

            // About
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAboutClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "About GitaVani",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryTextColor
                    )
                    Text(
                        "Credits, license, and privacy",
                        fontSize = 12.sp,
                        color = theme.secondaryTextColor
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = theme.secondaryTextColor
                )
            }
        }
    }
}

@Composable
private fun ThemePickerSection(
    currentTheme: String,
    displayTheme: AppTheme,
    onThemeSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Theme",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = displayTheme.primaryTextColor
        )

        // 2x2 grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemePreviewCard(AppTheme.Sattva, currentTheme == "sattva", displayTheme, Modifier.weight(1f)) {
                    onThemeSelect("sattva")
                }
                ThemePreviewCard(AppTheme.Parchment, currentTheme == "parchment", displayTheme, Modifier.weight(1f)) {
                    onThemeSelect("parchment")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemePreviewCard(AppTheme.Dusk, currentTheme == "dusk", displayTheme, Modifier.weight(1f)) {
                    onThemeSelect("dusk")
                }
                ThemePreviewCard(AppTheme.Lotus, currentTheme == "lotus", displayTheme, Modifier.weight(1f)) {
                    onThemeSelect("lotus")
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    themeOption: AppTheme,
    isSelected: Boolean,
    displayTheme: AppTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(themeOption.backgroundColor)
                .then(
                    if (isSelected) Modifier.border(2.dp, themeOption.accentColor, RoundedCornerShape(8.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aa", fontSize = 20.sp, color = themeOption.primaryTextColor)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth(0.2f)
                        .background(themeOption.accentColor, RoundedCornerShape(2.dp))
                )
            }
        }
        Text(
            themeOption.displayName,
            fontSize = 12.sp,
            color = if (isSelected) displayTheme.accentColor else displayTheme.secondaryTextColor
        )
    }
}

@Composable
private fun FontSizeSection(
    fontSize: Float,
    theme: AppTheme,
    onFontSizeChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Font Size",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.primaryTextColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("A", fontSize = 14.sp, color = theme.secondaryTextColor)
            Slider(
                value = fontSize,
                onValueChange = onFontSizeChange,
                valueRange = 14f..28f,
                steps = 13,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = theme.accentColor,
                    activeTrackColor = theme.accentColor
                )
            )
            Text("A", fontSize = 28.sp, color = theme.secondaryTextColor)
        }
        Text(
            "Preview text at ${fontSize.toInt()}pt",
            fontSize = fontSize.sp,
            color = theme.primaryTextColor
        )
    }
}

@Composable
private fun ToggleButton(
    label: String,
    isSelected: Boolean,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) theme.accentColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else theme.secondaryTextColor
        )
    }
}
