package com.mxj.mmitest.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 测试结果DAO
 * 提供测试结果的CRUD操作
 */
@Dao
interface TestResultDao {

    /**
     * 插入单条测试结果
     */
    @Insert
    suspend fun insert(result: TestResultEntity): Long

    /**
     * 批量插入测试结果
     */
    @Insert
    suspend fun insertAll(results: List<TestResultEntity>)

    /**
     * 更新测试结果
     */
    @Update
    suspend fun update(result: TestResultEntity)

    /**
     * 删除测试结果
     */
    @Delete
    suspend fun delete(result: TestResultEntity)

    /**
     * 根据ID获取测试结果
     */
    @Query("SELECT * FROM test_results WHERE id = :id")
    suspend fun getById(id: Long): TestResultEntity?

    /**
     * 根据会话ID获取所有测试结果
     */
    @Query("SELECT * FROM test_results WHERE testSessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getBySessionId(sessionId: String): List<TestResultEntity>

    /**
     * 根据会话ID获取测试结果（Flow版本）
     */
    @Query("SELECT * FROM test_results WHERE testSessionId = :sessionId ORDER BY timestamp ASC")
    fun getBySessionIdFlow(sessionId: String): Flow<List<TestResultEntity>>

    /**
     * 获取所有测试结果（按时间倒序）
     */
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<TestResultEntity>>

    /**
     * 获取所有测试结果
     */
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    suspend fun getAll(): List<TestResultEntity>

    /**
     * 根据设备ID获取测试结果
     */
    @Query("SELECT * FROM test_results WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    suspend fun getByDeviceId(deviceId: String): List<TestResultEntity>

    /**
     * 根据测试项ID获取历史测试结果
     */
    @Query("SELECT * FROM test_results WHERE testItemId = :testItemId ORDER BY timestamp DESC")
    suspend fun getByTestItemId(testItemId: Int): List<TestResultEntity>

    /**
     * 获取最近N条测试结果
     */
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<TestResultEntity>

    /**
     * 删除指定会话的所有测试结果
     */
    @Query("DELETE FROM test_results WHERE testSessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)

    /**
     * 删除所有测试结果
     */
    @Query("DELETE FROM test_results")
    suspend fun deleteAll()

    /**
     * 统计指定会话的测试结果数量
     */
    @Query("SELECT COUNT(*) FROM test_results WHERE testSessionId = :sessionId")
    suspend fun countBySessionId(sessionId: String): Int

    /**
     * 统计指定会话通过数量
     */
    @Query("SELECT COUNT(*) FROM test_results WHERE testSessionId = :sessionId AND result = 'PASS'")
    suspend fun countPassedBySessionId(sessionId: String): Int

    /**
     * 统计指定会话失败数量
     */
    @Query("SELECT COUNT(*) FROM test_results WHERE testSessionId = :sessionId AND result = 'FAIL'")
    suspend fun countFailedBySessionId(sessionId: String): Int
}

/**
 * 测试会话DAO
 * 提供测试会话的CRUD操作
 */
@Dao
interface TestSessionDao {

    /**
     * 插入测试会话
     */
    @Insert
    suspend fun insert(session: TestSessionEntity): Long

    /**
     * 更新测试会话
     */
    @Update
    suspend fun update(session: TestSessionEntity)

    /**
     * 删除测试会话
     */
    @Delete
    suspend fun delete(session: TestSessionEntity)

    /**
     * 根据会话ID获取测试会话
     */
    @Query("SELECT * FROM test_sessions WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): TestSessionEntity?

    /**
     * 根据会话ID获取测试会话（Flow版本）
     */
    @Query("SELECT * FROM test_sessions WHERE sessionId = :sessionId")
    fun getBySessionIdFlow(sessionId: String): Flow<TestSessionEntity?>

    /**
     * 获取所有测试会话（按时间倒序）
     */
    @Query("SELECT * FROM test_sessions ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<TestSessionEntity>>

    /**
     * 获取所有测试会话
     */
    @Query("SELECT * FROM test_sessions ORDER BY startTime DESC")
    suspend fun getAll(): List<TestSessionEntity>

    /**
     * 获取最近的测试会话
     */
    @Query("SELECT * FROM test_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<TestSessionEntity>

    /**
     * 删除指定会话
     */
    @Query("DELETE FROM test_sessions WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)

    /**
     * 删除所有会话
     */
    @Query("DELETE FROM test_sessions")
    suspend fun deleteAll()

    /**
     * 统计会话数量
     */
    @Query("SELECT COUNT(*) FROM test_sessions")
    suspend fun count(): Int
}
