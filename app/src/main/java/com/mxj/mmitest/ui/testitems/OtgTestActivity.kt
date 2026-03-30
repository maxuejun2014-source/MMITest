package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

class OtgTestActivity : BaseActivity() {
    private val testName = "OTG测试"
    private val timeoutSeconds = 20
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            
            TestItemScreen(
                testName = testName,
                testDescription = "OTG功能测试\n\n请连接OTG设备（如U盘）\n点击PASS表示能识别设备，FAIL表示异常",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )
            
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
