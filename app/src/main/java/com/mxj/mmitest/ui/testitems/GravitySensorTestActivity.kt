package com.mxj.mmitest.ui.testitems

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class GravitySensorTestActivity : BaseActivity(), SensorEventListener {
    private val testName = "重力传感器测试"
    private val timeoutSeconds = 30
    private val testItemId = 10
    private lateinit var repository: TestRepository
    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null

    // 传感器数据
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f

    // 用于检测传感器是否有变化
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var sensorStable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(false) }
            var hasDetectedMotion by remember { mutableStateOf(false) }

            // 实时更新PASS按钮状态
            LaunchedEffect(hasDetectedMotion) {
                passEnabled = hasDetectedMotion
            }

            TestItemScreen(
                testName = testName,
                testDescription = "请晃动设备观察数值变化\n\n当检测到传感器有变化时，PASS按钮可点击",
                remainingSeconds = remainingSeconds,
                passEnabled = passEnabled,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 3D球体显示
                        GravityBallDisplay(
                            x = gravityX,
                            y = gravityY,
                            z = gravityZ,
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 传感器数值卡片
                        SensorValuesCard(
                            x = gravityX,
                            y = gravityY,
                            z = gravityZ
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 设备姿态指示
                        DeviceOrientationCard(
                            x = gravityX,
                            y = gravityY,
                            z = gravityZ
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 传感器状态
                        SensorStatusCard(
                            hasDetectedMotion = hasDetectedMotion
                        )
                    }
                }
            )

            // 倒计时
            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0) delay(1000)
                }
                saveAndFinish(false)
            }

            // 检测是否有明显运动
            LaunchedEffect(gravityX, gravityY, gravityZ) {
                val deltaX = abs(gravityX - lastX)
                val deltaY = abs(gravityY - lastY)
                val deltaZ = abs(gravityZ - lastZ)

                if (deltaX > 0.5f || deltaY > 0.5f || deltaZ > 0.5f) {
                    hasDetectedMotion = true
                }

                lastX = gravityX
                lastY = gravityY
                lastZ = gravityZ
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gravitySensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // 低通滤波使数值更平滑
            val alpha = 0.15f
            gravityX = gravityX + alpha * (event.values[0] - gravityX)
            gravityY = gravityY + alpha * (event.values[1] - gravityY)
            gravityZ = gravityZ + alpha * (event.values[2] - gravityZ)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Composable
    private fun GravityBallDisplay(
        x: Float,
        y: Float,
        z: Float,
        modifier: Modifier = Modifier
    ) {
        val ballX by animateFloatAsState(
            targetValue = (x / 10f).coerceIn(-0.8f, 0.8f),
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
            label = "ballX"
        )
        val ballY by animateFloatAsState(
            targetValue = (y / 10f).coerceIn(-0.8f, 0.8f),
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
            label = "ballY"
        )

        Box(
            modifier = modifier
                .background(Color(0xFF1A1A2E), shape = MaterialTheme.shapes.medium)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2
                val centerY = canvasHeight / 2
                val ballRadius = minOf(canvasWidth, canvasHeight) * 0.25f

                // 绘制边框圆
                drawCircle(
                    color = Color(0xFF4FC3F7),
                    radius = minOf(canvasWidth, canvasHeight) * 0.4f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 绘制中心十字线
                drawLine(
                    color = Color(0xFF4FC3F7).copy(alpha = 0.5f),
                    start = Offset(centerX, centerY - minOf(canvasWidth, canvasHeight) * 0.4f),
                    end = Offset(centerX, centerY + minOf(canvasWidth, canvasHeight) * 0.4f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF4FC3F7).copy(alpha = 0.5f),
                    start = Offset(centerX - minOf(canvasWidth, canvasHeight) * 0.4f, centerY),
                    end = Offset(centerX + minOf(canvasWidth, canvasHeight) * 0.4f, centerY),
                    strokeWidth = 1.dp.toPx()
                )

                // 绘制球体（跟随重力方向移动）
                val ballOffsetX = centerX + ballX * minOf(canvasWidth, canvasHeight) * 0.4f
                val ballOffsetY = centerY + ballY * minOf(canvasWidth, canvasHeight) * 0.4f

                // 球体阴影
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = ballRadius,
                    center = Offset(ballOffsetX + 4.dp.toPx(), ballOffsetY + 4.dp.toPx())
                )

                // 球体
                drawCircle(
                    color = Color(0xFFE91E63),
                    radius = ballRadius,
                    center = Offset(ballOffsetX, ballOffsetY)
                )

                // 球体高光
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = ballRadius * 0.3f,
                    center = Offset(ballOffsetX - ballRadius * 0.3f, ballOffsetY - ballRadius * 0.3f)
                )
            }

            // 球体位置指示文字
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "X: ${String.format("%.1f", x)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }

    @Composable
    private fun SensorValuesCard(x: Float, y: Float, z: Float) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "加速度传感器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AxisValue(label = "X", value = x, color = Color(0xFFFF5252))
                    AxisValue(label = "Y", value = y, color = Color(0xFF4CAF50))
                    AxisValue(label = "Z", value = z, color = Color(0xFF2196F3))
                }
            }
        }
    }

    @Composable
    private fun AxisValue(label: String, value: Float, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    @Composable
    private fun DeviceOrientationCard(x: Float, y: Float, z: Float) {
        val orientation = remember(x, y, z) {
            when {
                abs(z) > abs(x) && abs(z) > abs(y) -> {
                    if (z > 0) "屏幕朝上" else "屏幕朝下"
                }
                abs(y) > abs(x) && abs(y) > abs(z) -> {
                    if (y > 0) "设备竖直" else "设备倒置"
                }
                else -> "设备倾斜"
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "设备姿态: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = orientation,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }

    @Composable
    private fun SensorStatusCard(hasDetectedMotion: Boolean) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasDetectedMotion)
                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                else
                    Color(0xFFFF9800).copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "传感器状态: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = if (hasDetectedMotion) "已检测到变化" else "等待晃动...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasDetectedMotion) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
    }

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
