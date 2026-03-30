package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BacklightTestActivity : BaseActivity() {
    private val testName = "背光测试"
    private val timeoutSeconds = 30
    private val testItemId = 6
    private lateinit var repository: TestRepository
    private var originalBrightness: Float = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        // 保存原始亮度
        originalBrightness = window.attributes.screenBrightness

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var currentBrightness by remember { mutableFloatStateOf(0.1f) }
            var passEnabled by remember { mutableStateOf(true) }

            TestItemScreen(
                testName = testName,
                testDescription = "屏幕亮度自动调节测试\n\n屏幕亮度将在 10% 和 100% 之间循环切换",
                remainingSeconds = remainingSeconds,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                passEnabled = passEnabled,
                content = {
                    // 自定义内容：亮度显示
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "当前亮度",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 亮度百分比显示
                        Text(
                            text = "${(currentBrightness * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // SeekBar显示亮度（只读，用户不可点击）
                        Slider(
                            value = currentBrightness,
                            onValueChange = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                disabledThumbColor = MaterialTheme.colorScheme.primary,
                                disabledActiveTrackColor = MaterialTheme.colorScheme.primary,
                                disabledInactiveTrackColor = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "0%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(text = "50%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(text = "100%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "亮度自动切换中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            )

            // 亮度平滑过渡控制
            var brightnessDirection by remember { mutableIntStateOf(1) } // 1=增加，-1=减少

            LaunchedEffect(currentBrightness) {
                val params = window.attributes
                params.screenBrightness = currentBrightness
                window.attributes = params
            }

            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    // 亮度平滑过渡
                    val step = 0.02f * brightnessDirection
                    currentBrightness = (currentBrightness + step).coerceIn(0.1f, 1.0f)

                    // 到达边界时反转方向
                    if (currentBrightness >= 1.0f || currentBrightness <= 0.1f) {
                        brightnessDirection = -brightnessDirection
                    }

                    delay(30)
                }
            }

            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                saveAndFinish(false)
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
