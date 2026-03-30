package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.ui.base.BaseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TpTestActivity : BaseActivity() {
    private val testName = "TP测试"
    private val timeoutSeconds = 45
    private val testItemId = 8
    private lateinit var repository: TestRepository

    // 横线数量
    private val horizontalLineCount = 6
    // 竖线数量
    private val verticalLineCount = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            // 记录水平和竖直线条是否被划过
            var horizontalLinesCompleted by remember { mutableStateOf(setOf<Int>()) }
            var verticalLinesCompleted by remember { mutableStateOf(setOf<Int>()) }
            // 划线时的当前位置
            var currentDragPosition by remember { mutableStateOf<Offset?>(null) }
            // 测试是否完成
            var testCompleted by remember { mutableStateOf(false) }

            // 检查是否所有线条都已完成
            val allLinesCompleted = horizontalLinesCompleted.size >= horizontalLineCount &&
                    verticalLinesCompleted.size >= verticalLineCount

            // 自动点击PASS
            LaunchedEffect(allLinesCompleted) {
                if (allLinesCompleted && !testCompleted) {
                    testCompleted = true
                    delay(500)
                    saveAndFinish(true)
                }
            }

            // 超时处理
            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0) delay(1000)
                }
                if (!testCompleted) {
                    saveAndFinish(false)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // 背景和线条绘制层
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 绘制背景
                    drawRect(Color.Black)

                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // 计算线条位置
                    val hPositions = (1..horizontalLineCount).map { canvasHeight * it / (horizontalLineCount + 1) }
                    val vPositions = (1..verticalLineCount).map { canvasWidth * it / (verticalLineCount + 1) }

                    // 绘制水平线条
                    hPositions.forEachIndexed { index, y ->
                        val isCompleted = horizontalLinesCompleted.contains(index)
                        drawLine(
                            color = if (isCompleted) Color.Green else Color.White.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 绘制竖直线条
                    vPositions.forEachIndexed { index, x ->
                        val isCompleted = verticalLinesCompleted.contains(index)
                        drawLine(
                            color = if (isCompleted) Color.Green else Color.White.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, canvasHeight),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 绘制触摸点
                    currentDragPosition?.let { pos ->
                        drawCircle(
                            color = Color.Yellow,
                            radius = 12.dp.toPx(),
                            center = pos
                        )
                    }
                }

                // 触摸检测层 - 水平拖动
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    currentDragPosition = offset
                                    val canvasHeight = size.height.toFloat()
                                    val hPositions = (1..horizontalLineCount).map { canvasHeight * it / (horizontalLineCount + 1) }
                                    hPositions.forEachIndexed { index, y ->
                                        if (kotlin.math.abs(offset.y - y) < 60.dp.toPx()) {
                                            horizontalLinesCompleted = horizontalLinesCompleted + index
                                        }
                                    }
                                },
                                onDragEnd = {
                                    currentDragPosition = null
                                },
                                onDragCancel = {
                                    currentDragPosition = null
                                },
                                onHorizontalDrag = { change, _ ->
                                    currentDragPosition = change.position
                                    val canvasHeight = size.height.toFloat()
                                    val hPositions = (1..horizontalLineCount).map { canvasHeight * it / (horizontalLineCount + 1) }
                                    hPositions.forEachIndexed { index, y ->
                                        if (kotlin.math.abs(change.position.y - y) < 60.dp.toPx()) {
                                            horizontalLinesCompleted = horizontalLinesCompleted + index
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    currentDragPosition = offset
                                    val canvasWidth = size.width.toFloat()
                                    val vPositions = (1..verticalLineCount).map { canvasWidth * it / (verticalLineCount + 1) }
                                    vPositions.forEachIndexed { index, x ->
                                        if (kotlin.math.abs(offset.x - x) < 60.dp.toPx()) {
                                            verticalLinesCompleted = verticalLinesCompleted + index
                                        }
                                    }
                                },
                                onDragEnd = {
                                    currentDragPosition = null
                                },
                                onDragCancel = {
                                    currentDragPosition = null
                                },
                                onVerticalDrag = { change, _ ->
                                    currentDragPosition = change.position
                                    val canvasWidth = size.width.toFloat()
                                    val vPositions = (1..verticalLineCount).map { canvasWidth * it / (verticalLineCount + 1) }
                                    vPositions.forEachIndexed { index, x ->
                                        if (kotlin.math.abs(change.position.x - x) < 60.dp.toPx()) {
                                            verticalLinesCompleted = verticalLinesCompleted + index
                                        }
                                    }
                                }
                            )
                        }
                )

                // 顶部状态栏
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = testName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "请滑动屏幕填充所有线条",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "已完成: ${horizontalLinesCompleted.size}/$horizontalLineCount 横线, ${verticalLinesCompleted.size}/$verticalLineCount 竖线",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (allLinesCompleted) Color.Green else Color.Yellow
                    )
                }

                // 底部进度提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                ) {
                    if (allLinesCompleted) {
                        Text(
                            text = "测试完成！",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Green
                        )
                    } else {
                        val remainingH = horizontalLineCount - horizontalLinesCompleted.size
                        val remainingV = verticalLineCount - verticalLinesCompleted.size
                        Text(
                            text = "剩余: $remainingH 横线, $remainingV 竖线",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    private fun saveAndFinish(passed: Boolean) {
        lifecycleScope.launch {
            repository.saveSingleTestResult(
                testItemId = testItemId,
                testItemName = testName,
                passed = passed,
                deviceId = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: android.os.Build.MODEL
            )
            finish()
        }
    }
}
