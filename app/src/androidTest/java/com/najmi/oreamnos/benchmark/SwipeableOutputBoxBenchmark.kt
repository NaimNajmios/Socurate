package com.najmi.oreamnos.benchmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.testutils.ComposeTestCase
import androidx.compose.testutils.benchmark.ComposeBenchmarkRule
import androidx.compose.testutils.benchmark.benchmarkFirstCompose
import androidx.compose.testutils.benchmark.benchmarkLayoutPerf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.najmi.oreamnos.ui.components.SwipeableOutputBox
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeableOutputBoxBenchmark {

    @get:Rule
    val benchmarkRule = ComposeBenchmarkRule()

    @Test
    fun swipeableOutputBox_recomposition() {
        benchmarkRule.benchmarkFirstCompose {
            SwipeableOutputBox(
                outputText = "Benchmark Text",
                textSize = 14,
                onCopy = {},
                onShare = {}
            )
        }
    }
}
