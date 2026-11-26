package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.algorithm.StatisticalAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PickSlip", appName)
  }

  @Test
  fun `test statistical algorithm determinism`() {
    val output = StatisticalAlgorithm.evaluate(
      homeTeam = "Arsenal",
      awayTeam = "Chelsea",
      homeForm = "WWWWW",
      awayForm = "LLDLD",
      homeGoalsForAvg = 2.6,
      homeGoalsAgainstAvg = 0.6,
      awayGoalsForAvg = 1.0,
      awayGoalsAgainstAvg = 1.9,
      h2hBttsRate = 0.50,
      h2hOver25Rate = 0.70
    )

    assertNotNull(output.pick)
    assertTrue(output.confidence in 50.0..95.0)
    assertTrue(output.reasoning.isNotEmpty())
    assertEquals("win", StatisticalAlgorithm.evaluateResult("HOME_WIN", 3, 1))
    assertEquals("loss", StatisticalAlgorithm.evaluateResult("HOME_WIN", 1, 2))
    assertEquals("win", StatisticalAlgorithm.evaluateResult("OVER_25", 2, 1))
    assertEquals("loss", StatisticalAlgorithm.evaluateResult("UNDER_25", 2, 1))
    assertEquals("win", StatisticalAlgorithm.evaluateResult("BTTS_YES", 1, 1))
  }
}
