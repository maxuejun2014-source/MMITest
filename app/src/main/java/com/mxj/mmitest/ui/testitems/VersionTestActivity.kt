package com.mxj.mmitest.ui.testitems

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestItemScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader

class VersionTestActivity : BaseActivity() {
    private val testName = "版本测试"
    private val timeoutSeconds = 15
    private val testItemId = 4
    private lateinit var repository: TestRepository

    // 读取系统属性
    private fun getSystemProperty(key: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = process.inputStream.bufferedReader()
            val line = reader.readLine() ?: "未知"
            reader.close()
            line
        } catch (e: Exception) {
            "读取失败"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        val displayId = getSystemProperty("ro.build.display.id")
        val fingerprint = getSystemProperty("ro.build.fingerprint")

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(true) }

            TestItemScreen(
                testName = testName,
                testDescription = "系统版本信息核对\n\n请核对以下信息：",
                remainingSeconds = remainingSeconds,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                passEnabled = passEnabled,
                content = {
                    // 自定义内容：版本信息显示
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        InfoRow(label = "版本号", value = displayId)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "Fingerprint", value = fingerprint)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "Model", value = Build.MODEL)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "Brand", value = Build.BRAND)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "Android", value = Build.VERSION.RELEASE)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "SDK", value = Build.VERSION.SDK_INT.toString())
                    }
                }
            )

            LaunchedEffect(Unit) {
                while (remainingSeconds > 0) {
                    delay(1000)
                    remainingSeconds--
                }
                saveAndFinish(false)
            }
        }
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
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
