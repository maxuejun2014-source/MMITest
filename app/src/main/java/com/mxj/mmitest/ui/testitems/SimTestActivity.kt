package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SIM卡测试Activity
 */
class SimTestActivity : BaseActivity() {

    private val testName = "SIM卡测试"
    private val testItemId = 1
    private val timeoutSeconds = 15

    private lateinit var repository: TestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(false) }

            TestItemScreen(
                testName = testName,
                testDescription = "请检查SIM卡是否正常识别\n\n" +
                        "操作步骤：\n" +
                        "1. 确认SIM卡已插入\n" +
                        "2. 检查信号强度\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                passEnabled = passEnabled
            )

            // 倒计时逻辑 - 超时直接finish，不弹框
            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束，标记为FAIL
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
