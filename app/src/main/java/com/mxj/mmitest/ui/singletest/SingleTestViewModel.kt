package com.mxj.mmitest.ui.singletest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mxj.mmitest.config.TestConfig
import com.mxj.mmitest.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 测试状态
 */
enum class TestStatus {
    NOT_TESTED,
    PASSED,
    FAILED
}

/**
 * 测试项状态（用于列表显示）
 */
data class TestItemStatus(
    val testItemId: Int,
    val status: TestStatus = TestStatus.NOT_TESTED
)

/**
 * 单项测试ViewModel
 */
class SingleTestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TestRepository(application)

    /**
     * 获取所有测试项
     */
    val testItems: List<TestConfig.TestItem> = TestConfig.testItems

    /**
     * 获取设备支持的测试项
     */
    val supportedTestItems: List<TestConfig.TestItem> = TestConfig.getSupportedTestItems()

    // 测试项状态列表
    private val _testItemStatuses = MutableStateFlow<Map<Int, TestStatus>>(emptyMap())
    val testItemStatuses: StateFlow<Map<Int, TestStatus>> = _testItemStatuses.asStateFlow()

    init {
        loadTestStatuses()
    }

    /**
     * 加载所有测试项的最新状态
     */
    fun loadTestStatuses() {
        viewModelScope.launch {
            val latestResults = repository.getLatestResultsForAllTestItems()
            val statusMap = latestResults.mapValues { (_, result) ->
                when (result.result) {
                    "PASS" -> TestStatus.PASSED
                    "FAIL" -> TestStatus.FAILED
                    else -> TestStatus.NOT_TESTED
                }
            }
            _testItemStatuses.value = statusMap
        }
    }

    /**
     * 更新单个测试项状态
     */
    fun updateTestItemStatus(testItemId: Int, passed: Boolean) {
        val currentStatuses = _testItemStatuses.value.toMutableMap()
        currentStatuses[testItemId] = if (passed) TestStatus.PASSED else TestStatus.FAILED
        _testItemStatuses.value = currentStatuses
    }

    /**
     * 获取指定测试项的状态
     */
    fun getTestItemStatus(testItemId: Int): TestStatus {
        return _testItemStatuses.value[testItemId] ?: TestStatus.NOT_TESTED
    }
}
