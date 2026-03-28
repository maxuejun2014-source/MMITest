package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
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
 * 震动测试Activity
 */
class VibrationTestActivity : BaseActivity() {

    private val testName = "震动测试"
    private val timeoutSeconds = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            // 测试震动
            LaunchedEffect(Unit) {
                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            TestItemScreen(
                testName = testName,
                testDescription = "测试振动马达功能\n\n" +
                        "操作步骤：\n" +
                        "1. 感受震动是否正常\n" +
                        "2. 检查震动强度\n" +
                        "3. 点击PASS或FAIL按钮",
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
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i == 0) { showTimeoutDialog = true; break }
                    delay(1000)
                }
            }
        }
    }
}
