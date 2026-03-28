package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class FrontCameraTestActivity : BaseActivity() {
    private val testName = "前摄测试"
    private val timeoutSeconds = 45
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            TestItemScreen(
                testName = testName,
                testDescription = "前置摄像头测试\n\n请对着前置摄像头观察画面\n点击PASS表示画面正常，FAIL表示异常",
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
