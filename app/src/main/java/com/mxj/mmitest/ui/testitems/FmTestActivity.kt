package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class FmTestActivity : BaseActivity() {
    private val testName = "FM测试"
    private val timeoutSeconds = 45
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            
            TestItemScreen(
                testName = testName,
                testDescription = "FM收音机功能测试\n\n注意：部分设备不支持FM\n插入耳机作为天线后测试\n点击PASS表示FM正常工作，FAIL表示异常",
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
