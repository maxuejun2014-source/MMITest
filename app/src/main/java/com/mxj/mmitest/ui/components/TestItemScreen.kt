package com.mxj.mmitest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 测试项界面组件
 * 通用的测试项界面布局，包含标题、描述、倒计时和PASS/FAIL按钮
 * passEnabled控制PASS按钮是否可点击，FAIL按钮始终可点击
 */
@Composable
fun TestItemScreen(
    testName: String,
    testDescription: String,
    remainingSeconds: Int,
    onPass: () -> Unit,
    onFail: () -> Unit,
    content: (@Composable () -> Unit)? = null,
    passEnabled: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 顶部：测试标题和描述
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = testName,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 倒计时显示
            Text(
                text = "剩余时间: ${remainingSeconds}秒",
                style = MaterialTheme.typography.bodyMedium,
                color = if (remainingSeconds <= 5)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = testDescription,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 自定义测试内容
            content?.invoke()
        }

        // PASS/FAIL按钮
        TestResultButtons(
            onPass = onPass,
            onFail = onFail,
            passEnabled = passEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )
    }
}
