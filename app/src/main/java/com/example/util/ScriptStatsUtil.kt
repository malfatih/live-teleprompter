package com.example.util

object ScriptStatsUtil {

    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
    }

    fun estimateDuration(wordCount: Int, wpm: Int = 130): String {
        if (wordCount <= 0) return "0 detik"
        val totalSeconds = (wordCount.toFloat() / wpm.toFloat() * 60).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            if (seconds > 0) "$minutes mnt $seconds dtk" else "$minutes mnt"
        } else {
            "$seconds dtk"
        }
    }

    fun countLines(text: String): Int {
        if (text.isEmpty()) return 0
        return text.lines().size
    }
}
