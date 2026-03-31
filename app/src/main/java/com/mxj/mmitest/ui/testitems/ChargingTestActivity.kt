package com.mxj.mmitest.ui.testitems

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChargingTestActivity : BaseActivity() {
    private val testName = "充电测试"
    private val timeoutSeconds = 30
    private val testItemId = 9
    private lateinit var repository: TestRepository

    // 电池信息数据类
    data class BatteryInfo(
        val level: Int = 0,
        val isCharging: Boolean = false,
        val chargeType: String = "未知",
        val health: String = "未知",
        val temperature: Float = 0f,
        val voltage: Int = 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var batteryInfo by remember { mutableStateOf(BatteryInfo()) }
            var passEnabled by remember { mutableStateOf(false) }

            // 监听电池变化
            val batteryReceiver = remember {
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        intent?.let {
                            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

                            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                    status == BatteryManager.BATTERY_STATUS_FULL

                            val plugType = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            val chargeType = when (plugType) {
                                BatteryManager.BATTERY_PLUGGED_AC -> "交流电 (AC)"
                                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
                                else -> "未连接"
                            }

                            val healthInt = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                            val health = when (healthInt) {
                                BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
                                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
                                BatteryManager.BATTERY_HEALTH_DEAD -> "损坏"
                                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "电压过高"
                                BatteryManager.BATTERY_HEALTH_COLD -> "温度过低"
                                else -> "未知"
                            }

                            val temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                            val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

                            batteryInfo = BatteryInfo(
                                level = batteryPct,
                                isCharging = isCharging,
                                chargeType = chargeType,
                                health = health,
                                temperature = temperature,
                                voltage = voltage
                            )

                            // 如果正在充电，允许点击PASS
                            passEnabled = isCharging
                        }
                    }
                }
            }

            // 注册电池广播
            remember {
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                registerReceiver(batteryReceiver, filter)
            }

            DisposableEffect(Unit) {
                onDispose {
                    try {
                        unregisterReceiver(batteryReceiver)
                    } catch (e: Exception) {}
                }
            }

            TestItemScreen(
                testName = testName,
                testDescription = "请连接充电器进行测试",
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
                        // 电池图标和电量
                        BatteryDisplay(
                            level = batteryInfo.level,
                            isCharging = batteryInfo.isCharging,
                            modifier = Modifier.size(120.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 充电状态
                        ChargingStatusCard(
                            isCharging = batteryInfo.isCharging,
                            chargeType = batteryInfo.chargeType
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 电池详情
                        BatteryDetailsCard(
                            health = batteryInfo.health,
                            temperature = batteryInfo.temperature,
                            voltage = batteryInfo.voltage
                        )
                    }
                }
            )

            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0) delay(1000)
                }
                saveAndFinish(false)
            }
        }
    }

    @Composable
    private fun BatteryDisplay(
        level: Int,
        isCharging: Boolean,
        modifier: Modifier = Modifier
    ) {
        val batteryColor = when {
            level < 20 -> Color.Red
            level < 50 -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }

        Canvas(modifier = modifier) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 电池体外框
            val batteryWidth = canvasWidth * 0.7f
            val batteryHeight = canvasHeight * 0.8f
            val left = (canvasWidth - batteryWidth) / 2
            val top = (canvasHeight - batteryHeight) / 2

            // 电池帽
            val capWidth = batteryWidth * 0.2f
            val capHeight = batteryHeight * 0.1f
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(left + (batteryWidth - capWidth) / 2, top - capHeight),
                size = Size(capWidth, capHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // 电池体
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(left, top),
                size = Size(batteryWidth, batteryHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // 电量填充
            val fillWidth = batteryWidth * 0.9f
            val fillHeight = (batteryHeight * 0.9f) * (level / 100f)
            val fillLeft = left + (batteryWidth - fillWidth) / 2
            val fillTop = top + (batteryHeight * 0.95f) - fillHeight

            if (level > 0) {
                drawRoundRect(
                    color = batteryColor,
                    topLeft = Offset(fillLeft, fillTop),
                    size = Size(fillWidth, fillHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // 充电图标
            if (isCharging) {
                val boltCenterX = canvasWidth / 2
                val boltCenterY = canvasHeight / 2
                val boltSize = minOf(canvasWidth, canvasHeight) * 0.25f

                // 绘制闪电符号
                drawLine(
                    color = Color.Yellow,
                    start = Offset(boltCenterX + boltSize * 0.3f, boltCenterY - boltSize),
                    end = Offset(boltCenterX - boltSize * 0.2f, boltCenterY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.Yellow,
                    start = Offset(boltCenterX - boltSize * 0.2f, boltCenterY),
                    end = Offset(boltCenterX + boltSize * 0.3f, boltCenterY + boltSize),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // 电量百分比文字
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${level}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (level > 50) Color.White else Color.Black
            )
        }
    }

    @Composable
    private fun ChargingStatusCard(isCharging: Boolean, chargeType: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCharging) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFFB71C1C).copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "充电状态",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isCharging) "正在充电" else "未充电",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCharging) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "电源类型",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = chargeType,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    private fun BatteryDetailsCard(health: String, temperature: Float, voltage: Int) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Gray.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "电池详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem(label = "健康状态", value = health)
                    DetailItem(label = "温度", value = "${temperature}°C")
                    DetailItem(label = "电压", value = "${voltage}mV")
                }
            }
        }
    }

    @Composable
    private fun DetailItem(label: String, value: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
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
