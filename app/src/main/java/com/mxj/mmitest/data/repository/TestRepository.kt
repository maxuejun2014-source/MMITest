package com.mxj.mmitest.data.repository

import android.content.Context
import com.mxj.mmitest.data.local.AppDatabase
import com.mxj.mmitest.data.local.TestResultEntity
import com.mxj.mmitest.data.local.TestSessionEntity
import com.mxj.mmitest.domain.model.TestResult
import com.mxj.mmitest.domain.model.TestResultStatus
import com.mxj.mmitest.domain.model.TestResultSummary
import com.mxj.mmitest.domain.model.TestResultItem
import com.mxj.mmitest.domain.model.TestSummaryStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * 测试结果仓库
 * 负责测试结果的CRUD操作和数据转换
 */
class TestRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val testResultDao = database.testResultDao()
    private val testSessionDao = database.testSessionDao()

    /**
     * 创建新的测试会话
     */
    suspend fun createSession(
        deviceId: String,
        deviceModel: String,
        deviceManufacturer: String,
        totalCount: Int
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val session = TestSessionEntity(
            sessionId = sessionId,
            deviceId = deviceId,
            deviceModel = deviceModel,
            deviceManufacturer = deviceManufacturer,
            startTime = System.currentTimeMillis(),
            totalCount = totalCount
        )
        testSessionDao.insert(session)
        return sessionId
    }

    /**
     * 更新测试会话汇总信息
     */
    suspend fun updateSessionSummary(
        sessionId: String,
        passedCount: Int,
        failedCount: Int,
        skippedCount: Int
    ) {
        val session = testSessionDao.getBySessionId(sessionId) ?: return
        val updatedSession = session.copy(
            endTime = System.currentTimeMillis(),
            passedCount = passedCount,
            failedCount = failedCount,
            skippedCount = skippedCount
        )
        testSessionDao.update(updatedSession)
    }

    /**
     * 保存测试结果
     */
    suspend fun saveTestResult(result: TestResult, sessionId: String): Long {
        val entity = TestResultEntity(
            testSessionId = sessionId,
            testItemId = result.testItemId,
            testItemName = result.testItemName,
            result = result.result.name,
            timestamp = result.timestamp,
            deviceId = result.deviceId,
            deviceModel = android.os.Build.MODEL,
            deviceManufacturer = android.os.Build.MANUFACTURER,
            timeoutReason = result.timeoutReason
        )
        return testResultDao.insert(entity)
    }

    /**
     * 批量保存测试结果
     */
    suspend fun saveTestResults(results: List<TestResult>, sessionId: String) {
        val entities = results.map { result ->
            TestResultEntity(
                testSessionId = sessionId,
                testItemId = result.testItemId,
                testItemName = result.testItemName,
                result = result.result.name,
                timestamp = result.timestamp,
                deviceId = result.deviceId,
                deviceModel = android.os.Build.MODEL,
                deviceManufacturer = android.os.Build.MANUFACTURER,
                timeoutReason = result.timeoutReason
            )
        }
        testResultDao.insertAll(entities)
    }

    /**
     * 获取会话的测试结果
     */
    suspend fun getSessionResults(sessionId: String): List<TestResult> {
        return testResultDao.getBySessionId(sessionId).map { it.toTestResult() }
    }

    /**
     * 获取会话的测试结果（Flow版本）
     */
    fun getSessionResultsFlow(sessionId: String): Flow<List<TestResult>> {
        return testResultDao.getBySessionIdFlow(sessionId).map { entities ->
            entities.map { it.toTestResult() }
        }
    }

    /**
     * 获取所有测试会话
     */
    fun getAllSessions(): Flow<List<TestSessionEntity>> {
        return testSessionDao.getAllFlow()
    }

    /**
     * 获取最近的测试会话
     */
    suspend fun getRecentSessions(limit: Int = 10): List<TestSessionEntity> {
        return testSessionDao.getRecent(limit)
    }

    /**
     * 根据会话ID获取会话信息
     */
    suspend fun getSession(sessionId: String): TestSessionEntity? {
        return testSessionDao.getBySessionId(sessionId)
    }

    /**
     * 获取测试结果汇总
     */
    suspend fun getResultSummary(sessionId: String): TestResultSummary? {
        val session = testSessionDao.getBySessionId(sessionId) ?: return null
        val results = testResultDao.getBySessionId(sessionId)

        return TestResultSummary(
            deviceId = session.deviceId,
            model = session.deviceModel,
            manufacturer = session.deviceManufacturer,
            testDate = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date(session.startTime)),
            operator = session.operator,
            results = results.map {
                TestResultItem(
                    id = it.testItemId,
                    name = it.testItemName,
                    status = it.result
                )
            },
            summary = TestSummaryStats(
                total = results.size,
                passed = results.count { r -> r.result == "PASS" },
                failed = results.count { r -> r.result == "FAIL" },
                skipped = results.count { r -> r.result in listOf("SKIPPED", "TIMEOUT") }
            ),
            appVersion = session.appVersion
        )
    }

    /**
     * 删除指定会话及其所有结果
     */
    suspend fun deleteSession(sessionId: String) {
        testResultDao.deleteBySessionId(sessionId)
        testSessionDao.deleteBySessionId(sessionId)
    }

    /**
     * 删除所有数据
     */
    suspend fun deleteAll() {
        testResultDao.deleteAll()
        testSessionDao.deleteAll()
    }

    /**
     * 将Entity转换为Domain模型
     */
    private fun TestResultEntity.toTestResult(): TestResult {
        return TestResult(
            id = id,
            testItemId = testItemId,
            testItemName = testItemName,
            result = try {
                TestResultStatus.valueOf(result)
            } catch (e: IllegalArgumentException) {
                TestResultStatus.SKIPPED
            },
            timestamp = timestamp,
            deviceId = deviceId,
            timeoutReason = timeoutReason
        )
    }

    /**
     * 保存单项测试结果（不创建会话）
     * 使用特殊的sessionId "single_test" 来标识单项测试
     */
    suspend fun saveSingleTestResult(
        testItemId: Int,
        testItemName: String,
        passed: Boolean,
        deviceId: String
    ): Long {
        val resultEntity = TestResultEntity(
            testSessionId = "single_test",
            testItemId = testItemId,
            testItemName = testItemName,
            result = if (passed) "PASS" else "FAIL",
            timestamp = System.currentTimeMillis(),
            deviceId = deviceId,
            deviceModel = android.os.Build.MODEL,
            deviceManufacturer = android.os.Build.MANUFACTURER
        )
        return testResultDao.insert(resultEntity)
    }

    /**
     * 获取所有测试项的最新结果（按测试项ID分组）
     */
    suspend fun getLatestResultsForAllTestItems(): Map<Int, TestResultEntity> {
        val allResults = testResultDao.getAll()
        return allResults
            .groupBy { it.testItemId }
            .mapValues { it.value.maxByOrNull { r -> r.timestamp }!! }
    }

    /**
     * 获取指定测试项的最新结果
     */
    suspend fun getLatestResultForTestItem(testItemId: Int): TestResultEntity? {
        return testResultDao.getByTestItemId(testItemId).firstOrNull()
    }
}
