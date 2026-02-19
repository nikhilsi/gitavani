package com.nikhilsi.gitavani.ui.chapters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.model.Chapter
import com.nikhilsi.gitavani.theme.AppTheme

@Composable
fun ChapterRowItem(
    chapter: Chapter,
    theme: AppTheme,
    fontSize: Float,
    defaultLanguage: String,
    modifier: Modifier = Modifier
) {
    val title = if (defaultLanguage == "hindi") chapter.name else chapter.transliteration
    val subtitle = chapter.meaning.oppositeLanguage(defaultLanguage)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "${chapter.chapterNumber}",
            fontSize = (fontSize + 4).sp,
            fontWeight = FontWeight.Bold,
            color = theme.accentColor,
            modifier = Modifier.width(40.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.primaryTextColor
            )
            Text(
                text = subtitle,
                fontSize = (fontSize - 2).sp,
                color = theme.secondaryTextColor
            )
        }

        Text(
            text = "${chapter.versesCount}",
            fontSize = (fontSize - 4).sp,
            color = theme.secondaryTextColor,
            modifier = Modifier
                .background(theme.cardBackgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
