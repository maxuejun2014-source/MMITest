package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

/**
 * SIM卡测试Activity
 */
class SimTestActivity : BaseActivity() {

    private val testName = "SIM卡测试"
    private val timeoutSeconds = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }

            TestItemScreen(
                testName = testName,
                testDescription = "请检查SIM卡是否正常识别\n\n" +
                        "操作步骤：\n" +
                        "1. 确认SIM卡已插入\n" +
                        "2. 检查信号强度\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )

            // 倒计时逻辑 - 超时直接finish，不弹框
            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束
                finish()
            }
        }
    }
}
