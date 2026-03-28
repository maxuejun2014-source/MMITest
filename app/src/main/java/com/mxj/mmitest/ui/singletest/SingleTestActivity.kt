package com.mxj.mmitest.ui.singletest

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
 * 单项测试选择界面Compose屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleTestScreen(
    viewModel: SingleTestViewModel,
    onTestItemClick: (TestConfig.TestItem) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单项测试") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.testItems) { testItem ->
                TestItemCard(
                    testItem = testItem,
                    onClick = { onTestItemClick(testItem) }
                )
            }
        }
    }
}

/**
 * 测试项卡片组件
 */
@Composable
fun TestItemCard(
    testItem: TestConfig.TestItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (testItem.enabledByDefault && testItem.supportedByDefault)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    text = "${testItem.id}. ${testItem.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = testItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!testItem.supportedByDefault) {
                    Text(
                        text = "（此设备不支持）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "${testItem.timeoutSeconds}秒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
