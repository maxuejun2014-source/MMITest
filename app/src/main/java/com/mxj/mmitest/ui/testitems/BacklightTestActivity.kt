package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

class BacklightTestActivity : BaseActivity() {
    private val testName = "背光测试"
    private val timeoutSeconds = 30
    private var originalBrightness: Float = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 保存原始亮度
        originalBrightness = window.attributes.screenBrightness
        
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var currentBrightness by remember { mutableFloatStateOf(0.1f) }
            
            TestItemScreen(
                testName = testName,
                testDescription = "屏幕亮度自动调节测试\n\n屏幕亮度将在 10% 和 100% 之间循环切换\n点击PASS表示正常，FAIL表示异常",
                remainingSeconds = remainingSeconds,
                onPass = { finish() },
                onFail = { finish() }
            )
            
            LaunchedEffect(currentBrightness) {
                val params = window.attributes
                params.screenBrightness = currentBrightness
                window.attributes = params
                
                delay(2000)
                currentBrightness = if (currentBrightness < 0.5f) 1.0f else 0.1f
            }
            
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

    override fun onDestroy() {
        // 恢复原始亮度
        val params = window.attributes
        params.screenBrightness = originalBrightness
        window.attributes = params
        super.onDestroy()
    }
}
