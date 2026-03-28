package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class WifiTestActivity : BaseActivity() {
    private val testName = "WIFI测试"
    private val timeoutSeconds = 30
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            
            TestItemScreen(
                testName = testName,
                testDescription = "检测附近可用WIFI网络\n\n请确认WIFI已开启\n点击PASS表示WIFI功能正常，FAIL表示异常",
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
