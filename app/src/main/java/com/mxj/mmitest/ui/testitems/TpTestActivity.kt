package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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

    private val gridRows = 6
    private val gridCols = 8
    private val gapRatio = 0.125f // 间隙为方格边长的12.5%

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
                // 根据高度计算方格边长，确保填满垂直方向
                val cellSize = canvasHeight / (gridRows + (gridRows - 1) * gapRatio)
                val gapSize = cellSize * gapRatio

                val col = (offset.x / (cellSize + gapSize)).toInt()
                val row = (offset.y / (cellSize + gapSize)).toInt()

                if (row in 0 until gridRows && col in 0 until gridCols) {
                    filledCells = filledCells + Pair(row, col)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // 根据高度计算方格边长（正方形），确保填满垂直方向
                    val cellSize = canvasHeight / (gridRows + (gridRows - 1) * gapRatio)
                    val gapSize = cellSize * gapRatio

                    // 绘制所有方格
                    for (row in 0 until gridRows) {
                        for (col in 0 until gridCols) {
                            val cellFilled = filledCells.contains(Pair(row, col))
                            val rowComplete = completedRows.contains(row)
                            val colComplete = completedCols.contains(col)

                            val fillColor = when {
                                cellFilled && rowComplete -> Color(0xFF1B5E20)
                                cellFilled && colComplete -> Color(0xFF2E7D32)
                                cellFilled -> Color(0xFFFFC107)
                                else -> Color(0xFF424242)
                            }

                            val left = col * (cellSize + gapSize)
                            val top = row * (cellSize + gapSize)

                            drawRect(
                                color = fillColor,
                                topLeft = Offset(left, top),
                                size = Size(cellSize, cellSize)
                            )

                            if (!cellFilled) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.4f),
                                    topLeft = Offset(left, top),
                                    size = Size(cellSize, cellSize),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = with(density) { 1.dp.toPx() })
                                )
                            }
                        }
                    }

                    touchPosition?.let { pos ->
                        drawCircle(
                            color = Color.Cyan,
                            radius = cellSize * 0.3f,
                            center = pos
                        )
                    }
                }

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
