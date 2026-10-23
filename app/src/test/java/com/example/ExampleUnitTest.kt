package com.example

import com.example.data.model.ScriptEntity
import com.example.util.ScriptStatsUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testWordCount() {
    val sampleText = "Halo semuanya selamat datang di live streaming kita hari ini"
    val wordCount = ScriptStatsUtil.countWords(sampleText)
    assertEquals(10, wordCount)
  }

  @Test
  fun testEmptyWordCount() {
    assertEquals(0, ScriptStatsUtil.countWords(""))
    assertEquals(0, ScriptStatsUtil.countWords("    "))
  }

  @Test
  fun testDurationEstimation() {
    // 130 words at 130 WPM should take approx 1 minute
    val duration = ScriptStatsUtil.estimateDuration(130, 130)
    assertEquals("1 mnt", duration)

    val durationSecondsOnly = ScriptStatsUtil.estimateDuration(30, 130)
    assertTrue(durationSecondsOnly.contains("dtk"))
  }

  @Test
  fun testScriptEntityCreation() {
    val script = ScriptEntity(
      title = "Test Script",
      content = "Test content for live",
      category = "Live Jualan"
    )
    assertEquals("Test Script", script.title)
    assertEquals("Live Jualan", script.category)
  }
}
