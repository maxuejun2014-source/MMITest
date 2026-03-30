package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BacklightTestActivity : BaseActivity() {
    private val testName = "背光测试"
    private val timeoutSeconds = 30
    private val testItemId = 6
    private lateinit var repository: TestRepository
    private var originalBrightness: Float = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        // 保存原始亮度
        originalBrightness = window.attributes.screenBrightness

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var currentBrightness by remember { mutableFloatStateOf(0.1f) }
            var passEnabled by remember { mutableStateOf(true) }

            TestItemScreen(
                testName = testName,
                testDescription = "屏幕亮度自动调节测试\n\n屏幕亮度将在 10% 和 100% 之间循环切换\n点击PASS表示正常，FAIL表示异常",
                remainingSeconds = remainingSeconds,
                passEnabled = passEnabled,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) }
            )

            LaunchedEffect(currentBrightness) {
                val params = window.attributes
                params.screenBrightness = currentBrightness
                window.attributes = params

                delay(2000)
                currentBrightness = if (currentBrightness < 0.5f) 1.0f else 0.1f
            }

            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束
                saveAndFinish(false)
            }
        }
    }

    override fun onDestroy() {
        // 恢复原始亮度
        val params = window.attributes
        params.screenBrightness = originalBrightness
        window.attributes = params
        super.onDestroy()
    }

    private fun saveAndFinish(passed: Boolean) {
        lifecycleScope.launch {
            repository.saveSingleTestResult(
                testItemId = testItemId,
                testItemName = testName,
                passed = passed,
                deviceId = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: android.os.Build.MODEL
            )
            finish()
        }
    }
}
