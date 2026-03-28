package com.mxj.mmitest.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.autotest.AutoTestActivity
import com.mxj.mmitest.ui.singletest.SingleTestActivity
import com.mxj.mmitest.ui.result.ResultActivity

/**
 * 主界面Activity
 * 显示四个主要功能按钮：自动测试、单项测试、测试结果、退出
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = viewModel()
            MainScreen(
                viewModel = viewModel,
                onAutoTestClick = { startActivity(Intent(this, AutoTestActivity::class.java)) },
                onSingleTestClick = { startActivity(Intent(this, SingleTestActivity::class.java)) },
                onResultClick = { startActivity(Intent(this, ResultActivity::class.java)) },
                onExitClick = { finish() }
            )
        }
    }
}

/**
 * 主界面Compose屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAutoTestClick: () -> Unit,
    onSingleTestClick: () -> Unit,
    onResultClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 应用标题
        Text(
            text = "手机工厂测试",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "MMITest",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // 测试项数量信息
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "共 ${viewModel.totalTestCount} 项测试",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 四个功能按钮卡片
        MainMenuCard(
            icon = Icons.Default.PlayArrow,
            title = "自动测试",
            description = "按顺序执行所有测试项，失败后继续",
            backgroundColor = Color(0xFF2196F3),
            onClick = onAutoTestClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        MainMenuCard(
            icon = Icons.Default.List,
            title = "单项测试",
            description = "选择单个测试项进行测试",
            backgroundColor = Color(0xFF4CAF50),
            onClick = onSingleTestClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        MainMenuCard(
            icon = Icons.Default.History,
            title = "测试结果",
            description = "查看历史测试记录和二维码",
            backgroundColor = Color(0xFFFF9800),
            onClick = onResultClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        MainMenuCard(
            icon = Icons.Default.ExitToApp,
            title = "退出",
            description = "关闭应用程序",
            backgroundColor = Color(0xFFF44336),
            onClick = onExitClick
        )

        Spacer(modifier = Modifier.weight(1f))

        // 版本信息
        Text(
            text = "版本 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 主界面菜单卡片组件
 * 优化的卡片式设计，带图标
 */
@Composable
fun MainMenuCard(
    icon: ImageVector,
    title: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
