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
 * 防呆设计：需要长按（1秒）才能触发，防止误触
 */
@Composable
fun TestResultButtons(
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier,
    requireLongPress: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // PASS按钮（绿色）
        LongPressButton(
            text = "PASS",
            backgroundColor = Color(0xFF4CAF50),
            onClick = onPass,
            requireLongPress = requireLongPress,
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(end = 8.dp)
        )

        // FAIL按钮（红色）
        LongPressButton(
            text = "FAIL",
            backgroundColor = Color(0xFFF44336),
            onClick = onFail,
            requireLongPress = requireLongPress,
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(start = 8.dp)
        )
    }
}

/**
 * 长按按钮组件
 */
@Composable
private fun LongPressButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    requireLongPress: Boolean,
    modifier: Modifier = Modifier
) {
    var isLongPressing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // 长按进度动画
    LaunchedEffect(isLongPressing) {
        if (isLongPressing && requireLongPress) {
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
            .pointerInput(requireLongPress) {
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
    ) {
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            ),
            modifier = Modifier.fillMaxSize(),
            enabled = false
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
