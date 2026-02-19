package com.nikhilsi.gitavani.ui.verses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.theme.AppTheme

@Composable
fun ActionBar(
    isPlaying: Boolean,
    showTransliteration: Boolean,
    isFavorite: Boolean,
    theme: AppTheme,
    onPlayPause: () -> Unit,
    onToggleTransliteration: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        @Suppress("DEPRECATION")
        ActionBarButton(
            icon = { Icon(Icons.Default.VolumeUp, null, tint = theme.accentColor) },
            label = if (isPlaying) "Pause" else "Play",
            color = theme.accentColor,
            contentDesc = if (isPlaying) "Pause audio" else "Play audio",
            onClick = onPlayPause,
            modifier = Modifier.weight(1f)
        )
        ActionBarButton(
            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = theme.accentColor) },
            label = "Romanize",
            color = theme.accentColor,
            contentDesc = if (showTransliteration) "Hide transliteration" else "Show transliteration",
            onClick = onToggleTransliteration,
            modifier = Modifier.weight(1f)
        )
        ActionBarButton(
            icon = {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (isFavorite) Color.Red else theme.accentColor
                )
            },
            label = if (isFavorite) "Saved" else "Save",
            color = if (isFavorite) Color.Red else theme.accentColor,
            contentDesc = if (isFavorite) "Remove from favorites" else "Add to favorites",
            onClick = onToggleFavorite,
            modifier = Modifier.weight(1f)
        )
        ActionBarButton(
            icon = { Icon(Icons.Default.Share, null, tint = theme.accentColor) },
            label = "Share",
            color = theme.accentColor,
            contentDesc = "Share verse",
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionBarButton(
    icon: @Composable () -> Unit,
    label: String,
    color: Color,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = contentDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        icon()
        Text(
            text = label,
            fontSize = 10.sp,
            color = color
        )
    }
}
