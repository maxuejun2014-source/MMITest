package com.mxj.mmitest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mxj.mmitest.domain.model.TestResultStatus

/**
 * 测试结果实体类
 * 用于Room数据库存储
 */
@Entity(tableName = "test_results")
@TypeConverters(TestResultConverters::class)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val testSessionId: String,      // 测试会话ID（用于关联同一轮测试的所有结果）
    val testItemId: Int,            // 测试项ID
    val testItemName: String,        // 测试项名称
    val result: String,             // 测试结果：PASS, FAIL, SKIPPED, TIMEOUT
    val timestamp: Long,             // 测试时间戳
    val deviceId: String,           // 设备ID
    val deviceModel: String,        // 设备型号
    val deviceManufacturer: String,  // 设备厂商
    val additionalInfo: String = "{}", // 额外信息（JSON格式）
    val timeoutReason: String? = null // 超时原因
)

/**
 * 测试会话实体类
 * 用于存储一轮测试的汇总信息
 */
@Entity(tableName = "test_sessions")
data class TestSessionEntity(
    @PrimaryKey
    val sessionId: String,          // 会话ID
    val deviceId: String,           // 设备ID
    val deviceModel: String,        // 设备型号
    val deviceManufacturer: String, // 设备厂商
    val startTime: Long,            // 开始时间
    val endTime: Long? = null,     // 结束时间
    val totalCount: Int,           // 总测试项数
    val passedCount: Int = 0,      // 通过数
    val failedCount: Int = 0,      // 失败数
    val skippedCount: Int = 0,     // 跳过数
    val operator: String = "工厂测试员", // 测试员
    val appVersion: String = "1.0.0" // APP版本
)

/**
 * 类型转换器
 */
class TestResultConverters {

    @TypeConverter
    fun fromTestResultStatus(status: TestResultStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTestResultStatus(value: String): TestResultStatus {
        return try {
            TestResultStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TestResultStatus.SKIPPED
        }
    }
}
