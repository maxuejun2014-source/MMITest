package com.mxj.mmitest.ui.testitems

import android.view.KeyEvent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

/**
 * 按键测试Activity
 */
class ButtonTestActivity : BaseActivity() {
    private val testName = "按键测试"
    private val timeoutSeconds = 30
    private val testItemId = 7
    private lateinit var repository: TestRepository

    // 记录已按下的按键
    private var volumeUpPressed by mutableStateOf(false)
    private var volumeDownPressed by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            // 默认PASS按钮不可点击，需要按下音量键后才可点击
            val passEnabled by remember { mutableStateOf(false) }

            TestItemScreen(
                testName = testName,
                testDescription = "物理按键和虚拟按键测试\n\n" +
                        "操作步骤：\n" +
                        "1. 测试音量+键\n" +
                        "2. 测试音量-键\n" +
                        "3. 点击PASS或FAIL按钮",
                remainingSeconds = remainingSeconds,
                passEnabled = volumeUpPressed && volumeDownPressed,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                content = {
                    // 自定义内容：按键状态显示
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "请按下以下按键：",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 音量+键状态
                        KeyStatusRow(
                            keyName = "音量+",
                            isPressed = volumeUpPressed
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 音量-键状态
                        KeyStatusRow(
                            keyName = "音量-",
                            isPressed = volumeDownPressed
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 提示文字
                        if (!volumeUpPressed || !volumeDownPressed) {
                            Text(
                                text = "请先按下音量键...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "按键检测成功，可以点击PASS",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )

            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0) delay(1000)
                }
                // 超时自动结束
                saveAndFinish(false)
            }
        }
    }

    @Composable
    private fun KeyStatusRow(keyName: String, isPressed: Boolean) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPressed) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = keyName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isPressed) "已按下 ✓" else "未按下",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed) Color(0xFF4CAF50) else Color.Gray
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                volumeUpPressed = true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeDownPressed = true
            }
        }
        return super.onKeyDown(keyCode, event)
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
