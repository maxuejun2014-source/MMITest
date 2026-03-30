package com.mxj.mmitest.ui.autotest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxj.mmitest.domain.model.TestResultStatus

/**
 * 自动测试界面Compose屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTestScreen(
    viewModel: AutoTestViewModel,
    onBackClick: () -> Unit,
    onTestComplete: () -> Unit = {}
) {
    val testState by viewModel.testState.collectAsState()
    val currentTestState by viewModel.currentTestState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val testResults by viewModel.testResults.collectAsState()
    val resultSummary by viewModel.resultSummary.collectAsState()
    val listState = rememberLazyListState()

    var showConfigSheet by remember { mutableStateOf(false) }
    var currentConfig by remember { mutableStateOf(AutoTestConfig()) }

    // 自动滚动日志
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    // 测试完成时显示结果
    LaunchedEffect(testState) {
        if (testState == AutoTestState.COMPLETED) {
            // 测试完成
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自动测试") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (testState == AutoTestState.IDLE) {
                        IconButton(onClick = { showConfigSheet = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "配置")
                        }
                        TextButton(onClick = onBackClick) {
                            Text("返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 状态显示区
            StatusSection(
                testState = testState,
                currentTestState = currentTestState,
                progress = progress,
                totalCount = viewModel.testQueue.value.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 控制按钮区
            ControlButtons(
                testState = testState,
                onStartClick = { viewModel.startTest() },
                onPauseClick = { viewModel.pauseTest() },
                onResumeClick = { viewModel.resumeTest() },
                onStopClick = { viewModel.stopTest() },
                onResetClick = { viewModel.reset() },
                onViewResultsClick = onTestComplete
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 结果统计区
            if (testResults.isNotEmpty()) {
                ResultStatsSection(
                    passed = testResults.count { it.result == TestResultStatus.PASS },
                    failed = testResults.count { it.result == TestResultStatus.FAIL },
                    skipped = testResults.count {
                        it.result == TestResultStatus.SKIPPED || it.result == TestResultStatus.TIMEOUT
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 日志输出区
            Text(
                text = "测试日志",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LogSection(
                logs = logs,
                listState = listState,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // 配置BottomSheet
    if (showConfigSheet) {
        AutoTestConfigSheet(
            currentConfig = currentConfig,
            onDismiss = { showConfigSheet = false },
            onStartTest = { config ->
                currentConfig = config
                showConfigSheet = false
                viewModel.updateTestQueue(config.testItems, config.customTimeouts)
                viewModel.startTest()
            }
        )
    }
}

/**
 * 状态显示区
 */
@Composable
private fun StatusSection(
    testState: AutoTestState,
    currentTestState: CurrentTestState,
    progress: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 状态文字
            Text(
                text = when (testState) {
                    AutoTestState.IDLE -> "等待开始"
                    AutoTestState.RUNNING -> "测试中..."
                    AutoTestState.PAUSED -> "已暂停"
                    AutoTestState.COMPLETED -> "测试完成"
                    AutoTestState.STOPPED -> "已停止"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when (testState) {
                    AutoTestState.RUNNING -> MaterialTheme.colorScheme.primary
                    AutoTestState.COMPLETED -> Color(0xFF4CAF50)
                    AutoTestState.STOPPED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 当前测试项
            if (testState == AutoTestState.RUNNING && currentTestState.testName.isNotEmpty()) {
                Text(
                    text = "正在测试: ${currentTestState.testName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "(${currentTestState.testIndex}/$totalCount)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = when (testState) {
                    AutoTestState.COMPLETED -> Color(0xFF4CAF50)
                    AutoTestState.STOPPED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$progress%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 控制按钮区
 */
@Composable
private fun ControlButtons(
    testState: AutoTestState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onResetClick: () -> Unit,
    onViewResultsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (testState) {
            AutoTestState.IDLE -> {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("开始测试", fontSize = 16.sp)
                }
            }

            AutoTestState.RUNNING -> {
                Button(
                    onClick = onPauseClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("暂停", fontSize = 16.sp)
                }
                Button(
                    onClick = onStopClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("停止", fontSize = 16.sp)
                }
            }

            AutoTestState.PAUSED -> {
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("继续", fontSize = 16.sp)
                }
                Button(
                    onClick = onStopClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("停止", fontSize = 16.sp)
                }
            }

            AutoTestState.COMPLETED, AutoTestState.STOPPED -> {
                Button(
                    onClick = onResetClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置", fontSize = 16.sp)
                }
                Button(
                    onClick = onViewResultsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("查看结果", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * 结果统计区
 */
@Composable
private fun ResultStatsSection(
    passed: Int,
    failed: Int,
    skipped: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "通过",
                count = passed,
                color = Color(0xFF4CAF50)
            )
            StatItem(
                label = "失败",
                count = failed,
                color = Color(0xFFF44336)
            )
            StatItem(
                label = "跳过",
                count = skipped,
                color = Color(0xFFFF9800)
            )
        }
    }
}

/**
 * 统计单项
 */
@Composable
private fun StatItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 日志输出区
 */
@Composable
private fun LogSection(
    logs: List<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        log.contains("✓") -> Color(0xFF4CAF50)
                        log.contains("✗") -> Color(0xFFF44336)
                        log.contains("⊘") -> Color(0xFFFF9800)
                        log.contains("⏱") -> Color(0xFFFF9800)
                        log.contains("开始") || log.contains("完成") -> Color(0xFF2196F3)
                        else -> Color(0xFFE0E0E0)
                    },
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}
