package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class HeadphoneTestActivity : BaseActivity() {
    private val testName = "耳机测试"
    private val timeoutSeconds = 30
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            
            TestItemScreen(
                testName = testName,
                testDescription = "耳机插孔功能测试\n\n请插入耳机并确认左右声道正常\n点击PASS表示正常，FAIL表示异常",
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
