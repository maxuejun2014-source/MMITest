package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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

class ProximitySensorTestActivity : BaseActivity(), SensorEventListener {
    private val testName = "距离传感器测试"
    private val timeoutSeconds = 20
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var maxRange: Float = 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        maxRange = proximitySensor?.maximumRange ?: 5f

        setContent {
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }
            var showTimeoutDialog by remember { mutableStateOf(false) }
            var sensorValue by remember { mutableStateOf("检测中...") }

            TestItemScreen(
                testName = testName,
                testDescription = "距离传感器功能测试\n\n当前值: $sensorValue\n\n用手遮挡屏幕上方的传感器区域，观察数值变化",
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
                sensorManager?.registerListener(
                    this@ProximitySensorTestActivity,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i == 0) { showTimeoutDialog = true; break }
                    delay(1000)
                }
                sensorManager?.unregisterListener(this@ProximitySensorTestActivity)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PROXIMITY) {
                val value = it.values[0]
                val status = if (value >= maxRange) "远离" else "接近"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
