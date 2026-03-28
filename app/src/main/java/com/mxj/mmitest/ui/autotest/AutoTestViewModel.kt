package com.mxj.mmitest.ui.autotest

import androidx.lifecycle.ViewModel
import com.mxj.mmitest.config.TestConfig

/**
 * 自动测试ViewModel
 */
class AutoTestViewModel : ViewModel() {

    /**
     * 获取启用的测试项数量
     */
    val totalCount: Int = TestConfig.getEnabledTestItems().size
}
