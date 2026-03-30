package com.mxj.mmitest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 简单的饼图组件
 */
@Composable
fun SimplePieChart(
    passed: Int,
    failed: Int,
    skipped: Int,
    modifier: Modifier = Modifier
) {
    val total = passed + failed + skipped
    if (total == 0) return

    val passedAngle = (passed.toFloat() / total) * 360f
    val failedAngle = (failed.toFloat() / total) * 360f
    val skippedAngle = (skipped.toFloat() / total) * 360f

    val passedColor = Color(0xFF4CAF50)
    val failedColor = Color(0xFFF44336)
    val skippedColor = Color(0xFFFF9800)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 饼图
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val strokeWidth = 30.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f

                // 绘制通过部分
                if (passed > 0) {
                    drawArc(
                        color = passedColor,
                        startAngle = startAngle,
                        sweepAngle = passedAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += passedAngle
                }

                // 绘制失败部分
                if (failed > 0) {
                    drawArc(
                        color = failedColor,
                        startAngle = startAngle,
                        sweepAngle = failedAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += failedAngle
                }

                // 绘制跳过部分
                if (skipped > 0) {
                    drawArc(
                        color = skippedColor,
                        startAngle = startAngle,
                        sweepAngle = skippedAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }
            }

            // 中心文字
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(passed.toFloat() / total * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = passedColor
                )
                Text(
                    text = "通过率",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 图例
        Column {
            LegendItem(color = passedColor, label = "通过", count = passed)
            Spacer(modifier = Modifier.height(8.dp))
            LegendItem(color = failedColor, label = "失败", count = failed)
            Spacer(modifier = Modifier.height(8.dp))
            LegendItem(color = skippedColor, label = "跳过", count = skipped)
        }
    }
}

/**
 * 图例项
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 简单的柱状图组件
 */
@Composable
fun SimpleBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .width(24.dp)
                        .height((item.value.toFloat() / maxValue * 80).dp)
                ) {
                    drawRoundRect(
                        color = item.color,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 柱状图数据
 */
data class BarChartData(
    val label: String,
    val value: Int,
    val color: Color = Color(0xFF2196F3)
)
