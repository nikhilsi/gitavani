package com.nikhilsi.gitavani.ui.verses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.theme.AppTheme

@Composable
fun ShlokSection(
    slok: String,
    transliteration: String,
    showTransliteration: Boolean,
    theme: AppTheme,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.cardBackgroundColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = slok,
            fontSize = (fontSize + 2).sp,
            color = theme.primaryTextColor,
            textAlign = TextAlign.Center,
            lineHeight = (fontSize + 8).sp,
            modifier = Modifier.fillMaxWidth()
        )

        if (showTransliteration) {
            HorizontalDivider(
                color = theme.secondaryTextColor.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(
                text = transliteration,
                fontSize = (fontSize - 2).sp,
                fontStyle = FontStyle.Italic,
                color = theme.secondaryTextColor,
                textAlign = TextAlign.Center,
                lineHeight = (fontSize + 2).sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
