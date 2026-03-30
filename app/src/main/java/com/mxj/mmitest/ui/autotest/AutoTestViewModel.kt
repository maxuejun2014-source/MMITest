package com.mxj.mmitest.ui.autotest

import android.app.Application
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mxj.mmitest.config.TestConfig
import com.mxj.mmitest.domain.model.TestResult
import com.mxj.mmitest.domain.model.TestResultStatus
import com.mxj.mmitest.domain.model.TestResultSummary
import com.mxj.mmitest.domain.model.TestResultItem
import com.mxj.mmitest.domain.model.TestSummaryStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 自动测试状态
 */
enum class AutoTestState {
    IDLE,           // 空闲状态
    RUNNING,        // 测试中
    PAUSED,         // 暂停
    COMPLETED,      // 完成
    STOPPED         // 手动停止
}

/**
 * 当前测试项的状态
 */
data class CurrentTestState(
    val testIndex: Int = 0,
    val testName: String = "",
    val isRunning: Boolean = false,
    val isWaitingForResult: Boolean = false
)

/**
 * 自动测试ViewModel
 * 负责管理测试队列、执行逻辑和结果收集
 */
class AutoTestViewModel(application: Application) : AndroidViewModel(application) {

    // 测试状态
    private val _testState = MutableStateFlow(AutoTestState.IDLE)
    val testState: StateFlow<AutoTestState> = _testState.asStateFlow()

    // 当前测试项状态
    private val _currentTestState = MutableStateFlow(CurrentTestState())
    val currentTestState: StateFlow<CurrentTestState> = _currentTestState.asStateFlow()

    // 测试队列
    private val _testQueue = MutableStateFlow<List<TestConfig.TestItem>>(emptyList())
    val testQueue: StateFlow<List<TestConfig.TestItem>> = _testQueue.asStateFlow()

    // 自定义超时时间映射
    private var customTimeouts: Map<Int, Int> = emptyMap()

    // 测试结果列表
    private val _testResults = MutableStateFlow<List<TestResult>>(emptyList())
    val testResults: StateFlow<List<TestResult>> = _testResults.asStateFlow()

    // 进度
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    // 日志
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    // 测试结果汇总
    private val _resultSummary = MutableStateFlow<TestResultSummary?>(null)
    val resultSummary: StateFlow<TestResultSummary?> = _resultSummary.asStateFlow()

    // 设备ID
    private val deviceId: String = android.os.Build.MODEL + "_" + android.os.Build.SERIAL

    // 待处理的Activity结果回调
    var onActivityResult: ((TestConfig.TestItem, Boolean) -> Unit)? = null

    init {
        // 初始化测试队列
        _testQueue.value = TestConfig.getEnabledTestItems()
        addLog("初始化完成，待测试项数量: ${_testQueue.value.size}")
    }

    /**
     * 更新测试队列和超时配置
     */
    fun updateTestQueue(testItems: List<TestConfig.TestItem>, customTimeouts: Map<Int, Int>) {
        _testQueue.value = testItems
        this.customTimeouts = customTimeouts
        addLog("测试队列已更新，共 ${testItems.size} 项")
    }

    /**
     * 获取测试项的超时时间（优先使用自定义值）
     */
    private fun getTimeoutSeconds(testItem: TestConfig.TestItem): Int {
        return customTimeouts[testItem.id] ?: testItem.timeoutSeconds
    }

    /**
     * 开始自动测试
     */
    fun startTest() {
        if (_testState.value == AutoTestState.RUNNING) {
            addLog("测试正在运行中...")
            return
        }

        viewModelScope.launch {
            _testState.value = AutoTestState.RUNNING
            _testResults.value = emptyList()
            _progress.value = 0
            _resultSummary.value = null
            addLog("========== 开始自动测试 ==========")

            val queue = _testQueue.value
            for ((index, testItem) in queue.withIndex()) {
                if (_testState.value != AutoTestState.RUNNING) {
                    break
                }

                _currentTestState.value = CurrentTestState(
                    testIndex = index + 1,
                    testName = testItem.name,
                    isRunning = true,
                    isWaitingForResult = false
                )

                addLog("[$index/${queue.size}] 正在测试: ${testItem.name}")

                // 执行测试项
                val result = executeTestItem(testItem)

                _testResults.value = _testResults.value + result
                _progress.value = ((index + 1) * 100) / queue.size

                // 根据结果记录日志
                when (result.result) {
                    TestResultStatus.PASS -> addLog("✓ ${testItem.name} - 通过")
                    TestResultStatus.FAIL -> addLog("✗ ${testItem.name} - 失败")
                    TestResultStatus.SKIPPED -> addLog("⊘ ${testItem.name} - 跳过")
                    TestResultStatus.TIMEOUT -> addLog("⏱ ${testItem.name} - 超时")
                }

                // 失败后继续执行下一项（不中断）
                delay(500) // 短暂延迟让用户看清状态
            }

            if (_testState.value == AutoTestState.RUNNING) {
                _testState.value = AutoTestState.COMPLETED
                generateSummary()
                addLog("========== 测试完成 ==========")
            }

            _currentTestState.value = CurrentTestState(isRunning = false)
        }
    }

