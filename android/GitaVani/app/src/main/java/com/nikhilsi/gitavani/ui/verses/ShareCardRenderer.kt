package com.nikhilsi.gitavani.ui.verses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.nikhilsi.gitavani.model.Translation
import com.nikhilsi.gitavani.model.Verse
import com.nikhilsi.gitavani.theme.AppTheme
import java.io.File

object ShareCardRenderer {

    fun render(
        verse: Verse,
        translation: Translation?,
        showTransliteration: Boolean,
        theme: AppTheme
    ): Bitmap {
        val width = 1080
        val padding = 96f
        val contentWidth = width - (padding * 2)

        // Paints
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 42f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            color = theme.secondaryTextColor.toArgb()
            textAlign = Paint.Align.CENTER
        }

        val chapterPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 36f
            color = theme.accentColor.toArgb()
            textAlign = Paint.Align.CENTER
        }

        val shlokPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 54f
            color = theme.primaryTextColor.toArgb()
        }

        val translitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 39f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            color = theme.secondaryTextColor.toArgb()
        }

        val translationPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 45f
            color = theme.primaryTextColor.toArgb()
        }

        val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 33f
            color = theme.secondaryTextColor.toArgb()
            textAlign = Paint.Align.CENTER
        }

        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 33f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            color = theme.accentColor.copy(alpha = 0.6f).toArgb()
            textAlign = Paint.Align.CENTER
        }

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentColor.copy(alpha = 0.3f).toArgb()
            strokeWidth = 3f
        }

        // Pre-layout text blocks
        val centerX = width / 2f
        val cw = contentWidth.toInt()

        val shlokLayout = StaticLayout.Builder.obtain(verse.slok, 0, verse.slok.length, shlokPaint, cw)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(18f, 1f)
            .build()

        val translitLayout = if (showTransliteration) {
            StaticLayout.Builder.obtain(verse.transliteration, 0, verse.transliteration.length, translitPaint, cw)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(10f, 1f)
                .build()
        } else null

        val translationLayout = if (translation != null) {
            StaticLayout.Builder.obtain(translation.text, 0, translation.text.length, translationPaint, cw)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(12f, 1f)
                .build()
        } else null

        // Calculate total height
        var totalHeight = padding // top padding
        totalHeight += 42f + 20f  // "Bhagavad Gita" header + spacing
        totalHeight += 36f + 40f  // chapter/verse + spacing
        totalHeight += shlokLayout.height + 40f  // shlok + spacing
        if (translitLayout != null) {
            totalHeight += translitLayout.height + 40f
        }
        totalHeight += 3f + 40f  // divider + spacing
        if (translationLayout != null) {
            totalHeight += translationLayout.height + 20f  // translation + spacing
            totalHeight += 33f + 30f  // author + spacing
        }
        totalHeight += 33f  // branding
        totalHeight += padding  // bottom padding

        // Create bitmap
        val bitmap = Bitmap.createBitmap(width, totalHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(theme.backgroundColor.toArgb())

        var y = padding

        // "Bhagavad Gita"
        canvas.drawText("Bhagavad Gita", centerX, y + 42f, headerPaint)
        y += 42f + 20f

        // Chapter, Verse
        canvas.drawText("Chapter ${verse.chapter}, Verse ${verse.verse}", centerX, y + 36f, chapterPaint)
        y += 36f + 40f

        // Sanskrit shlok
        canvas.save()
        canvas.translate(padding, y)
        shlokLayout.draw(canvas)
        canvas.restore()
        y += shlokLayout.height + 40f

        // Transliteration
        if (translitLayout != null) {
            canvas.save()
            canvas.translate(padding, y)
            translitLayout.draw(canvas)
            canvas.restore()
            y += translitLayout.height + 40f
        }

        // Divider
        canvas.drawLine(centerX - 90f, y, centerX + 90f, y, dividerPaint)
        y += 3f + 40f

        // Translation
        if (translationLayout != null) {
            canvas.save()
            canvas.translate(padding, y)
            translationLayout.draw(canvas)
            canvas.restore()
            y += translationLayout.height + 20f

            // Author
            canvas.drawText("— ${translation!!.author}", centerX, y + 33f, authorPaint)
            y += 33f + 30f
        }

        // Branding
        canvas.drawText("\uD83D\uDCD6 GitaVani", centerX, y + 33f, brandPaint)

        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): android.net.Uri? {
        return try {
            val cachePath = File(context.cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "gitavani_verse.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}
