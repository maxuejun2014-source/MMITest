package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.content.Context
import android.os.BatteryManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChargingTestActivity : BaseActivity() {
    private val testName = "充电测试"
    private val timeoutSeconds = 20
    private val testItemId = 9
    private lateinit var repository: TestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var batteryStatus by remember { mutableStateOf("检测中...") }

            TestItemScreen(
                testName = testName,
                testDescription = "充电接口和充电状态测试\n\n$batteryStatus\n\n请连接充电器后点击PASS或FAIL",
                remainingSeconds = remainingSeconds,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) }
            )

            LaunchedEffect(Unit) {
                val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                while (remainingSeconds > 0) {
                    val isCharging = batteryManager.isCharging
                    batteryStatus = if (isCharging) "正在充电" else "未充电"

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
