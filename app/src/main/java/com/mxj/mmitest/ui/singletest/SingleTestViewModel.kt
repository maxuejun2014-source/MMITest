package com.mxj.mmitest.ui.singletest

import androidx.lifecycle.ViewModel
import com.mxj.mmitest.config.TestConfig

/**
 * 单项测试ViewModel
 */
class SingleTestViewModel : ViewModel() {

    /**
     * 获取所有测试项
     */
    val testItems: List<TestConfig.TestItem> = TestConfig.testItems

    /**
     * 获取设备支持的测试项
     */
    val supportedTestItems: List<TestConfig.TestItem> = TestConfig.getSupportedTestItems()
}
