package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Live Teleprompter", appName)
  }

  @Test
  fun testNotificationChannelStrings() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val channelName = context.getString(R.string.channel_name_teleprompter)
    assertNotNull(channelName)
  }
}
