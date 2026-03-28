package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

/**
 * 背光测试Activity
 */
class BacklightTestActivity : BaseActivity() {

    private val testName = "背光测试"
    private val timeoutSeconds = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var brightness by remember { mutableStateOf(50) }
            var showTimeoutDialog by remember { mutableStateOf(false) }

            TestItemScreen(
                testName = testName,
                testDescription = "屏幕背光调节测试\n\n" +
                        "当前亮度: $brightness%\n\n" +
                        "操作步骤：\n" +
                        "1. 观察屏幕亮度变化\n" +
                        "2. 点击 +/- 调整亮度\n" +
                        "3. 点击PASS或FAIL按钮",
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