    /**
     * 执行单个测试项
     * 通过启动Activity并等待结果
     */
    private suspend fun executeTestItem(testItem: TestConfig.TestItem): TestResult {
        val startTime = System.currentTimeMillis()

        // 标记为等待结果状态
        _currentTestState.value = _currentTestState.value.copy(isWaitingForResult = true)

        // 创建结果回调
        var testPassed = false
        onActivityResult = { item, passed ->
            if (item.id == testItem.id) {
                testPassed = passed
            }
        }

        // 启动测试Activity
        val context = getApplication<Application>()
        val intent = Intent(context, testItem.activityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        // 等待测试完成（通过超时机制）
        val timeoutMs = getTimeoutSeconds(testItem) * 1000L
        val pollInterval = 500L
        var elapsed = 0L

        while (elapsed < timeoutMs) {
            delay(pollInterval)
            elapsed += pollInterval

            // 检查是否已收到结果
            if (testPassed) {
                onActivityResult = null
                return TestResult(
                    testItemId = testItem.id,
                    testItemName = testItem.name,
                    result = TestResultStatus.PASS,
                    deviceId = deviceId,
                    timestamp = startTime
                )
            }
        }

        // 超时
        onActivityResult = null
        return TestResult(
            testItemId = testItem.id,
            testItemName = testItem.name,
            result = TestResultStatus.TIMEOUT,
            deviceId = deviceId,
            timestamp = startTime,
            timeoutReason = "测试超时（${testItem.timeoutSeconds}秒）"
        )
    }

    /**
     * 暂停测试
     */
    fun pauseTest() {
        if (_testState.value == AutoTestState.RUNNING) {
            _testState.value = AutoTestState.PAUSED
            addLog("测试已暂停")
        }
    }

    /**
     * 继续测试
     */
    fun resumeTest() {
        if (_testState.value == AutoTestState.PAUSED) {
            _testState.value = AutoTestState.RUNNING
            addLog("测试继续")
            // 继续执行剩余测试项
            continueTest()
        }
    }

    /**
     * 继续执行剩余测试项
     */
    private fun continueTest() {
        viewModelScope.launch {
            val queue = _testQueue.value
            val completedCount = _testResults.value.size

            for ((index, testItem) in queue.withIndex()) {
                if (index < completedCount) continue
                if (_testState.value != AutoTestState.RUNNING) break

                _currentTestState.value = CurrentTestState(
                    testIndex = index + 1,
                    testName = testItem.name,
                    isRunning = true,
                    isWaitingForResult = false
                )

                addLog("[$index/${queue.size}] 正在测试: ${testItem.name}")

                val result = executeTestItem(testItem)
                _testResults.value = _testResults.value + result
                _progress.value = ((index + 1) * 100) / queue.size

                when (result.result) {
                    TestResultStatus.PASS -> addLog("✓ ${testItem.name} - 通过")
                    TestResultStatus.FAIL -> addLog("✗ ${testItem.name} - 失败")
                    TestResultStatus.SKIPPED -> addLog("⊘ ${testItem.name} - 跳过")
                    TestResultStatus.TIMEOUT -> addLog("⏱ ${testItem.name} - 超时")
                }

                delay(500)
            }

            if (_testState.value == AutoTestState.RUNNING) {
                _testState.value = AutoTestState.COMPLETED
                generateSummary()
                addLog("========== 测试完成 ==========")
            }

            _currentTestState.value = CurrentTestState(isRunning = false)
        }
    }

    /**
     * 停止测试
     */
    fun stopTest() {
        _testState.value = AutoTestState.STOPPED
        addLog("测试已手动停止")
        _currentTestState.value = CurrentTestState(isRunning = false)

        // 将未完成的测试项标记为跳过
        val completedIds = _testResults.value.map { it.testItemId }.toSet()
        val queue = _testQueue.value
        val skippedResults = queue
            .filter { it.id !in completedIds }
            .map {
                TestResult(
                    testItemId = it.id,
                    testItemName = it.name,
                    result = TestResultStatus.SKIPPED,
                    deviceId = deviceId
                )
            }

        if (skippedResults.isNotEmpty()) {
            _testResults.value = _testResults.value + skippedResults
        }

        generateSummary()
    }

    /**
     * 重置测试状态
     */
    fun reset() {
        _testState.value = AutoTestState.IDLE
        _testResults.value = emptyList()
        _progress.value = 0
        _logs.value = emptyList()
        _resultSummary.value = null
        _currentTestState.value = CurrentTestState()
        addLog("状态已重置")
    }

    /**
     * 生成测试结果汇总
     */
    private fun generateSummary() {
        val results = _testResults.value
        val passed = results.count { it.result == TestResultStatus.PASS }
        val failed = results.count { it.result == TestResultStatus.FAIL }
        val skipped = results.count { it.result == TestResultStatus.SKIPPED || it.result == TestResultStatus.TIMEOUT }

        val summary = TestResultSummary(
            deviceId = deviceId,
            model = android.os.Build.MODEL,
            manufacturer = android.os.Build.MANUFACTURER,
            testDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date()),
            operator = "工厂测试员",
            results = results.map {
                TestResultItem(
                    id = it.testItemId,
                    name = it.testItemName,
                    status = when (it.result) {
                        TestResultStatus.PASS -> "PASS"
                        TestResultStatus.FAIL -> "FAIL"
                        TestResultStatus.SKIPPED -> "SKIP"
                        TestResultStatus.TIMEOUT -> "TIMEOUT"
                    }
                )
            },
            summary = TestSummaryStats(
                total = results.size,
                passed = passed,
                failed = failed,
                skipped = skipped
            ),
            appVersion = "1.0.0"
        )

        _resultSummary.value = summary
    }

    /**
     * 添加日志
     */
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _logs.value = _logs.value + "[$timestamp] $message"
    }

    /**
     * 获取测试统计信息
     */
    fun getTestStats(): TestSummaryStats {
        val results = _testResults.value
        return TestSummaryStats(
            total = results.size,
            passed = results.count { it.result == TestResultStatus.PASS },
            failed = results.count { it.result == TestResultStatus.FAIL },
            skipped = results.count { it.result == TestResultStatus.SKIPPED || it.result == TestResultStatus.TIMEOUT }
        )
    }
}
