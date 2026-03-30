package com.mxj.mmitest.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * 二维码显示组件
 * 使用ZXing库生成并显示二维码
 *
 * @param content 二维码内容（字符串）
 * @param modifier 修饰符
 * @param size 二维码尺寸（dp）
 * @param foregroundColor 二维码前景色
 * @param backgroundColor 二维码背景色
 * @param showLoading 是否显示加载状态
 */
@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    foregroundColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    showLoading: Boolean = false
) {
    val bitmap = remember(content) {
        generateQrCodeBitmap(
            content = content,
            size = (size.value * 3).toInt(), // dp to px (approximate)
            foregroundColor = foregroundColor.toArgb(),
            backgroundColor = backgroundColor.toArgb()
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            showLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            bitmap != null -> {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "二维码",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Text(
                    text = "二维码生成失败",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 使用Bitmap生成二维码
 *
 * @param content 二维码内容
 * @param size 二维码尺寸（像素）
 * @param foregroundColor 前景色
 * @param backgroundColor 背景色
 * @return 生成的Bitmap，如果没有则返回null
 */
private fun generateQrCodeBitmap(
    content: String,
    size: Int,
    foregroundColor: Int,
    backgroundColor: Int
): Bitmap? {
    return try {
        if (content.isEmpty()) return null

        val qrCodeWriter = QRCodeWriter()
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 2
        )

        val bitMatrix = qrCodeWriter.encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) foregroundColor else backgroundColor
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 直接显示Bitmap的二维码组件
 *
 * @param bitmap 二维码Bitmap
 * @param modifier 修饰符
 * @param size 二维码尺寸（dp）
 * @param backgroundColor 背景色
 */
@Composable
fun QrCodeBitmapView(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    backgroundColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "二维码",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
