package com.mxj.mmitest.ui.testitems

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VersionTestActivity : BaseActivity() {
    private val testName = "版本测试"
    private val timeoutSeconds = 15
    private val testItemId = 4
    private lateinit var repository: TestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(true) }

            val versionInfo = "Model: ${Build.MODEL}\n" +
                    "Brand: ${Build.BRAND}\n" +
                    "Android: ${Build.VERSION.RELEASE}\n" +
                    "SDK: ${Build.VERSION.SDK_INT}"

            TestItemScreen(
                testName = testName,
                testDescription = "系统版本信息核对\n\n$versionInfo\n\n核对无误后请点击PASS",
                remainingSeconds = remainingSeconds,
                passEnabled = passEnabled,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) }
            )

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
