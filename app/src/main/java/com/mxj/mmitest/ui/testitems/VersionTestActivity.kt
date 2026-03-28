package com.mxj.mmitest.ui.testitems

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class VersionTestActivity : BaseActivity() {
    private val testName = "版本测试"
    private val timeoutSeconds = 15
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            
            val versionInfo = "Model: ${Build.MODEL}\n" +
                    "Brand: ${Build.BRAND}\n" +
                    "Android: ${Build.VERSION.RELEASE}\n" +
                    "SDK: ${Build.VERSION.SDK_INT}"
            
            TestItemScreen(
                testName = testName,
                testDescription = "系统版本信息核对\n\n$versionInfo\n\n核对无误后请点击PASS",
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
