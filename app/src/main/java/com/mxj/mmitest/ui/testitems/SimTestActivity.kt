package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mxj.mmitest.config.TestConfig
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestResultButtons
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

/**
 * SIM卡测试Activity
 */
class SimTestActivity : BaseActivity() {

    private val testItemId = 1
    private val testName = "SIM卡测试"
    private val timeoutSeconds = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }

            TestItemScreen(
                testName = testName,
                testDescription = "请检查SIM卡是否正常识别\n\n" +
                        "操作步骤：\n" +
                        "1. 确认SIM卡已插入\n" +
                        "2. 检查信号强度\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = {
                    // TODO: 保存测试结果
                    finish()
                },
                onFail = {
                    // TODO: 保存测试结果
                    finish()
                }
            )

            if (showTimeoutDialog) {
                TimeoutDialog(
                    remainingSeconds = remainingSeconds,
                    onContinueWait = {
                        remainingSeconds = timeoutSeconds
                        showTimeoutDialog = false
                    },
                    onMarkFailed = {
                        // TODO: 保存测试结果（超时失败）
                        finish()
                    },
                    onSkip = {
                        // TODO: 保存测试结果（跳过）
                        finish()
                    }
                )
            }

            // 倒计时逻辑
            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i == 0) {
                        showTimeoutDialog = true
                        break
                    }
                    delay(1000)
                }
            }
        }
    }
}
