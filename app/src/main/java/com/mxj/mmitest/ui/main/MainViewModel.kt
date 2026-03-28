package com.mxj.mmitest.ui.main

import androidx.lifecycle.ViewModel
import com.mxj.mmitest.config.TestConfig

/**
 * 主界面ViewModel
 * 管理主界面的数据和业务逻辑
 */
class MainViewModel : ViewModel() {

    /**
     * 获取测试项总数
     */
    val totalTestCount: Int = TestConfig.getTotalTestCount()

    /**
     * 获取默认启用的测试项数量
     */
    val enabledTestCount: Int = TestConfig.getDefaultEnabledCount()

    /**
     * 获取当前设备配置
     */
    val deviceProfile = TestConfig.getCurrentDeviceProfile()

    /**
     * 获取应用版本名称
     */
    val appVersion: String = "1.0.0"
}
