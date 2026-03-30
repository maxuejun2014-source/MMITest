package com.mxj.mmitest.ui.result

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxj.mmitest.data.local.TestSessionEntity
import com.mxj.mmitest.domain.model.TestResultSummary
import com.mxj.mmitest.ui.components.BarChartData
import com.mxj.mmitest.ui.components.SimpleBarChart
import com.mxj.mmitest.ui.components.SimplePieChart
import com.mxj.mmitest.util.ExportFormat

/**
 * 测试结果界面
 * 包含历史记录和二维码显示两个Tab页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBackClick: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val filteredSessions by viewModel.filteredSessions.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedSessionId by viewModel.selectedSessionId.collectAsState()
    val selectedSummary by viewModel.selectedSummary.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val filterCriteria by viewModel.filterCriteria.collectAsState()
    val hasActiveFilter by viewModel.hasActiveFilter.collectAsState()
    val availableDeviceModels by viewModel.availableDeviceModels.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("测试结果") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("返回", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab切换
            TabRow(
                selectedTabIndex = when (currentTab) {
                    ResultTab.HISTORY -> 0
                    ResultTab.STATISTICS -> 1
                    ResultTab.QR_CODE -> 2
                }
            ) {
                Tab(
                    selected = currentTab == ResultTab.HISTORY,
                    onClick = { viewModel.switchTab(ResultTab.HISTORY) },
                    text = { Text("历史记录") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = currentTab == ResultTab.STATISTICS,
                    onClick = { viewModel.switchTab(ResultTab.STATISTICS) },
                    text = { Text("统计") },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                )
                Tab(
                    selected = currentTab == ResultTab.QR_CODE,
                    onClick = { viewModel.switchTab(ResultTab.QR_CODE) },
                    text = { Text("二维码") },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                )
            }

            // 内容区域
            when (currentTab) {
                ResultTab.HISTORY -> {
                    HistoryTab(
                        sessions = filteredSessions,
                        selectedSessionId = selectedSessionId,
                        isLoading = isLoading,
                        onSessionClick = { session ->
                            viewModel.selectSession(session.sessionId)
                        },
                        onDeleteClick = { session ->
                            viewModel.deleteSession(session.sessionId)
                        },
                        onDeleteAllClick = {
                            viewModel.deleteAllSessions()
                        },
                        filterCriteria = filterCriteria,
                        hasActiveFilter = hasActiveFilter,
                        availableDeviceModels = availableDeviceModels,
                        onFilterChange = { viewModel.updateFilter(it) },
                        onClearFilter = { viewModel.clearFilter() }
                    )
                }
                ResultTab.STATISTICS -> {
                    StatisticsTab(statistics = statistics)
                }
                ResultTab.QR_CODE -> {
                    QrCodeTab(
                        summary = selectedSummary,
                        qrCodeBitmap = qrCodeBitmap,
                        resultText = viewModel.getResultText(),
                        onExportClick = { showExportDialog = true }
                    )
                }
            }
        }
    }

    // 导出对话框
    if (showExportDialog) {
        ExportDialog(
            onDismiss = {
                showExportDialog = false
                viewModel.resetExportState()
            },
            onExport = { format ->
                viewModel.exportResult(format)
            },
            exportState = exportState
        )
    }
}

/**
 * 历史记录Tab页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTab(
    sessions: List<TestSessionEntity>,
    selectedSessionId: String?,
    isLoading: Boolean,
    onSessionClick: (TestSessionEntity) -> Unit,
    onDeleteClick: (TestSessionEntity) -> Unit,
    onDeleteAllClick: () -> Unit,
    filterCriteria: FilterCriteria,
    hasActiveFilter: Boolean,
    availableDeviceModels: List<String>,
    onFilterChange: (FilterCriteria) -> Unit,
    onClearFilter: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 筛选按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    onClick = { showFilterSheet = true },
                    label = {
                        Text(if (hasActiveFilter) "已筛选" else "筛选")
                    },
                    selected = hasActiveFilter,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                if (hasActiveFilter) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = onClearFilter) {
                        Text("清除", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 结果数量
            Text(
                text = "共 ${sessions.size} 条",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // 删除全部按钮
            if (sessions.isNotEmpty()) {
                TextButton(
                    onClick = onDeleteAllClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("清除全部")
                }
            }
        }

        // 加载状态
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (sessions.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无测试记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.sessionId }) { session ->
                    SessionItem(
                        session = session,
                        isSelected = session.sessionId == selectedSessionId,
                        onClick = { onSessionClick(session) },
                        onDeleteClick = { onDeleteClick(session) }
                    )
                }
            }
        }
    }

    // 筛选BottomSheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentCriteria = filterCriteria,
            availableDeviceModels = availableDeviceModels,
            onDismiss = { showFilterSheet = false },
            onApply = { newCriteria ->
                onFilterChange(newCriteria)
                showFilterSheet = false
            }
        )
    }
}

/**
 * 会话列表项
 */
