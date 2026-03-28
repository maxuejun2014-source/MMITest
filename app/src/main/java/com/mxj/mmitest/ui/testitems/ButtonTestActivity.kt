package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

/**
 * 按键测试Activity
 */
class ButtonTestActivity : BaseActivity() {
    private val testName = "按键测试"
    private val timeoutSeconds = 30
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            TestItemScreen(
                testName = testName,
                testDescription = "物理按键和虚拟按键测试\n\n" +
                        "操作步骤：\n" +
                        "1. 测试音量+键\n" +
                        "2. 测试音量-键\n" +
                        "3. 测试电源键\n" +
                        "4. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )
            if (showTimeoutDialog) {
                TimeoutDialog(
                    remainingSeconds = remainingSeconds,
                    onContinueWait = { remainingSeconds = timeoutSeconds; showTimeoutDialog = false },
                    onMarkFailed = { finish() },
                    onSkip = { finish() }
                )
            }
            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i == 0) { showTimeoutDialog = true; break }
                    delay(1000)
                }
            }
        }
    }
}
