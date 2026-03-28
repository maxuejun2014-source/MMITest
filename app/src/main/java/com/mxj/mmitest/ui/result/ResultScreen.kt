package com.mxj.mmitest.ui.result

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mxj.mmitest.data.local.TestSessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 测试结果界面Compose屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBackClick: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val selectedSummary by viewModel.selectedSummary.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    val selectedSessionId by viewModel.selectedSessionId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("测试结果") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (currentTab == ResultTab.HISTORY && sessions.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllSessions() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除全部"
                            )
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
        ) {
            // Tab选择器
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = currentTab == ResultTab.HISTORY,
                    onClick = { viewModel.switchTab(ResultTab.HISTORY) },
                    text = { Text("历史记录") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = currentTab == ResultTab.QR_CODE,
                    onClick = { viewModel.switchTab(ResultTab.QR_CODE) },
                    text = { Text("二维码") },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                )
            }

            // 内容区
            when (currentTab) {
                ResultTab.HISTORY -> {
                    HistoryTab(
                        sessions = sessions,
                        selectedSessionId = selectedSessionId,
                        isLoading = isLoading,
                        onSessionClick = { viewModel.selectSession(it.sessionId) },
                        onDeleteClick = { viewModel.deleteSession(it.sessionId) }
                    )
                }
                ResultTab.QR_CODE -> {
                    QrCodeTab(
                        summary = selectedSummary,
                        qrCodeBitmap = qrCodeBitmap,
                        resultText = viewModel.getResultText(),
                        hasSelectedSession = selectedSessionId != null
                    )
                }
            }
        }
    }
}

/**
 * 历史记录Tab
 */
@Composable
private fun HistoryTab(
    sessions: List<TestSessionEntity>,
    selectedSessionId: String?,
    isLoading: Boolean,
    onSessionClick: (TestSessionEntity) -> Unit,
    onDeleteClick: (TestSessionEntity) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无历史记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "请先运行自动测试",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions) { session ->
            SessionCard(
                session = session,
                isSelected = session.sessionId == selectedSessionId,
                onClick = { onSessionClick(session) },
                onDeleteClick = { onDeleteClick(session) }
            )
        }
    }
}

/**
 * 会话卡片
 */
@Composable
private fun SessionCard(
    session: TestSessionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateFormat.format(Date(session.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 结果统计
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ResultChip(
                        label = "通过",
                        count = session.passedCount,
                        color = Color(0xFF4CAF50)
                    )
                    ResultChip(
                        label = "失败",
                        count = session.failedCount,
                        color = Color(0xFFF44336)
                    )
                    ResultChip(
                        label = "跳过",
                        count = session.skippedCount,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 结果标签
 */
@Composable
private fun ResultChip(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 二维码Tab
 */
@Composable
private fun QrCodeTab(
    summary: com.mxj.mmitest.domain.model.TestResultSummary?,
    qrCodeBitmap: Bitmap?,
    resultText: String,
    hasSelectedSession: Boolean
) {
    if (!hasSelectedSession || summary == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "请先选择一条历史记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 测试汇总信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "测试汇总",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("设备型号", summary.model)
                SummaryRow("设备厂商", summary.manufacturer)
                SummaryRow("测试时间", summary.testDate)
                SummaryRow("测试员", summary.operator)

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // 大字显示统计
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BigStatItem(
                        label = "通过",
                        count = summary.summary.passed,
                        total = summary.summary.total,
                        color = Color(0xFF4CAF50)
                    )
                    BigStatItem(
                        label = "失败",
                        count = summary.summary.failed,
                        total = summary.summary.total,
                        color = Color(0xFFF44336)
                    )
                    BigStatItem(
                        label = "跳过",
                        count = summary.summary.skipped,
                        total = summary.summary.total,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 二维码显示
        Card(
            modifier = Modifier.size(280.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "扫描二维码查看完整测试结果",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 详细结果文本
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0E0E0),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * 汇总行
 */
@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 大号统计项
 */
@Composable
private fun BigStatItem(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "/$total",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
