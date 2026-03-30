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
import kotlinx.coroutines.delay

/**
 * PASS/FAIL 按钮组件
 * 防呆设计：FAIL按钮始终可点击，PASS按钮受passEnabled控制
 * 需要长按（1秒）才能触发，防止误触
 */
@Composable
fun TestResultButtons(
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier,
    requireLongPress: Boolean = true,
    passEnabled: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // PASS按钮（绿色）- 受passEnabled控制
        LongPressButton(
            text = "PASS",
            backgroundColor = if (passEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            onClick = onPass,
            requireLongPress = requireLongPress,
            enabled = passEnabled,
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(end = 8.dp)
        )

        // FAIL按钮（红色）- 始终可点击
        LongPressButton(
            text = "FAIL",
            backgroundColor = Color(0xFFF44336),
            onClick = onFail,
            requireLongPress = requireLongPress,
            enabled = true,
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
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isLongPressing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // 长按进度动画
    LaunchedEffect(isLongPressing) {
        if (isLongPressing && requireLongPress && enabled) {
            for (i in 1..10) {
                delay(100)
                progress = i / 10f
                if (!isLongPressing) {
                    progress = 0f
                    break
                }
            }
            if (isLongPressing) {
                onClick()
                isLongPressing = false
                progress = 0f
            }
        } else {
            progress = 0f
        }
    }

    Box(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.pointerInput(requireLongPress) {
                        if (requireLongPress) {
                            detectTapGestures(
                                onPress = {
                                    isLongPressing = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isLongPressing = false
                                    }
                                },
                                onTap = {
                                    if (!requireLongPress) {
                                        onClick()
                                    }
                                }
                            )
                        } else {
                            detectTapGestures(
                                onTap = { onClick() }
                            )
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxSize(),
            enabled = false,
            elevation = ButtonDefaults.buttonElevation(
                disabledElevation = 0.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium
                )
                if (requireLongPress && isLongPressing) {
                    Text(
                        text = "请长按...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 长按进度指示
        if (requireLongPress && progress > 0f) {
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
