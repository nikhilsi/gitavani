package com.nikhilsi.gitavani.ui.verses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilsi.gitavani.model.VerseWords
import com.nikhilsi.gitavani.model.WordPart
import com.nikhilsi.gitavani.theme.AppTheme

/**
 * "Word by word" section on the reading screen: each word of the verse with its meaning.
 * Closed by default; the open or closed state is remembered in AppSettings.
 */
@Composable
fun WordByWordSection(
    words: VerseWords,
    expanded: Boolean,
    onToggle: () -> Unit,
    theme: AppTheme,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    val lineGroups = words.lines.filter { it.parts.isNotEmpty() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.cardBackgroundColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .semantics {
                    contentDescription =
                        if (expanded) "Hide word by word meanings" else "Show word by word meanings"
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Word by word",
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.primaryTextColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = theme.accentColor
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (words.available && lineGroups.isNotEmpty()) {
                    lineGroups.forEachIndexed { index, line ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = theme.secondaryTextColor.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        line.parts.forEach { part ->
                            WordPartRow(part = part, theme = theme, fontSize = fontSize)
                        }
                    }
                    Text(
                        "Word meanings: Swami Sivananda",
                        fontSize = maxOf(fontSize - 5f, 11f).sp,
                        color = theme.secondaryTextColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else {
                    Text(
                        "Word meanings aren't available for this verse yet.",
                        fontSize = (fontSize - 2).sp,
                        color = theme.secondaryTextColor
                    )
                }
            }
        }
    }
}

/** One word: Devanagari and Roman letters on the first line, its meaning below. */
@Composable
private fun WordPartRow(part: WordPart, theme: AppTheme, fontSize: Float) {
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(part.deva, fontSize = fontSize.sp, color = theme.primaryTextColor)
            Spacer(Modifier.width(8.dp))
            Text(
                part.roman,
                fontSize = (fontSize - 3).sp,
                fontStyle = FontStyle.Italic,
                color = theme.secondaryTextColor
            )
        }
        Text(
            part.meaning,
            fontSize = (fontSize - 2).sp,
            color = theme.primaryTextColor,
            lineHeight = (fontSize + 2).sp
        )
    }
}
