package com.mxj.mmitest.ui.result

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mxj.mmitest.data.local.TestSessionEntity
import com.mxj.mmitest.data.repository.TestRepository
import com.mxj.mmitest.domain.model.TestResultSummary
import com.mxj.mmitest.util.ExportFormat
import com.mxj.mmitest.util.ExportResult
import com.mxj.mmitest.util.ExportUtils
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
    STATISTICS, // 统计图表
    QR_CODE     // 二维码
}

/**
 * 筛选条件
 */
data class FilterCriteria(
    val deviceModel: String = "",
    val resultStatus: ResultStatusFilter = ResultStatusFilter.ALL,
    val dateFrom: Long? = null,
    val dateTo: Long? = null
)

/**
 * 结果状态筛选
 */
enum class ResultStatusFilter {
    ALL,        // 全部
    PASSED,     // 全部通过
    FAILED,     // 有失败
    COMPLETED   // 已完成
}

/**
 * 导出状态
 */
sealed class ExportState {
    data object Idle : ExportState()
    data object Exporting : ExportState()
    data class Success(val filePath: String, val fileName: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

/**
 * 测试统计数据
 */
data class TestStatistics(
    val totalSessions: Int = 0,
    val totalTests: Int = 0,
    val passedTests: Int = 0,
    val failedTests: Int = 0,
    val skippedTests: Int = 0,
    val passRate: Float = 0f,
    val failRate: Float = 0f,
    val deviceStats: Map<String, Int> = emptyMap(), // 设备型号 -> 测试次数
    val recentTrends: List<DailyStats> = emptyList() // 最近趋势
)

/**
 * 每日测试统计
 */
data class DailyStats(
    val date: String,
    val passed: Int,
    val failed: Int,
    val total: Int
)

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

    // 筛选条件
    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    val filterCriteria: StateFlow<FilterCriteria> = _filterCriteria.asStateFlow()

    // 筛选后的会话列表
    private val _filteredSessions = MutableStateFlow<List<TestSessionEntity>>(emptyList())
    val filteredSessions: StateFlow<List<TestSessionEntity>> = _filteredSessions.asStateFlow()

    // 是否有筛选条件
    private val _hasActiveFilter = MutableStateFlow(false)
    val hasActiveFilter: StateFlow<Boolean> = _hasActiveFilter.asStateFlow()

    // 可用的设备型号列表
    private val _availableDeviceModels = MutableStateFlow<List<String>>(emptyList())
    val availableDeviceModels: StateFlow<List<String>> = _availableDeviceModels.asStateFlow()

    // 导出状态
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // 统计数据（基于筛选后的数据）
    private val _statistics = MutableStateFlow<TestStatistics?>(null)
    val statistics: StateFlow<TestStatistics?> = _statistics.asStateFlow()

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
                // 更新可用设备型号列表
                _availableDeviceModels.value = sessionList
                    .map { it.deviceModel }
                    .distinct()
                    .sorted()
                // 应用筛选
                applyFilter(sessionList)
                // 计算统计
                calculateStatistics(_filteredSessions.value)
                _isLoading.value = false
            }
        }
    }

    /**
     * 计算统计数据
     */
    private fun calculateStatistics(sessions: List<TestSessionEntity>) {
        if (sessions.isEmpty()) {
            _statistics.value = null
            return
        }

        val totalTests = sessions.sumOf { it.totalCount }
        val passedTests = sessions.sumOf { it.passedCount }
        val failedTests = sessions.sumOf { it.failedCount }
        val skippedTests = sessions.sumOf { it.skippedCount }

        val passRate = if (totalTests > 0) passedTests.toFloat() / totalTests else 0f
        val failRate = if (totalTests > 0) failedTests.toFloat() / totalTests else 0f

        // 按设备统计
        val deviceStats = sessions
            .groupBy { it.deviceModel }
            .mapValues { it.value.size }

        // 最近趋势（按天分组）
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val recentTrends = sessions
            .groupBy { dateFormat.format(java.util.Date(it.startTime)) }
            .map { (date, daySessions) ->
                DailyStats(
                    date = date,
                    passed = daySessions.sumOf { it.passedCount },
                    failed = daySessions.sumOf { it.failedCount },
                    total = daySessions.sumOf { it.totalCount }
                )
            }
            .sortedBy { it.date }
            .takeLast(7) // 最近7天

        _statistics.value = TestStatistics(
            totalSessions = sessions.size,
            totalTests = totalTests,
            passedTests = passedTests,
            failedTests = failedTests,
            skippedTests = skippedTests,
            passRate = passRate,
            failRate = failRate,
            deviceStats = deviceStats,
            recentTrends = recentTrends
        )
    }

    /**
     * 更新筛选条件并重新计算统计
     */
    fun updateFilter(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
        applyFilter(_sessions.value)
        calculateStatistics(_filteredSessions.value)
        _hasActiveFilter.value = criteria.deviceModel.isNotEmpty() ||
                criteria.resultStatus != ResultStatusFilter.ALL ||
                criteria.dateFrom != null ||
                criteria.dateTo != null
    }

    /**
     * 清除筛选条件
     */
    fun clearFilter() {
        _filterCriteria.value = FilterCriteria()
        _filteredSessions.value = _sessions.value
        _hasActiveFilter.value = false
        calculateStatistics(_sessions.value)
    }

    /**
     * 应用筛选
     */
    private fun applyFilter(sessions: List<TestSessionEntity>) {
        val criteria = _filterCriteria.value
        _filteredSessions.value = sessions.filter { session ->
            // 按设备型号筛选
            val matchesModel = criteria.deviceModel.isEmpty() ||
                    session.deviceModel.contains(criteria.deviceModel, ignoreCase = true)

            // 按结果状态筛选
            val matchesStatus = when (criteria.resultStatus) {
                ResultStatusFilter.ALL -> true
                ResultStatusFilter.PASSED -> session.failedCount == 0 && session.skippedCount == 0
                ResultStatusFilter.FAILED -> session.failedCount > 0
                ResultStatusFilter.COMPLETED -> session.endTime != null
            }

            // 按日期范围筛选
            val matchesDateFrom = criteria.dateFrom == null || session.startTime >= criteria.dateFrom
            val matchesDateTo = criteria.dateTo == null || session.startTime <= criteria.dateTo

            matchesModel && matchesStatus && matchesDateFrom && matchesDateTo
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

    /**
     * 导出测试结果
     */
    fun exportResult(format: ExportFormat) {
        val summary = _selectedSummary.value ?: return

        viewModelScope.launch {
            _exportState.value = ExportState.Exporting

            val result = withContext(Dispatchers.IO) {
                ExportUtils.exportResult(getApplication(), summary, format)
            }

            _exportState.value = when (result) {
                is ExportResult.Success -> ExportState.Success(result.filePath, result.fileName)
                is ExportResult.Error -> ExportState.Error(result.message)
            }
        }
    }

    /**
     * 重置导出状态
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
