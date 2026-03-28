package com.mxj.mmitest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 超时对话框组件
 */
@Composable
fun TimeoutDialog(
    remainingSeconds: Int,
    onContinueWait: () -> Unit,
    onMarkFailed: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text("测试超时")
        },
        text = {
            Column {
                Text("测试项在 $remainingSeconds 秒内未完成操作")
                Spacer(modifier = Modifier.height(8.dp))
                Text("请选择下一步操作：")
            }
        },
        confirmButton = {
            TextButton(onClick = onContinueWait) {
                Text("继续等待")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSkip) {
                    Text("跳过")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onMarkFailed) {
                    Text("标记失败", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
