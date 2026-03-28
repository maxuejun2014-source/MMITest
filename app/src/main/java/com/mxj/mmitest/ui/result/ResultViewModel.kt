package com.mxj.mmitest.ui.result

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mxj.mmitest.data.local.TestSessionEntity
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.domain.model.TestResultSummary
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 结果Tab页枚举
 */
enum class ResultTab {
    HISTORY,    // 历史记录
    QR_CODE     // 二维码
}

/**
 * 测试结果ViewModel
 * 负责管理测试结果、历史记录和二维码生成
 */
class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TestRepository(application)

    // 当前Tab
    private val _currentTab = MutableStateFlow(ResultTab.HISTORY)
    val currentTab: StateFlow<ResultTab> = _currentTab.asStateFlow()

    // 历史记录列表
    private val _sessions = MutableStateFlow<List<TestSessionEntity>>(emptyList())
    val sessions: StateFlow<List<TestSessionEntity>> = _sessions.asStateFlow()

    // 选中的会话ID
    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    // 选中的测试汇总
    private val _selectedSummary = MutableStateFlow<TestResultSummary?>(null)
    val selectedSummary: StateFlow<TestResultSummary?> = _selectedSummary.asStateFlow()

    // 二维码Bitmap
    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSessions()
    }

    /**
     * 加载历史会话列表
     */
    fun loadSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllSessions().collect { sessionList ->
                _sessions.value = sessionList
                _isLoading.value = false
            }
        }
    }

    /**
     * 选择会话查看详情
     */
    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
        viewModelScope.launch {
            val summary = repository.getResultSummary(sessionId)
            _selectedSummary.value = summary
            summary?.let { generateQrCode(it) }
        }
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _selectedSessionId.value = null
        _selectedSummary.value = null
        _qrCodeBitmap.value = null
    }

    /**
     * 切换Tab
     */
    fun switchTab(tab: ResultTab) {
        _currentTab.value = tab
    }

    /**
     * 删除指定会话
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) {
                clearSelection()
            }
        }
    }

    /**
     * 删除所有历史记录
     */
    fun deleteAllSessions() {
        viewModelScope.launch {
            repository.deleteAll()
            clearSelection()
        }
    }

    /**
     * 生成二维码
     * 将测试结果汇总转换为JSON并生成二维码
     */
    private suspend fun generateQrCode(summary: TestResultSummary) {
        withContext(Dispatchers.Default) {
            try {
                // 构建JSON数据
                val jsonObject = JSONObject().apply {
                    put("deviceId", summary.deviceId)
                    put("model", summary.model)
                    put("manufacturer", summary.manufacturer)
                    put("testDate", summary.testDate)
                    put("operator", summary.operator)
                    put("appVersion", summary.appVersion)
                    put("total", summary.summary.total)
                    put("passed", summary.summary.passed)
                    put("failed", summary.summary.failed)
                    put("skipped", summary.summary.skipped)

                    val resultsArray = org.json.JSONArray()
                    summary.results.forEach { item ->
                        resultsArray.put(JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("status", item.status)
                        })
                    }
                    put("results", resultsArray)
                }

                val jsonString = jsonObject.toString()

                // 生成二维码
                val qrCodeWriter = QRCodeWriter()
                val hints = mapOf(
                    EncodeHintType.CHARACTER_SET to "UTF-8",
                    EncodeHintType.MARGIN to 2
                )

                val size = 512
                val bitMatrix = qrCodeWriter.encode(
                    jsonString,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
                )

                // 转换为Bitmap
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        bitmap.setPixel(
                            x, y,
                            if (bitMatrix[x, y]) android.graphics.Color.BLACK
                            else android.graphics.Color.WHITE
                        )
                    }
                }

                _qrCodeBitmap.value = bitmap

            } catch (e: Exception) {
                e.printStackTrace()
                _qrCodeBitmap.value = null
            }
        }
    }

    /**
     * 获取测试结果文字描述
     */
    fun getResultText(): String {
        val summary = _selectedSummary.value ?: return "无测试结果"
        return buildString {
            appendLine("========== 测试结果 ==========")
            appendLine("设备: ${summary.model}")
            appendLine("厂商: ${summary.manufacturer}")
            appendLine("测试时间: ${summary.testDate}")
            appendLine("测试员: ${summary.operator}")
            appendLine()
            appendLine("汇总: 共${summary.summary.total}项")
            appendLine("  通过: ${summary.summary.passed}")
            appendLine("  失败: ${summary.summary.failed}")
            appendLine("  跳过: ${summary.summary.skipped}")
            appendLine()
            appendLine("详细结果:")
            summary.results.forEach { item ->
                val statusIcon = when (item.status) {
                    "PASS" -> "✓"
                    "FAIL" -> "✗"
                    else -> "⊘"
                }
                appendLine("  $statusIcon ${item.name}: ${item.status}")
            }
        }
    }
}
