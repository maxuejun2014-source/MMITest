package com.mxj.mmitest.ui.testitems

import android.os.Bundle
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
 * 版本号测试Activity
 */
class VersionTestActivity : BaseActivity() {

    private val testName = "版本号测试"
    private val timeoutSeconds = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val build = android.os.Build.VERSION

            val versionInfo = """
                Android版本: ${build.RELEASE}
                SDK版本: ${build.SDK_INT}
                内核版本: ${System.getProperty("os.version")}
                设备型号: ${android.os.Build.MODEL}
                制造商: ${android.os.Build.MANUFACTURER}
                设备: ${android.os.Build.DEVICE}
                产品: ${android.os.Build.PRODUCT}
            """.trimIndent()

            TestItemScreen(
                testName = testName,
                testDescription = "显示设备版本信息\n\n$versionInfo\n\n点击PASS确认信息正确，FAIL表示信息有误",
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
