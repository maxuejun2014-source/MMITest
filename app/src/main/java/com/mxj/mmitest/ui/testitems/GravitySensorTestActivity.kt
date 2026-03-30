package com.mxj.mmitest.ui.testitems

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GravitySensorTestActivity : BaseActivity(), SensorEventListener {
    private val testName = "重力传感器测试"
    private val timeoutSeconds = 20
    private val testItemId = 10
    private lateinit var repository: TestRepository
    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null

    // 使用 Compose State 来同步传感器数据
    private var sensorDataState = mutableStateOf("检测中...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            val sensorData by remember { sensorDataState }
            var passEnabled by remember { mutableStateOf(true) }

            TestItemScreen(
                testName = testName,
                testDescription = "加速度计功能测试\n\n数据: $sensorData\n\n请晃动设备观察数值变化",
                remainingSeconds = remainingSeconds,
                passEnabled = passEnabled,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) }
            )

            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                // 超时自动结束
                saveAndFinish(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gravitySensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            sensorDataState.value = String.format("X: %.2f, Y: %.2f, Z: %.2f", x, y, z)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveAndFinish(passed: Boolean) {
        lifecycleScope.launch {
            repository.saveSingleTestResult(
                testItemId = testItemId,
                testItemName = testName,
                passed = passed,
                deviceId = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: android.os.Build.MODEL
            )
            finish()
        }
    }
}
