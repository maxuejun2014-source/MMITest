package com.mxj.mmitest.domain.model

/**
 * 测试项数据模型
 */
data class TestItem(
    val id: Int,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val supported: Boolean = true,
    val timeoutSeconds: Int = 60
)

/**
 * 测试结果状态枚举
 */
enum class TestResultStatus {
    PASS,      // 通过
    FAIL,      // 失败
    SKIPPED,   // 跳过
    TIMEOUT    // 超时
}

/**
 * 测试结果数据模型
 */
data class TestResult(
    val id: Long = 0,
    val testItemId: Int,
    val testItemName: String,
    val result: TestResultStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String,
    val additionalInfo: Map<String, String> = emptyMap(),
    val timeoutReason: String? = null
)

/**
 * 设备信息数据模型
 */
data class DeviceInfo(
    val deviceId: String,
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val serialNumber: String
)

/**
 * 测试结果汇总（用于二维码生成）
 */
data class TestResultSummary(
    val deviceId: String,
    val model: String,
    val manufacturer: String,
    val testDate: String,
    val operator: String,
    val results: List<TestResultItem>,
    val summary: TestSummaryStats,
    val appVersion: String
)

/**
 * 单个测试结果项
 */
data class TestResultItem(
    val id: Int,
    val name: String,
    val status: String
)

/**
 * 测试汇总统计
 */
data class TestSummaryStats(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int
)
