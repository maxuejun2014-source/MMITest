package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

/**
 * 存储测试Activity
 */
class StorageTestActivity : BaseActivity() {

    private val testName = "存储测试"
    private val timeoutSeconds = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }

            TestItemScreen(
                testName = testName,
                testDescription = "检测内部存储和SD卡\n\n" +
                        "操作步骤：\n" +
                        "1. 检查内部存储容量\n" +
                        "2. 检查SD卡是否存在（如有）\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )

            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i == 0) { break }
                    delay(1000)
                }
                // 超时自动结束
                if (remainingSeconds == 0) finish()
            }
        }
    }
}
