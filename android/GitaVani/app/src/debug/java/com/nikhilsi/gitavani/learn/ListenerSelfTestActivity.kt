package com.nikhilsi.gitavani.learn

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import java.io.File
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Debug-only check of ChantListener on a real phone (spec step 10).
 *
 * Push a plan and recordings, then start it:
 *   adb push plan.json *.pcm /sdcard/Android/data/com.nikhilsi.gitavani.debug/files/selftest/
 *   adb shell am start -n com.nikhilsi.gitavani.debug/com.nikhilsi.gitavani.learn.ListenerSelfTestActivity
 * Each item is a raw 16 kHz mono PCM file ("file") or a stretch of a reciter recording ("verse", "from",
 * "to"). Results go to selftest/results.json next to the plan and to logcat (tag SelfTest).
 */
class ListenerSelfTestActivity : ComponentActivity() {
    private lateinit var view: TextView

    private fun log(line: String) {
        Log.i("SelfTest", line)
        view.append(line + "\n")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = TextView(this).apply { textSize = 11f; setPadding(24, 96, 24, 24) }
        setContentView(ScrollView(this).apply { addView(view) })
        lifecycleScope.launch { run() }
    }

    private suspend fun run() {
        log("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) ${Build.MODEL}")
        if (Build.VERSION.SDK_INT < 33 || !deviceCanLearn(this)) {
            log("Learn not available on this device")
            return
        }
        val dir = File(getExternalFilesDir(null), "selftest")
        val listener = ChantListener(this)
        try {
            var state = listener.modelState()
            log("model: $state")
            if (state == ChantListener.ModelState.NEEDS_DOWNLOAD) {
                state = listener.downloadModel { log("download $it%") }
                log("model after download: $state")
            }
            val plan = JSONObject(File(dir, "plan.json").readText()).getJSONArray("items")
            val results = JSONArray()
            for (i in 0 until plan.length()) {
                val item = plan.getJSONObject(i)
                val name = item.getString("name")
                val realTime = !item.optBoolean("fast", false)
                val start = System.currentTimeMillis()
                val out = JSONObject().put("name", name)
                try {
                    val heard = if (item.has("file")) {
                        val bytes = File(dir, item.getString("file")).readBytes()
                        val pcm = ShortArray(bytes.size / 2) { k ->
                            ((bytes[2 * k].toInt() and 0xff) or (bytes[2 * k + 1].toInt() shl 8)).toShort()
                        }
                        listener.transcribe(pcm, realTime)
                    } else {
                        listener.transcribeAsset("audio/${item.getString("verse")}.mp3",
                            item.getDouble("from"), item.getDouble("to"), realTime)
                    }
                    val seconds = (System.currentTimeMillis() - start) / 1000.0
                    out.put("heard", heard).put("seconds", seconds)
                    log("$name (${"%.1f".format(seconds)} s): $heard")
                } catch (e: Exception) {
                    out.put("error", e.toString())
                    log("$name: ERROR $e")
                }
                results.put(out)
                File(dir, "results.json").writeText(JSONObject().put("done", false).put("results", results).toString(1))
            }
            File(dir, "results.json").writeText(JSONObject().put("done", true).put("results", results).toString(1))
            log("DONE")
        } finally {
            listener.close()
        }
    }
}
