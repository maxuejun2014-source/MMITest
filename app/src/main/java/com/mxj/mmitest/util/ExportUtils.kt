package com.mxj.mmitest.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mxj.mmitest.domain.model.TestResultSummary
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 导出格式枚举
 */
enum class ExportFormat {
    JSON,  // JSON格式
    CSV,   // CSV格式
    TXT    // 纯文本格式
}

/**
 * 导出结果
 */
sealed class ExportResult {
    data class Success(val filePath: String, val fileName: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * 结果导出工具类
 */
object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 导出测试结果
     * @param context 上下文
     * @param summary 测试结果汇总
     * @param format 导出格式
     * @return 导出结果
     */
    fun exportResult(context: Context, summary: TestResultSummary, format: ExportFormat): ExportResult {
        val fileName = generateFileName(summary, format)

        return try {
            val content = when (format) {
                ExportFormat.JSON -> toJson(summary)
                ExportFormat.CSV -> toCsv(summary)
                ExportFormat.TXT -> toTxt(summary)
            }

            val filePath = saveToFile(context, fileName, content, format)
            ExportResult.Success(filePath, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 生成文件名
     */
    private fun generateFileName(summary: TestResultSummary, format: ExportFormat): String {
        val timestamp = dateFormat.format(Date())
        val extension = when (format) {
            ExportFormat.JSON -> "json"
            ExportFormat.CSV -> "csv"
            ExportFormat.TXT -> "txt"
        }
        return "MMITest_${summary.model}_${timestamp}.$extension"
    }

    /**
     * 转换为JSON格式
     */
    private fun toJson(summary: TestResultSummary): String {
        return buildString {
            appendLine("{")
            appendLine("  \"deviceId\": \"${summary.deviceId}\",")
            appendLine("  \"model\": \"${summary.model}\",")
            appendLine("  \"manufacturer\": \"${summary.manufacturer}\",")
            appendLine("  \"testDate\": \"${summary.testDate}\",")
            appendLine("  \"operator\": \"${summary.operator}\",")
            appendLine("  \"appVersion\": \"${summary.appVersion}\",")
            appendLine("  \"summary\": {")
            appendLine("    \"total\": ${summary.summary.total},")
            appendLine("    \"passed\": ${summary.summary.passed},")
            appendLine("    \"failed\": ${summary.summary.failed},")
            appendLine("    \"skipped\": ${summary.summary.skipped}")
            appendLine("  },")
            appendLine("  \"results\": [")

            summary.results.forEachIndexed { index, item ->
                val comma = if (index < summary.results.size - 1) "," else ""
                appendLine("    {\"id\": ${item.id}, \"name\": \"${item.name}\", \"status\": \"${item.status}\"}$comma")
            }

            appendLine("  ]")
            appendLine("}")
        }
    }

    /**
     * 转换为CSV格式
     */
    private fun toCsv(summary: TestResultSummary): String {
        return buildString {
            // 头部信息
            appendLine("# MMI Test Results")
            appendLine("# Device: ${summary.model}")
            appendLine("# Manufacturer: ${summary.manufacturer}")
            appendLine("# Test Date: ${summary.testDate}")
            appendLine("# Operator: ${summary.operator}")
            appendLine("# App Version: ${summary.appVersion}")
            appendLine("# Summary: Total=${summary.summary.total}, Passed=${summary.summary.passed}, Failed=${summary.summary.failed}, Skipped=${summary.summary.skipped}")
            appendLine()

            // CSV头部
            appendLine("ID,Test Name,Status")

            // 数据行
            summary.results.forEach { item ->
                appendLine("${item.id},\"${item.name}\",${item.status}")
            }
        }
    }

    /**
     * 转换为纯文本格式
     */
    private fun toTxt(summary: TestResultSummary): String {
        return buildString {
            appendLine("===========================================")
            appendLine("       MMI Factory Test Report")
            appendLine("===========================================")
            appendLine()
            appendLine("Device Information:")
            appendLine("  Model: ${summary.model}")
            appendLine("  Manufacturer: ${summary.manufacturer}")
            appendLine("  Device ID: ${summary.deviceId}")
            appendLine()
            appendLine("Test Information:")
            appendLine("  Test Date: ${summary.testDate}")
            appendLine("  Operator: ${summary.operator}")
            appendLine("  App Version: ${summary.appVersion}")
            appendLine()
            appendLine("Test Summary:")
            appendLine("  Total: ${summary.summary.total}")
            appendLine("  Passed: ${summary.summary.passed}")
            appendLine("  Failed: ${summary.summary.failed}")
            appendLine("  Skipped: ${summary.summary.skipped}")
            appendLine()
            appendLine("Detailed Results:")
            appendLine("-------------------------------------------")

            summary.results.forEach { item ->
                val statusIcon = when (item.status) {
                    "PASS" -> "[PASS]"
                    "FAIL" -> "[FAIL]"
                    else -> "[SKIP]"
                }
                appendLine("${item.id}. $statusIcon ${item.name}")
            }

            appendLine("-------------------------------------------")
            appendLine()
            appendLine("Generated: ${displayDateFormat.format(Date())}")
        }
    }

    /**
     * 保存到文件
     */
    private fun saveToFile(context: Context, fileName: String, content: String, format: ExportFormat): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            saveToMediaStore(context, fileName, content, format)
        } else {
            // 旧版本直接保存到外部存储
            saveToExternalStorage(fileName, content)
        }
    }

    /**
     * 保存到MediaStore（Android 10+）
     */
    private fun saveToMediaStore(context: Context, fileName: String, content: String, format: ExportFormat): String {
        val mimeType = when (format) {
            ExportFormat.JSON -> "application/json"
            ExportFormat.CSV -> "text/csv"
            ExportFormat.TXT -> "text/plain"
        }

        val directory = when (format) {
            ExportFormat.JSON -> Environment.DIRECTORY_DOCUMENTS
            ExportFormat.CSV -> Environment.DIRECTORY_DOCUMENTS
            ExportFormat.TXT -> Environment.DIRECTORY_DOCUMENTS
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, directory + "/MMITest")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: throw Exception("无法创建文件")

        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray(Charsets.UTF_8))
        } ?: throw Exception("无法打开输出流")

        return "${Environment.DIRECTORY_DOCUMENTS}/MMITest/$fileName"
    }

    /**
     * 保存到外部存储（Android 9及以下）
     */
    private fun saveToExternalStorage(fileName: String, content: String): String {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MMITest")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray(Charsets.UTF_8))
        }

        return file.absolutePath
    }
}
