package com.mxj.mmitest.ui.testitems

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChargingTestActivity : BaseActivity() {
    private val testName = "充电测试"
    private val timeoutSeconds = 30
    private val testItemId = 9
    private lateinit var repository: TestRepository

    // 电池状态常量
    private val batteryStatusMap = mapOf(
        BatteryManager.BATTERY_STATUS_UNKNOWN to "未知",
        BatteryManager.BATTERY_STATUS_CHARGING to "充电中",
        BatteryManager.BATTERY_STATUS_DISCHARGING to "放电中",
        BatteryManager.BATTERY_STATUS_NOT_CHARGING to "未充电",
        BatteryManager.BATTERY_STATUS_FULL to "已充满"
    )

    // 电池健康状态常量
    private val batteryHealthMap = mapOf(
        BatteryManager.BATTERY_HEALTH_UNKNOWN to "未知",
        BatteryManager.BATTERY_HEALTH_GOOD to "良好",
        BatteryManager.BATTERY_HEALTH_OVERHEAT to "过热",
        BatteryManager.BATTERY_HEALTH_DEAD to "电池损坏",
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE to "电压过高",
        BatteryManager.BATTERY_HEALTH_COLD to "温度过低"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var batteryLevel by remember { mutableIntStateOf(0) }
            var batteryVoltage by remember { mutableIntStateOf(0) }
            var batteryTemperature by remember { mutableFloatStateOf(0f) }
            var batteryStatus by remember { mutableIntStateOf(BatteryManager.BATTERY_STATUS_UNKNOWN) }
            var batteryHealth by remember { mutableIntStateOf(BatteryManager.BATTERY_HEALTH_UNKNOWN) }
            var pluggedType by remember { mutableIntStateOf(0) }
            var batteryTechnology by remember { mutableStateOf("未知") }
            var isCharging by remember { mutableStateOf(false) }
            var passEnabled by remember { mutableStateOf(false) }

            // 充电来源
            val chargingSource = when (pluggedType) {
                BatteryManager.BATTERY_PLUGGED_AC -> "交流电 (AC)"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
                else -> "未连接"
            }

            // 状态颜色
            val statusColor = when {
                isCharging -> Color(0xFF4CAF50) // 绿色
                batteryLevel < 20 -> Color(0xFFF44336) // 红色
                else -> Color(0xFFFF9800) // 橙色
            }

            // 背景颜色
            val backgroundColor = MaterialTheme.colorScheme.surface

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 顶部标题
                Text(
                    text = testName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 倒计时
                Text(
                    text = "剩余时间: ${remainingSeconds}秒",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (remainingSeconds <= 5)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 电池电量圆形指示器
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCharging) Color(0xFF1B5E20).copy(alpha = 0.1f)
                            else Color(0xFF1B5E20).copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 外圈
                    CircularProgressIndicator(
                        progress = { batteryLevel / 100f },
                        modifier = Modifier.size(140.dp),
                        color = statusColor,
                        strokeWidth = 12.dp,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                    // 电量文字
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$batteryLevel%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        if (isCharging) {
                            Text(
                                text = "⚡ 充电中",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 状态提示
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCharging) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isCharging) "✓ 已检测到充电 - 可以点击PASS" else "请连接充电器",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 详细信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "电池详细信息",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        InfoRow("充电状态", batteryStatusMap[batteryStatus] ?: "未知")
                        InfoRow("充电来源", chargingSource)
                        InfoRow("电池电压", "${batteryVoltage}mV")
                        InfoRow("电池温度", "${String.format("%.1f", batteryTemperature / 10.0)}°C")
                        InfoRow("电池健康", batteryHealthMap[batteryHealth] ?: "未知")
                        InfoRow("电池技术", batteryTechnology)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 底部按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // FAIL按钮
                    Button(
                        onClick = { saveAndFinish(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "FAIL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // PASS按钮
                    Button(
                        onClick = { saveAndFinish(true) },
                        enabled = passEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "PASS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (passEnabled) Color.White else Color.Gray
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    updateBatteryInfo(
                        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager,
                        onLevelChange = { level, _ -> batteryLevel = level },
                        onVoltageChange = { voltage -> batteryVoltage = voltage },
                        onTemperatureChange = { temp -> batteryTemperature = temp },
                        onStatusChange = { status -> batteryStatus = status },
                        onHealthChange = { health -> batteryHealth = health },
                        onPluggedChange = { plugged -> pluggedType = plugged },
                        onTechnologyChange = { tech -> batteryTechnology = tech },
                        onChargingChange = { charging -> isCharging = charging }
                    )
                    passEnabled = isCharging
                    delay(1000)
                    remainingSeconds--
                }
                saveAndFinish(false)
            }
        }
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    private fun updateBatteryInfo(
        batteryManager: BatteryManager,
        onLevelChange: (Int, Int) -> Unit,
        onVoltageChange: (Int) -> Unit,
        onTemperatureChange: (Float) -> Unit,
        onStatusChange: (Int) -> Unit,
        onHealthChange: (Int) -> Unit,
        onPluggedChange: (Int) -> Unit,
        onTechnologyChange: (String) -> Unit,
        onChargingChange: (Boolean) -> Unit
    ) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)

        batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "未知"

            onLevelChange(level, scale)
            onVoltageChange(voltage)
            onTemperatureChange(temperature.toFloat())
            onStatusChange(status)
            onHealthChange(health)
            onPluggedChange(plugged)
            onTechnologyChange(technology)
            onChargingChange(status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL)
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
