package com.nikhilsi.gitavani.ui.learn

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikhilsi.gitavani.audio.AudioService
import com.nikhilsi.gitavani.learn.ChantListener
import com.nikhilsi.gitavani.learn.ChantScorer
import com.nikhilsi.gitavani.model.VerseWords
import com.nikhilsi.gitavani.state.LearnProgress
import com.nikhilsi.gitavani.theme.AppTheme
import com.nikhilsi.gitavani.ui.learn.LessonState.Setup
import com.nikhilsi.gitavani.ui.learn.LessonState.Step
import com.nikhilsi.gitavani.ui.verses.WordPartRow

/** The lesson screen, titled "Abhyāsa" (spec D5, 6.3). Opened full screen from the verse's Learn button. */
@RequiresApi(33)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    verseId: String,
    words: VerseWords,
    audioService: AudioService,
    learnProgress: LearnProgress,
    theme: AppTheme,
    fontSize: Float,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    // Kept in a ViewModel tied to this screen, so rotating the phone doesn't restart the lesson.
    val holder: LessonHolder = viewModel(key = "lesson-$verseId") {
        LessonHolder(context.applicationContext, verseId, words, audioService, learnProgress)
    }
    val lesson = holder.lesson
    val micPrompt = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { lesson.micAnswered(it) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> lesson.appLeftForeground()
                Lifecycle.Event.ON_RESUME -> lesson.resumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abhyāsa", color = theme.primaryTextColor, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.accentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.backgroundColor),
            )
        },
        containerColor = theme.backgroundColor,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 700.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                when (val setup = lesson.setup) {
                    Setup.Ready -> Lesson(lesson, theme, fontSize, onClose)
                    Setup.Checking -> Box40 { CircularProgressIndicator(color = theme.accentColor) }
                    Setup.NeedsMic -> Notice(theme, fontSize, Icons.Default.Mic,
                        "GitaVani listens while you chant so it can show where you slipped. Audio stays on your phone.",
                        "Allow microphone") { micPrompt.launch(Manifest.permission.RECORD_AUDIO) }
                    Setup.MicDenied -> Notice(theme, fontSize, Icons.Default.Mic,
                        "The microphone is off for GitaVani. Turn it on in the app's settings to use Learn.",
                        "Open settings") {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)))
                    }
                    Setup.NeedsModel -> Notice(theme, fontSize, Icons.Default.Mic,
                        "Learn needs a download from Google: the Hindi speech model. It stays on your phone. Learn works offline after this.",
                        "Download") { lesson.downloadModel() }
                    is Setup.Downloading -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (setup.percent != null) {
                            LinearProgressIndicator(progress = { setup.percent / 100f }, modifier = Modifier.fillMaxWidth(), color = theme.accentColor)
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = theme.accentColor)
                        }
                        Text("Downloading from Google…", fontSize = (fontSize - 2).sp, color = theme.secondaryTextColor)
                    }
                    is Setup.Failed -> Notice(theme, fontSize, Icons.Default.Mic, setup.text, "Try again") { lesson.retrySetup() }
                }
            }
        }
    }
}

@Composable
private fun Box40(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) { content() }
}

@Composable
private fun Notice(theme: AppTheme, fontSize: Float, icon: ImageVector, text: String, button: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(40.dp))
        Text(text, fontSize = fontSize.sp, color = theme.primaryTextColor, textAlign = TextAlign.Center)
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)) { Text(button) }
    }
}

