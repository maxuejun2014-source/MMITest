package com.mxj.mmitest.ui.singletest

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxj.mmitest.config.TestConfig
import com.mxj.mmitest.ui.base.BaseActivity

/**
 * 单项测试选择界面Activity
 * 显示所有可用的测试项列表
 */
class SingleTestActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: SingleTestViewModel = viewModel()

            // 监听生命周期，在onResume时刷新状态
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.loadTestStatuses()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            SingleTestScreen(
                viewModel = viewModel,
                onTestItemClick = { testItem ->
                    val intent = Intent(this, testItem.activityClass)
                    startActivity(intent)
                },
                onBackClick = { finish() }
            )
        }
    }
}

/**
 * 获取测试状态颜色
 */
fun getTestStatusColor(status: TestStatus): Color {
    return when (status) {
        TestStatus.NOT_TESTED -> Color(0xFF9E9E9E)
        TestStatus.PASSED -> Color(0xFF4CAF50)
        TestStatus.FAILED -> Color(0xFFF44336)
    }
}

/**
 * 单项测试选择界面Compose屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleTestScreen(
    viewModel: SingleTestViewModel,
    onTestItemClick: (TestConfig.TestItem) -> Unit,
    onBackClick: () -> Unit
) {
    val testItemStatuses by viewModel.testItemStatuses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "单项测试",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 列表头部分隔线
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFBDBDBD)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.testItems) { testItem ->
                    val status = testItemStatuses[testItem.id] ?: TestStatus.NOT_TESTED
                    TestItemRow(
                        testItem = testItem,
                        status = status,
                        onClick = { onTestItemClick(testItem) }
                    )
                }
            }
        }
    }
}

/**
 * 测试项列表行
 * 简化设计：无彩色圆形图标，参考list_item_report.xml样式
 */
@Composable
private fun TestItemRow(
    testItem: TestConfig.TestItem,
    status: TestStatus,
    onClick: () -> Unit
) {
    val isEnabled = testItem.enabledByDefault && testItem.supportedByDefault
    val statusColor = getTestStatusColor(status)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isEnabled)
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 26.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：测试项名称
            Text(
                text = testItem.name,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isEnabled)
                    Color(0xFF333333)
                else
                    Color(0xFF333333).copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )

            // 右侧：测试状态（带符号）
            Text(
                text = when (status) {
                    TestStatus.NOT_TESTED -> "⚪ NOT TESTED"
                    TestStatus.PASSED -> "✅ PASSED"
                    TestStatus.FAILED -> "❌ FAILED"
                    else -> "⚪ NOT TESTED"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        // 分隔线
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFDADADA),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
