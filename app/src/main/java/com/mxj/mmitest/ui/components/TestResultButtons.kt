package com.mxj.mmitest.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mxj.mmitest.config.TestConfig
import kotlinx.coroutines.delay

/**
 * PASS/FAIL 按钮组件
 * 防呆设计：FAIL按钮始终可点击（单击即触发），PASS按钮受passEnabled控制
 * 长按时间可在TestConfig.TestSettings中配置
 */
@Composable
fun TestResultButtons(
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier,
    passEnabled: Boolean = false
) {
    // 从配置读取长按设置
    val requireLongPress = TestConfig.TestSettings.PASS_REQUIRE_LONG_PRESS
    val longPressDurationMs = TestConfig.TestSettings.PASS_LONG_PRESS_DURATION_MS

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // PASS按钮（绿色）- 受passEnabled控制，长按时间可配置
        LongPressButton(
            text = "PASS",
            backgroundColor = if (passEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            onClick = onPass,
            requireLongPress = requireLongPress,
            longPressDurationMs = longPressDurationMs,
            enabled = passEnabled,
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(end = 8.dp)
        )

        // FAIL按钮（红色）- 单击即触发，始终可点击
        SimpleClickButton(
            text = "FAIL",
            backgroundColor = Color(0xFFF44336),
            onClick = onFail,
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(start = 8.dp)
        )
    }
}

/**
 * 长按按钮组件
 * enabled=false时按钮灰显且不可点击
 */
@Composable
private fun LongPressButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    requireLongPress: Boolean,
    longPressDurationMs: Long = 500L,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isLongPressing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // 计算每步进度的时间（总时长分成10步）
    val stepDelay = longPressDurationMs / 10

    // 长按进度动画
    LaunchedEffect(isLongPressing, enabled, requireLongPress) {
        if (isLongPressing && requireLongPress && enabled) {
            for (i in 1..10) {
                delay(stepDelay)
                progress = i / 10f
                if (!isLongPressing) {
                    progress = 0f
                    break
                }
            }
            if (isLongPressing) {
                onClick()
            }
        }
        isLongPressing = false
        progress = 0f
    }

    Box(modifier = modifier) {
        Button(
            onClick = {
                if (enabled) {
                    if (!requireLongPress) {
                        onClick()
                    } else if (isLongPressing && progress >= 1f) {
                        // Long press completed, onClick already called
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium
                )
                if (requireLongPress && enabled && isLongPressing) {
                    Text(
                        text = "请长按...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 长按检测覆盖层
        if (enabled && requireLongPress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(enabled, requireLongPress) {
                        detectTapGestures(
                            onPress = {
                                isLongPressing = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isLongPressing = false
                                }
                            }
                        )
                    }
            )
        }

        // 长按进度指示
        if (requireLongPress && enabled && progress > 0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

/**
 * 简单单击按钮组件（无长按要求）
 * FAIL按钮使用此组件 - 单击即触发
 */
@Composable
private fun SimpleClickButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        modifier = modifier,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
