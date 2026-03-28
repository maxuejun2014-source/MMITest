package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.content.Context
import android.os.BatteryManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import com.mxj.mmitest.ui.components.TimeoutDialog
import kotlinx.coroutines.delay

class ChargingTestActivity : BaseActivity() {
    private val testName = "充电测试"
    private val timeoutSeconds = 20
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            var batteryStatus by remember { mutableStateOf("检测中...") }
            
            TestItemScreen(
                testName = testName,
                testDescription = "充电接口和充电状态测试\n\n$batteryStatus\n\n请连接充电器后点击PASS或FAIL",
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
                val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                while (remainingSeconds > 0) {
                    val isCharging = batteryManager.isCharging
                    batteryStatus = if (isCharging) "正在充电" else "未充电"
                    
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