@Composable
private fun SessionItem(
    session: TestSessionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.deviceModel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = session.deviceManufacturer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 测试时间
            Text(
                text = formatTime(session.startTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 结果统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultCount(
                    label = "通过",
                    count = session.passedCount,
                    color = Color(0xFF4CAF50)
                )
                ResultCount(
                    label = "失败",
                    count = session.failedCount,
                    color = Color(0xFFF44336)
                )
                ResultCount(
                    label = "跳过",
                    count = session.skippedCount,
                    color = Color(0xFFFF9800)
                )
                ResultCount(
                    label = "总计",
                    count = session.totalCount,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 结果计数组件
 */
@Composable
private fun ResultCount(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 二维码Tab页
 */
@Composable
private fun QrCodeTab(
    summary: TestResultSummary?,
    qrCodeBitmap: Bitmap?,
    resultText: String,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (summary == null) {
            // 未选择会话
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请先在历史记录中选择一条测试结果",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 显示测试汇总信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "设备信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("型号: ${summary.model}")
                    Text("厂商: ${summary.manufacturer}")
                    Text("测试时间: ${summary.testDate}")
                    Text("测试员: ${summary.operator}")
                    Text("应用版本: ${summary.appVersion}")

                    Spacer(modifier = Modifier.height(16.dp))

                    // 测试结果汇总
                    Text(
                        text = "测试结果",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.summary.passed}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text("通过", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.summary.failed}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                            Text("失败", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.summary.skipped}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text("跳过", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.summary.total}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("总计", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 二维码显示
            Card(
                modifier = Modifier.size(280.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrCodeBitmap != null) {
                        Image(
                            bitmap = qrCodeBitmap.asImageBitmap(),
                            contentDescription = "测试结果二维码",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "扫描二维码获取完整测试报告",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 导出按钮
            OutlinedButton(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("导出报告")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 详细结果文本
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "详细结果",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * 格式化时间戳
 */
private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

/**
 * 筛选BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentCriteria: FilterCriteria,
    availableDeviceModels: List<String>,
    onDismiss: () -> Unit,
    onApply: (FilterCriteria) -> Unit
) {
    var localCriteria by remember { mutableStateOf(currentCriteria) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "筛选条件",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 设备型号筛选
            Text(
                text = "设备型号",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = modelDropdownExpanded,
                onExpandedChange = { modelDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = localCriteria.deviceModel,
                    onValueChange = { localCriteria = localCriteria.copy(deviceModel = it) },
                    placeholder = { Text("输入设备型号") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (localCriteria.deviceModel.isNotEmpty()) {
                            IconButton(onClick = { localCriteria = localCriteria.copy(deviceModel = "") }) {
                                Icon(Icons.Default.QrCode, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true
                )

                if (availableDeviceModels.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = modelDropdownExpanded,
                        onDismissRequest = { modelDropdownExpanded = false }
                    ) {
                        availableDeviceModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    localCriteria = localCriteria.copy(deviceModel = model)
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 结果状态筛选
            Text(
                text = "结果状态",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultStatusFilter.entries.forEach { status ->
                    FilterChip(
                        onClick = { localCriteria = localCriteria.copy(resultStatus = status) },
                        label = {
                            Text(
                                when (status) {
                                    ResultStatusFilter.ALL -> "全部"
                                    ResultStatusFilter.PASSED -> "全部通过"
                                    ResultStatusFilter.FAILED -> "有失败"
                                    ResultStatusFilter.COMPLETED -> "已完成"
                                }
                            )
                        },
                        selected = localCriteria.resultStatus == status
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        localCriteria = FilterCriteria()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }

                Button(
                    onClick = { onApply(localCriteria) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("应用")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 导出对话框
 */
@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit,
    exportState: ExportState
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出测试结果") },
        text = {
            Column {
                when (exportState) {
                    is ExportState.Idle -> {
                        Text("选择导出格式：")
                        Spacer(modifier = Modifier.height(16.dp))

                        ExportFormat.entries.forEach { format ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = when (format) {
                                            ExportFormat.JSON -> "JSON 格式"
                                            ExportFormat.CSV -> "CSV 格式"
                                            ExportFormat.TXT -> "文本格式"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = when (format) {
                                            ExportFormat.JSON -> "适合程序解析"
                                            ExportFormat.CSV -> "适合表格查看"
                                            ExportFormat.TXT -> "适合打印"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    is ExportState.Exporting -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("正在导出...")
                        }
                    }
                    is ExportState.Success -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("导出成功！", color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "已保存至：${exportState.filePath}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    is ExportState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFF44336)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("导出失败：${exportState.message}", color = Color(0xFFF44336))
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (exportState) {
                is ExportState.Idle -> {
                    Button(onClick = { onExport(selectedFormat) }) {
                        Text("导出")
                    }
                }
                is ExportState.Success, is ExportState.Error -> {
                    Button(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            if (exportState is ExportState.Idle) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

/**
 * 统计Tab页
 */
@Composable
private fun StatisticsTab(statistics: TestStatistics?) {
    if (statistics == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无统计数据",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 总体概览卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "总体统计",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox(label = "测试次数", value = "${statistics.totalSessions}")
                        StatBox(label = "测试项总数", value = "${statistics.totalTests}")
                        StatBox(label = "通过率", value = "${(statistics.passRate * 100).toInt()}%")
                    }
                }
            }
        }

        // 饼图
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "结果分布",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SimplePieChart(
                        passed = statistics.passedTests,
                        failed = statistics.failedTests,
                        skipped = statistics.skippedTests,
                        modifier = Modifier.height(180.dp)
                    )
                }
            }
        }

        // 测试项统计
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "详细统计",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${statistics.passedTests}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text("通过", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${statistics.failedTests}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                            Text("失败", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${statistics.skippedTests}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text("跳过", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // 设备分布
        if (statistics.deviceStats.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "设备分布",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val chartData = statistics.deviceStats.map { (device, count) ->
                            BarChartData(
                                label = device.take(8),
                                value = count,
                                color = Color(0xFF2196F3)
                            )
                        }

                        SimpleBarChart(
                            data = chartData,
                            modifier = Modifier.height(150.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 统计数字卡片
 */
@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
