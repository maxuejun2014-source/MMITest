package com.mxj.mmitest.ui.testitems

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay

class ProximitySensorTestActivity : BaseActivity(), SensorEventListener {
    private val testName = "距离传感器测试"
    private val timeoutSeconds = 20
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    
    private var distanceState = mutableStateOf("检测中...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            val distance by remember { distanceState }
            
            TestItemScreen(
                testName = testName,
                testDescription = "距离传感器测试\n\n状态: $distance\n\n请用手遮挡屏幕上方传感器查看状态变化",
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

    override fun onResume() {
        super.onResume()
        proximitySensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val dist = event.values[0]
            distanceState.value = if (dist < (proximitySensor?.maximumRange ?: 5f)) "已遮挡 (近)" else "未遮挡 (远)"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
