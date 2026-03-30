package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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

    private val gridRows = 6
    private val gridCols = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = TestRepository(this)
        setContent {
            var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }
            var filledCells by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
            var touchPosition by remember { mutableStateOf<Offset?>(null) }
            var testCompleted by remember { mutableStateOf(false) }

            val density = LocalDensity.current

            val completedRows = remember(filledCells) {
                (0 until gridRows).filter { row ->
                    (0 until gridCols).all { col -> filledCells.contains(Pair(row, col)) }
                }
            }
            val completedCols = remember(filledCells) {
                (0 until gridCols).filter { col ->
                    (0 until gridRows).all { row -> filledCells.contains(Pair(row, col)) }
                }
            }

            val allLinesCompleted = completedRows.size >= gridRows && completedCols.size >= gridCols

            LaunchedEffect(allLinesCompleted) {
                if (allLinesCompleted && !testCompleted) {
                    testCompleted = true
                    delay(500)
                    saveAndFinish(true)
                }
            }

            LaunchedEffect(Unit) {
                for (i in timeoutSeconds downTo 0) {
                    remainingSeconds = i
                    if (i > 0) delay(1000)
                }
                if (!testCompleted) {
                    saveAndFinish(false)
                }
            }

            // 触摸更新函数
            val updateCell: (Offset, Float, Float) -> Unit = { offset, canvasWidth, canvasHeight ->
                val headerHeight = with(density) { 90.dp.toPx() }
                val footerHeight = with(density) { 60.dp.toPx() }
                val gapRatio = 1.2f // 间隙为方格边长的1.2倍

                val availableWidth = canvasWidth
                val availableHeight = canvasHeight - headerHeight - footerHeight

                // 根据宽度计算方格边长（充满宽度方向）
                val cellSize = availableWidth / (gridCols + (gridCols - 1) * gapRatio)
                val gapSize = cellSize * gapRatio

                val col = (offset.x / (cellSize + gapSize)).toInt()
                val row = ((offset.y - headerHeight) / (cellSize + gapSize)).toInt()

                if (row in 0 until gridRows && col in 0 until gridCols) {
                    filledCells = filledCells + Pair(row, col)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // 绘制层
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val headerHeight = with(density) { 90.dp.toPx() }
                    val footerHeight = with(density) { 60.dp.toPx() }
                    val gapRatio = 1.2f

                    val availableWidth = canvasWidth
                    val availableHeight = canvasHeight - headerHeight - footerHeight

                    // 根据宽度计算方格边长
                    val cellSize = availableWidth / (gridCols + (gridCols - 1) * gapRatio)
                    val gapSize = cellSize * gapRatio

                    // 绘制所有方格
                    for (row in 0 until gridRows) {
                        for (col in 0 until gridCols) {
                            val cellFilled = filledCells.contains(Pair(row, col))
                            val rowComplete = completedRows.contains(row)

                            // 颜色：填充=黄色，完成行=深绿色，未填充=深灰色
                            val fillColor = when {
                                cellFilled && rowComplete -> Color(0xFF1B5E20) // 深绿
                                cellFilled -> Color(0xFFFFC107)               // 黄色
                                else -> Color(0xFF424242)                     // 深灰
                            }

                            val left = col * (cellSize + gapSize)
                            val top = headerHeight + row * (cellSize + gapSize)

                            drawRect(
                                color = fillColor,
                                topLeft = Offset(left, top),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }

                    // 绘制触摸点
                    touchPosition?.let { pos ->
                        drawCircle(
                            color = Color.Cyan,
                            radius = cellSize * 0.35f,
                            center = pos
                        )
                    }
                }

                // 触摸检测层
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    touchPosition = offset
                                    updateCell(offset, this.size.width.toFloat(), this.size.height.toFloat())
                                },
                                onDrag = { change, _ ->
                                    touchPosition = change.position
                                    updateCell(change.position, this.size.width.toFloat(), this.size.height.toFloat())
                                },
                                onDragEnd = { touchPosition = null },
                                onDragCancel = { touchPosition = null }
                            )
                        }
                )

                // 顶部标题和进度
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp)
                ) {
                    Text(
                        text = testName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "请在屏幕上划线填充所有方格",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text(
                            text = "横线: ${completedRows.size}/$gridRows",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completedRows.size >= gridRows) Color.Green else Color.Yellow,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = "竖线: ${completedCols.size}/$gridCols",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completedCols.size >= gridCols) Color.Green else Color.Yellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 底部状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    if (allLinesCompleted) {
                        Text(
                            text = "测试完成！",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        val remainingCells = gridRows * gridCols - filledCells.size
                        Text(
                            text = "剩余方格: $remainingCells",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
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
