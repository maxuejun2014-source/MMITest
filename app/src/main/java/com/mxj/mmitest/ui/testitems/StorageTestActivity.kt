package com.mxj.mmitest.ui.testitems

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
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
import java.io.File

/**
 * 存储测试Activity
 */
class StorageTestActivity : BaseActivity() {

    private val testName = "存储测试"
    private val timeoutSeconds = 30
    private val testItemId = 2
    private lateinit var repository: TestRepository

    // 获取存储空间信息
    private fun getStorageInfo(path: String): Pair<Long, Long>? {
        return try {
            val statFs = StatFs(path)
            val totalBytes = statFs.totalBytes
            val availableBytes = statFs.availableBytes
            val usedBytes = totalBytes - availableBytes
            Pair(usedBytes, totalBytes)
        } catch (e: Exception) {
            null
        }
    }

    // 格式化字节大小
    private fun formatSize(sizeBytes: Long): String {
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$sizeBytes B"
        }
    }

    // 检测可移动SD卡
    private fun getRemovableSdCard(): File? {
        val dirs = ContextCompat.getExternalFilesDirs(this, null)
        if (dirs.size > 1 && dirs[1] != null && Environment.isExternalStorageRemovable(dirs[1])) {
            val path = dirs[1].absolutePath
            val androidDataIndex = path.indexOf("/Android/data")
            if (androidDataIndex > 0) {
                return File(path.substring(0, androidDataIndex))
            }
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)

        // 内部存储信息
        val internalStorage = getStorageInfo(Environment.getDataDirectory().path)

        // 外插SD卡信息（可移动存储）
        val removableSdCard = getRemovableSdCard()
        val sdCardStorage = removableSdCard?.let { getStorageInfo(it.absolutePath) }

        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var passEnabled by remember { mutableStateOf(true) }

            TestItemScreen(
                testName = testName,
                testDescription = "检测内部存储和SD卡",
                remainingSeconds = remainingSeconds,
                onPass = { saveAndFinish(true) },
                onFail = { saveAndFinish(false) },
                passEnabled = passEnabled,
                content = {
                    // 自定义内容：存储信息
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // 内部存储
                        Text(
                            text = "内部存储",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        internalStorage?.let { (used, total) ->
                            StorageInfoRow(
                                used = used,
                                total = total,
                                formatSize = ::formatSize
                            )
                        } ?: Text(
                            text = "无法获取内部存储信息",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // SD卡（可移动存储）
                        Text(
                            text = "SD卡（外置）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (removableSdCard != null && sdCardStorage != null) {
                            val (used, total) = sdCardStorage
                            StorageInfoRow(
                                used = used,
                                total = total,
                                formatSize = ::formatSize
                            )
                        } else {
                            Text(
                                text = "未检测到外置SD卡",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            )

            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i == 0) { break }
                    delay(1000)
                }
                if (remainingSeconds == 0) saveAndFinish(false)
            }
        }
    }

    @Composable
    private fun StorageInfoRow(
        used: Long,
        total: Long,
        formatSize: (Long) -> String
    ) {
        val usagePercent = if (total > 0) (used.toFloat() / total * 100).toInt() else 0

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已用空间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "${formatSize(used)} / ${formatSize(total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { usagePercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    usagePercent > 90 -> Color.Red
                    usagePercent > 70 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                trackColor = Color.LightGray,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "使用率: $usagePercent%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
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