@RequiresApi(33)
@Composable
private fun Lesson(lesson: LessonState, theme: AppTheme, fontSize: Float, onClose: () -> Unit) {
    val phase by lesson.listener.phase.collectAsState()
    val listening = lesson.isRecording || phase != ChantListener.Phase.IDLE
    val step = lesson.step

    // Header
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stepTitle(lesson), fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold, color = theme.primaryTextColor)
        if (lesson.calibrated) {
            val (checked, total) = lesson.checkedCount
            Text("$checked of $total words checked", fontSize = (fontSize - 4).sp, color = theme.secondaryTextColor)
        }
    }

    VerseText(lesson, theme, fontSize)

    when (step) {
        Step.Listen -> {
            LaunchedEffect(Unit) { lesson.playVerse() }     // the lesson opens with the reciter chanting the verse
            OutlinedButton(
                onClick = { if (lesson.isPlayingReciter) lesson.stopReciter() else lesson.playVerse() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Icon(if (lesson.isPlayingReciter) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp, null, tint = theme.accentColor)
                Spacer(Modifier.width(8.dp))
                Text(if (lesson.isPlayingReciter) "Stop" else "Hear the verse", color = theme.accentColor)
            }
            Button(
                onClick = { lesson.begin() },
                enabled = lesson.calibrated,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
            ) { Text(if (lesson.calibrated) "Start learning" else "Getting ready…") }
            lesson.message?.let { Text(it, fontSize = (fontSize - 2).sp, color = theme.secondaryTextColor) }
        }
        Step.Learned -> Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Default.Verified, null, tint = theme.accentColor, modifier = Modifier.size(48.dp))
            Text("You chanted this verse from memory. It's marked Learned.", fontSize = fontSize.sp,
                color = theme.primaryTextColor, textAlign = TextAlign.Center)
            Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)) { Text("Done") }
        }
        else -> {
            // In From memory the parts would give the verse away. They show only after a take.
            if (step !is Step.Memory || lesson.result != null) PartsList(lesson, theme, fontSize)
            tipText(lesson)?.let { Tip(lesson, it, theme, fontSize) }
            lesson.message?.let { Text(it, fontSize = (fontSize - 2).sp, color = theme.secondaryTextColor) }

            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .clickable(role = Role.Button) { lesson.micTapped() }
                    .semantics { contentDescription = if (listening) "Stop listening" else "Start chanting" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(if (listening) Icons.Default.Stop else Icons.Default.Mic, null, tint = theme.accentColor, modifier = Modifier.size(64.dp))
                Text(if (listening) "Listening… tap when done" else "Tap and chant", fontSize = (fontSize - 2).sp, color = theme.accentColor)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallButton("Hear reciter", Icons.AutoMirrored.Filled.VolumeUp, theme, !listening, Modifier.weight(1f)) { lesson.playReciter() }
                if (lesson.lastTake != null) {
                    SmallButton("Hear my try", Icons.Default.PlayCircle, theme, !listening, Modifier.weight(1f)) { lesson.hearMyTry() }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (lesson.result != null) {
                    SmallButton("Try again", Icons.Default.Refresh, theme, !listening, Modifier.weight(1f)) { lesson.tryAgain() }
                }
                if (step != Step.Memory(hints = false)) {
                    val last = step is Step.Line && step.n + 1 >= lesson.practiceLines.size
                    SmallButton(if (step is Step.Line && !last) "Next line" else "Next", Icons.AutoMirrored.Filled.ArrowForward,
                        theme, !listening, Modifier.weight(1f)) { lesson.next() }
                }
            }
        }
    }
}

@Composable
private fun SmallButton(title: String, icon: ImageVector, theme: AppTheme, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp)) {
        Icon(icon, null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, color = theme.accentColor, fontSize = 13.sp)
    }
}

@RequiresApi(33)
private fun stepTitle(lesson: LessonState): String = when (val s = lesson.step) {
    Step.Listen -> "Listen"
    is Step.Line -> "Line ${s.n + 1} of ${lesson.practiceLines.size}"
    Step.Join -> "All lines together"
    is Step.Memory -> if (s.hints) "From memory, with first letters" else "From memory"
    Step.Learned -> "Learned"
}

@RequiresApi(33)
@Composable
private fun VerseText(lesson: LessonState, theme: AppTheme, fontSize: Float) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(theme.cardBackgroundColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val s = lesson.step) {
            is Step.Memory -> if (s.hints) {
                for (i in lesson.stepLines) {
                    // "yadā yadā hi" becomes "y… y… h…".
                    val hints = lesson.words.lines[i].roman.split(" ").filter { it.isNotEmpty() }.joinToString(" ") { "${it.first()}…" }
                    Text(hints, fontSize = (fontSize + 2).sp, color = theme.primaryTextColor)
                }
            } else {
                Text("Chant the verse from memory.", fontSize = fontSize.sp, color = theme.secondaryTextColor)
            }
            else -> {
                val shown = if (s == Step.Listen || s == Step.Learned) lesson.words.lines.indices.toList() else lesson.stepLines
                for (i in shown) {
                    val line = lesson.words.lines[i]
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(line.deva, fontSize = (fontSize + 2).sp,
                            color = if (line.speaker) theme.secondaryTextColor else theme.primaryTextColor)
                        Text(line.roman, fontSize = (fontSize - 2).sp, fontStyle = FontStyle.Italic, color = theme.secondaryTextColor)
                    }
                }
            }
        }
    }
}

