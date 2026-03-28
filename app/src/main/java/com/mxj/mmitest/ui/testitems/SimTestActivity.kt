package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

/**
 * SIM卡测试Activity
 */
class SimTestActivity : BaseActivity() {

    private val testName = "SIM卡测试"
    private val timeoutSeconds = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }

            TestItemScreen(
                testName = testName,
                testDescription = "请检查SIM卡是否正常识别\n\n" +
                        "操作步骤：\n" +
                        "1. 确认SIM卡已插入\n" +
                        "2. 检查信号强度\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )

            if (showTimeoutDialog) {
                TimeoutDialog(
                    remainingSeconds = remainingSeconds,
                    onContinueWait = {
                        remainingSeconds = timeoutSeconds
                        showTimeoutDialog = false
                    },
                    onMarkFailed = { finish() },
                    onSkip = { finish() }
                )
            }

            // 倒计时逻辑
            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                    if (remainingSeconds == 0) {
                        showTimeoutDialog = true
                    }
                }
            }
        }
    }
}
