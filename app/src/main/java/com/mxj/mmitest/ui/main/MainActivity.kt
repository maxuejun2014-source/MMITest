package com.mxj.mmitest.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 应用标题
        Text(
            text = "手机工厂测试",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "MMITest",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 测试项数量信息
        Text(
            text = "共 ${viewModel.totalTestCount} 项测试",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 四个功能按钮
        MainMenuButton(
            text = "自动测试",
            description = "按顺序执行所有测试项",
            backgroundColor = Color(0xFF2196F3), // 蓝色
            onClick = onAutoTestClick,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MainMenuButton(
            text = "单项测试",
            description = "选择单个测试项进行测试",
            backgroundColor = Color(0xFF4CAF50), // 绿色
            onClick = onSingleTestClick,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MainMenuButton(
            text = "测试结果",
            description = "查看历史测试记录",
            backgroundColor = Color(0xFFFF9800), // 橙色
            onClick = onResultClick,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MainMenuButton(
            text = "退出",
            description = "关闭应用程序",
            backgroundColor = Color(0xFFF44336), // 红色
            onClick = onExitClick,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // 版本信息
        Text(
            text = "版本 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 主界面菜单按钮组件
 */
@Composable
fun MainMenuButton(
    text: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
