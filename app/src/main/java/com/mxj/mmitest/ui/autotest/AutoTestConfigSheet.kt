package com.mxj.mmitest.ui.autotest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mxj.mmitest.config.TestConfig

/**
 * 自动测试配置数据类
 */
data class AutoTestConfig(
    val testItems: List<TestConfig.TestItem> = TestConfig.getEnabledTestItems(),
    val customTimeouts: Map<Int, Int> = emptyMap() // 测试项ID到自定义超时时间的映射
)

/**
 * 自动测试配置BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTestConfigSheet(
    currentConfig: AutoTestConfig,
    onDismiss: () -> Unit,
    onStartTest: (AutoTestConfig) -> Unit
) {
    var config by remember { mutableStateOf(currentConfig) }
    var editingTimeoutItemId by remember { mutableStateOf<Int?>(null) }
    var tempTimeout by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "自动测试配置",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        config = AutoTestConfig() // 重置为默认配置
                    }
                ) {
                    Text("重置")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 统计信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${config.testItems.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("测试项", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${config.testItems.sumOf { config.customTimeouts[it.id] ?: it.timeoutSeconds }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("总超时(秒)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 测试项列表
            Text(
                text = "测试项列表（点击编辑超时时间）",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(config.testItems) { index, item ->
                    TestItemConfigCard(
                        index = index + 1,
                        item = item,
                        customTimeout = config.customTimeouts[item.id],
                        onTimeoutClick = {
                            editingTimeoutItemId = item.id
                            tempTimeout = config.customTimeouts[item.id] ?: item.timeoutSeconds
                        },
                        onRemove = {
                            config = config.copy(
                                testItems = config.testItems.filter { it.id != item.id }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }

                Button(
                    onClick = { onStartTest(config) },
                    modifier = Modifier.weight(1f),
                    enabled = config.testItems.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始测试")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 超时时间编辑对话框
    if (editingTimeoutItemId != null) {
        val item = config.testItems.find { it.id == editingTimeoutItemId }
        if (item != null) {
            AlertDialog(
                onDismissRequest = { editingTimeoutItemId = null },
                title = { Text("设置超时时间") },
                text = {
                    Column {
                        Text("测试项: ${item.name}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("默认时间: ${item.timeoutSeconds}秒")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = tempTimeout.toString(),
                            onValueChange = { value ->
                                tempTimeout = value.toIntOrNull() ?: 0
                            },
                            label = { Text("自定义超时（秒）") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempTimeout > 0 && tempTimeout != item.timeoutSeconds) {
                                config = config.copy(
                                    customTimeouts = config.customTimeouts + (item.id to tempTimeout)
                                )
                            } else {
                                config = config.copy(
                                    customTimeouts = config.customTimeouts - item.id
                                )
                            }
                            editingTimeoutItemId = null
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingTimeoutItemId = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

/**
 * 测试项配置卡片
 */
@Composable
private fun TestItemConfigCard(
    index: Int,
    item: TestConfig.TestItem,
    customTimeout: Int?,
    onTimeoutClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "超时: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        TextButton(
                            onClick = onTimeoutClick,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (customTimeout != null) "${customTimeout}秒" else "${item.timeoutSeconds}秒（默认）",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (customTimeout != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "移除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
