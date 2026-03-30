package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

class TpTestActivity : BaseActivity() {
    private val testName = "TP测试"
    private val timeoutSeconds = 45
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            TestItemScreen(
                testName = testName,
                testDescription = "触摸屏测试\n\n操作步骤：\n1. 在屏幕上滑动\n2. 点击不同区域\n3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )
            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0) delay(1000)
                }
                // 超时自动结束
                finish()
            }
        }
    }
}
