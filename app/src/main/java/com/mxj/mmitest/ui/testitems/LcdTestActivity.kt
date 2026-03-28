package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mxj.mmitest.ui.base.BaseActivity
import com.mxj.mmitest.ui.components.TestResultButtons
import kotlinx.coroutines.delay

/**
 * LCD测试Activity
 * 防呆设计：按顺序显示多种颜色
 */
class LcdTestActivity : BaseActivity() {

    private val testName = "LCD测试"
    private val timeoutSeconds = 60
    private val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.White, Color.Black
    )
    private val colorNames = listOf("红", "绿", "蓝", "白", "黑")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentColorIndex by remember { mutableStateOf(0) }
            var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors[currentColorIndex])
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = testName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (colors[currentColorIndex] == Color.Black ||
                                       colors[currentColorIndex] == Color.Blue) Color.White else Color.Black
                        )
                        Text(
                            text = "剩余时间: ${remainingSeconds}秒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (colors[currentColorIndex] == Color.Black ||
                                       colors[currentColorIndex] == Color.Blue) Color.White else Color.Black
                        )
                        Text(
                            text = "当前颜色: ${colorNames[currentColorIndex]}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (colors[currentColorIndex] == Color.Black ||
                                       colors[currentColorIndex] == Color.Blue) Color.White else Color.Black
                        )
                    }

                    TestResultButtons(
                        onPass = { finish() },
                        onFail = { finish() }
                    )
                }
            }

            // 自动切换颜色
            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0 && i % 12 == 0 && currentColorIndex < colors.size - 1) {
                        currentColorIndex++
                    }
                    if (i == 0) break
                    delay(1000)
                }
            }
        }
    }
}
