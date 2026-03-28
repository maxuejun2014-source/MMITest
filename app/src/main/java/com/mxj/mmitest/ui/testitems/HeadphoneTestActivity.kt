package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class HeadphoneTestActivity : BaseActivity() {
    private val testName = "耳机回环测试"
    private val timeoutSeconds = 20
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            TestItemScreen(
                testName = testName,
                testDescription = "耳机接口测试\n\n插入耳机后测试\n点击PASS表示耳机正常工作，FAIL表示异常",
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
