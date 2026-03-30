package com.mxj.mmitest.ui.testitems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
                val marginX = with(density) { 40.dp.toPx() }
                val marginY = with(density) { 120.dp.toPx() }
                val availableWidth = canvasWidth - marginX * 2
                val availableHeight = canvasHeight - marginY * 2 - with(density) { 80.dp.toPx() }
                val cellWidth = availableWidth / gridCols
                val cellHeight = availableHeight / gridRows

                val col = ((offset.x - marginX) / cellWidth).toInt()
                val row = ((offset.y - marginY) / cellHeight).toInt()

                if (row in 0 until gridRows && col in 0 until gridCols) {
                    filledCells = filledCells + Pair(row, col)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val marginX = with(density) { 40.dp.toPx() }
                    val marginY = with(density) { 120.dp.toPx() }
                    val availableWidth = canvasWidth - marginX * 2
                    val availableHeight = canvasHeight - marginY * 2 - with(density) { 80.dp.toPx() }
                    val cellWidth = availableWidth / gridCols
                    val cellHeight = availableHeight / gridRows

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

                            val left = marginX + col * cellWidth + with(density) { 3.dp.toPx() }
                            val top = marginY + row * cellHeight + with(density) { 3.dp.toPx() }
                            val right = left + cellWidth - with(density) { 6.dp.toPx() }
                            val bottom = top + cellHeight - with(density) { 6.dp.toPx() }

                            drawRect(
                                color = fillColor,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top)
                            )

                            if (!cellFilled) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.4f),
                                    topLeft = Offset(left, top),
                                    size = Size(right - left, bottom - top),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = with(density) { 1.dp.toPx() })
                                )
                            }
                        }
                    }

                    touchPosition?.let { pos ->
                        drawCircle(
                            color = Color.Cyan,
                            radius = with(density) { 12.dp.toPx() },
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 24.dp)
                ) {
                    Text(text = testName, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(text = "请在屏幕上划线填充所有方格", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "横线: ${completedRows.size}/$gridRows",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completedRows.size >= gridRows) Color.Green else Color.Yellow
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "竖线: ${completedCols.size}/$gridCols",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completedCols.size >= gridCols) Color.Green else Color.Yellow
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                ) {
                    if (allLinesCompleted) {
                        Text(text = "测试完成！", style = MaterialTheme.typography.headlineMedium, color = Color.Green)
                    } else {
                        val remainingCells = gridRows * gridCols - filledCells.size
                        Text(text = "剩余方格: $remainingCells", style = MaterialTheme.typography.bodyLarge, color = Color.White)
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
