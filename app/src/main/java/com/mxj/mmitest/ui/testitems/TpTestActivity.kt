package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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

    // 网格配置（参考MTK原厂代码）
    private val YCOUNT = 24 // 横向网格数（行数）
    private val XCOUNT = 15 // 纵向网格数（列数）

    // 颜色配置
    private val gridLineColor = Color.Black       // 网格线颜色（黑色）
    private val gridFillColor = Color(0xFFFFC107)   // 格子填充颜色（黄色）
    private val touchPathColor = Color.Green        // 划线路径颜色（绿色）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            // 记录每个格子是否被触摸过
            var drawnGrid by remember { mutableStateOf(Array(YCOUNT) { BooleanArray(XCOUNT) }) }
            // 触摸路径
            var touchPath by remember { mutableStateOf(listOf<Offset>()) }
            // 测试是否完成
            var testCompleted by remember { mutableStateOf(false) }

            val density = LocalDensity.current

            // 判断是否通过（所有需要检测的格子都被绘制）
            val isPassed = remember(drawnGrid) {
                isGridPassed(drawnGrid)
            }

            // 自动PASS
            LaunchedEffect(isPassed) {
                if (isPassed && !testCompleted) {
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

            // 更新格子状态的函数
            val updateGrid: (Offset, Float, Float) -> Unit = { offset, canvasWidth, canvasHeight ->
                val stepX = canvasWidth / XCOUNT
                val stepY = canvasHeight / YCOUNT

                val x = (offset.x / stepX).toInt()
                val y = (offset.y / stepY).toInt()

                if (x in 0 until XCOUNT && y in 0 until YCOUNT) {
                    if (isNeedCheck(y, x)) {
                        val newGrid = drawnGrid.copyOf()
                        newGrid[y] = newGrid[y].copyOf()
                        newGrid[y][x] = true
                        drawnGrid = newGrid
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // 绘制层
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val stepX = canvasWidth / XCOUNT
                    val stepY = canvasHeight / YCOUNT

                    // 绘制网格线和填充
                    for (i in 0 until YCOUNT) {
                        for (j in 0 until XCOUNT) {
                            if (isNeedCheck(i, j)) {
                                val left = j * stepX + 1
                                val top = i * stepY + 1
                                val right = left + stepX - 1
                                val bottom = top + stepY - 1

                                // 已填充的格子显示黄色
                                if (drawnGrid[i][j]) {
                                    drawRect(
                                        color = gridFillColor,
                                        topLeft = Offset(left, top),
                                        size = Size(right - left, bottom - top)
                                    )
                                }

                                // 绘制黑色边框
                                drawRect(
                                    color = gridLineColor,
                                    topLeft = Offset(left, top),
                                    size = Size(right - left, bottom - top),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }

                    // 绘制触摸路径（绿色虚线）
                    if (touchPath.isNotEmpty()) {
                        for (i in 0 until touchPath.size - 1) {
                            drawLine(
                                color = touchPathColor,
                                start = touchPath[i],
                                end = touchPath[i + 1],
                                strokeWidth = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // 触摸检测层
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    touchPath = listOf(offset)
                                    updateGrid(offset, this.size.width.toFloat(), this.size.height.toFloat())
                                },
                                onDrag = { change, _ ->
                                    touchPath = touchPath + change.position
                                    updateGrid(change.position, this.size.width.toFloat(), this.size.height.toFloat())
                                },
                                onDragEnd = {
                                    touchPath = emptyList()
                                },
                                onDragCancel = {
                                    touchPath = emptyList()
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    touchPath = listOf(offset)
                                    updateGrid(offset, this.size.width.toFloat(), this.size.height.toFloat())
                                }
                            )
                        }
                )
            }
        }
    }

    // 判断该位置是否需要检测（参考MTK原厂代码）
    private fun isNeedCheck(y: Int, x: Int): Boolean {
        return (y == 0 // 顶部
                || y == (YCOUNT - 1) // 底部
                || y == (YCOUNT / 5) // Y方向1/5处
                || y == (YCOUNT * 2 / 5) // Y方向2/5处
                || y == (YCOUNT * 3 / 5) // Y方向3/5处
                || y == (YCOUNT * 4 / 5) // Y方向4/5处
                || x == 0 // 左边
                || x == (XCOUNT - 1) // 右边
                || x == (XCOUNT / 2) // X方向中间
                )
    }

    // 判断是否所有需要检测的格子都被绘制
    private fun isGridPassed(grid: Array<BooleanArray>): Boolean {
        for (i in 0 until YCOUNT) {
            for (j in 0 until XCOUNT) {
                if (isNeedCheck(i, j) && !grid[i][j]) {
                    return false
                }
            }
        }
        return true
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