@RequiresApi(33)
@Composable
private fun PartsList(lesson: LessonState, theme: AppTheme, fontSize: Float) {
    // A take where nothing was heard gets the tip only, not a row of Slipped marks.
    val marks = lesson.result?.takeIf { it.tipKind != ChantScorer.TipKind.NOTHING_HEARD }?.marks
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lesson.stepParts.forEachIndexed { index, item ->
            val mark = marks?.getOrNull(index)
            val name = when (mark) {
                ChantScorer.Mark.RIGHT -> ", right"
                ChantScorer.Mark.SLIPPED -> ", slipped"
                ChantScorer.Mark.NOT_CHECKED -> ", not checked"
                null -> ""
            }
            Row(
                Modifier.semantics(mergeDescendants = true) { contentDescription = "${item.part.roman}, ${item.part.meaning}$name" },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MarkSymbol(mark, theme, Modifier.padding(top = 4.dp))
                WordPartRow(item.part, theme, fontSize)
            }
        }
        if (lesson.showNotCheckedNote && marks?.contains(ChantScorer.Mark.NOT_CHECKED) == true) {
            lesson.notCheckedNoteShown = true
            Text("Not checked: the phone can't hear this word reliably yet.", fontSize = (fontSize - 4).sp, color = theme.secondaryTextColor)
        }
    }
}

@Composable
private fun MarkSymbol(mark: ChantScorer.Mark?, theme: AppTheme, modifier: Modifier) {
    val size = Modifier.size(18.dp)
    when (mark) {
        ChantScorer.Mark.RIGHT -> Icon(Icons.Default.CheckCircle, null, tint = theme.accentColor, modifier = modifier.then(size))
        ChantScorer.Mark.SLIPPED -> Icon(Icons.Default.Circle, null, tint = theme.slippedColor, modifier = modifier.then(size))
        ChantScorer.Mark.NOT_CHECKED -> Icon(Icons.Outlined.Circle, null, tint = theme.secondaryTextColor, modifier = modifier.then(size))
        null -> Spacer(modifier.then(size))
    }
}

@RequiresApi(33)
private fun tipText(lesson: LessonState): String? {
    val result = lesson.result ?: return null
    val word = result.tipPart?.let { lesson.stepParts.getOrNull(it)?.part?.roman }.orEmpty()
    val step = lesson.step
    return when (result.tipKind) {
        ChantScorer.TipKind.NOTHING_HEARD -> "Nothing was heard. Tap the mic and chant."
        ChantScorer.TipKind.SKIPPED -> "You skipped $word."
        ChantScorer.TipKind.SLIPPED -> "Listen again to $word."
        ChantScorer.TipKind.UNFINISHED ->
            if (step is Step.Line) "That take didn't finish. Try the whole line." else "That take didn't finish. Try the whole verse."
        ChantScorer.TipKind.ALL_RIGHT -> when {
            // Every part was Not checked (calibration): nothing was actually judged.
            ChantScorer.Mark.RIGHT !in result.marks -> "None of these words can be checked on this phone yet."
            step is Step.Line && step.n + 1 < lesson.practiceLines.size -> "All words right. Next line when you're ready."
            step == Step.Memory(hints = true) -> "All words right. Next, without the letters."
            else -> "All words right. Next step when you're ready."
        }
    }
}

@RequiresApi(33)
@Composable
private fun Tip(lesson: LessonState, text: String, theme: AppTheme, fontSize: Float) {
    val part = lesson.result?.tipPart
    if (lesson.result?.tipKind == ChantScorer.TipKind.SLIPPED && part != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { lesson.playLine(part) }
                .semantics(mergeDescendants = true) { contentDescription = "$text Plays the reciter's line." },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text, fontSize = fontSize.sp, color = theme.primaryTextColor, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = theme.accentColor)
        }
    } else {
        Text(text, fontSize = fontSize.sp, color = theme.primaryTextColor)
    }
}
