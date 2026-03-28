package com.mxj.mmitest.config

import android.app.Activity
import com.mxj.mmitest.ui.testitems.*

/**
 * 测试项配置
 * 包含23个测试项的定义和设备配置管理
 */
object TestConfig {

    /**
     * 测试项列表
     */
    val testItems = listOf(
        TestItem(
            id = 1,
            name = "SIM卡测试",
            description = "检测SIM卡状态",
            activityClass = SimTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(
                android.Manifest.permission.READ_PHONE_STATE
            ),
            timeoutSeconds = 15
        ),
        TestItem(
            id = 2,
            name = "存储测试",
            description = "检测内部存储和SD卡",
            activityClass = StorageTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            timeoutSeconds = 30
        ),
        TestItem(
            id = 3,
            name = "震动测试",
            description = "测试振动马达功能",
            activityClass = VibrationTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 15
        ),
        TestItem(
            id = 4,
            name = "版本号测试",
            description = "显示设备版本信息",
            activityClass = VersionTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 10
        ),
        TestItem(
            id = 5,
            name = "LCD测试",
            description = "屏幕显示测试（纯色检测）",
            activityClass = LcdTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 60
        ),
        TestItem(
            id = 6,
            name = "背光测试",
            description = "屏幕背光调节测试",
            activityClass = BacklightTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 30
        ),
        TestItem(
            id = 7,
            name = "按键测试",
            description = "物理按键和虚拟按键测试",
            activityClass = ButtonTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 30
        ),
        TestItem(
            id = 8,
            name = "TP测试",
            description = "触摸屏测试",
            activityClass = TpTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 45
        ),
        TestItem(
            id = 9,
            name = "充电测试",
            description = "充电接口和充电状态测试",
            activityClass = ChargingTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 20
        ),
        TestItem(
            id = 10,
            name = "重力传感器测试",
            description = "加速度计功能测试",
            activityClass = GravitySensorTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 20
        ),
        TestItem(
            id = 11,
            name = "铃声测试",
            description = "扬声器播放铃声测试",
            activityClass = RingtoneTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 20
        ),
        TestItem(
            id = 12,
            name = "音频回环测试",
            description = "麦克风和扬声器回环测试",
            activityClass = AudioLoopbackTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(android.Manifest.permission.RECORD_AUDIO),
            timeoutSeconds = 30
        ),
        TestItem(
            id = 13,
            name = "听筒测试",
            description = "听筒功能测试",
            activityClass = EarpieceTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 20
        ),
        TestItem(
            id = 14,
            name = "耳机回环测试",
            description = "耳机接口测试",
            activityClass = HeadphoneTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 20
        ),
        TestItem(
            id = 15,
            name = "FM测试",
            description = "FM收音机功能测试",
            activityClass = FmTestActivity::class.java,
            enabledByDefault = false,
            supportedByDefault = false,
            requiredPermissions = listOf(android.Manifest.permission.RECEIVE_BOOT_COMPLETED),
            timeoutSeconds = 45
        ),
        TestItem(
            id = 16,
            name = "前摄测试",
            description = "前置摄像头测试",
            activityClass = FrontCameraTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(android.Manifest.permission.CAMERA),
            timeoutSeconds = 45
        ),
        TestItem(
            id = 17,
            name = "后摄测试",
            description = "后置摄像头测试（含闪光灯）",
            activityClass = RearCameraTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(android.Manifest.permission.CAMERA),
            timeoutSeconds = 60
        ),
        TestItem(
            id = 18,
            name = "电话测试",
            description = "通话功能测试",
            activityClass = PhoneTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.CALL_PHONE
            ),
            timeoutSeconds = 30
        ),
        TestItem(
            id = 19,
            name = "WiFi测试",
            description = "检测附近可用WiFi网络",
            activityClass = WifiTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE
            ),
            timeoutSeconds = 30
        ),
        TestItem(
            id = 20,
            name = "蓝牙测试",
            description = "检测附近可用蓝牙设备",
            activityClass = BluetoothTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            ),
            timeoutSeconds = 30
        ),
        TestItem(
            id = 21,
            name = "GPS测试",
            description = "GPS定位测试（搜到星大于等于3）",
            activityClass = GpsTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            requiredPermissions = listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            timeoutSeconds = 120
        ),
        TestItem(
            id = 22,
            name = "OTG测试",
            description = "USB OTG功能测试",
            activityClass = OtgTestActivity::class.java,
            enabledByDefault = false,
            supportedByDefault = false,
            timeoutSeconds = 30
        ),
        TestItem(
            id = 23,
            name = "距离传感器测试",
            description = "距离传感器功能测试",
            activityClass = ProximitySensorTestActivity::class.java,
            enabledByDefault = true,
            supportedByDefault = true,
            timeoutSeconds = 20
        )
    )

    /**
     * 测试项数据类
     */
    data class TestItem(
        val id: Int,
        val name: String,
        val description: String,
        val activityClass: Class<out Activity>,
        val enabledByDefault: Boolean = true,
        val supportedByDefault: Boolean = true,
        val requiredPermissions: List<String> = emptyList(),
        val timeoutSeconds: Int = 60
    )

    /**
     * 设备配置
     */
    data class DeviceProfile(
        val deviceModel: String,
        val deviceManufacturer: String,
        val enabledTestIds: Set<Int>,
        val supportedTestIds: Set<Int>,
        val description: String = ""
    )

    /**
     * 预定义设备配置列表
     */
    val deviceProfiles = listOf(
        DeviceProfile(
            deviceModel = "DEFAULT",
            deviceManufacturer = "通用",
            enabledTestIds = testItems.filter { it.enabledByDefault }.map { it.id }.toSet(),
            supportedTestIds = testItems.filter { it.supportedByDefault }.map { it.id }.toSet(),
            description = "默认配置，包含大多数标准测试项"
        )
    )

    /**
     * 获取当前设备的配置
     */
    fun getCurrentDeviceProfile(): DeviceProfile {
        val currentModel = android.os.Build.MODEL
        val currentManufacturer = android.os.Build.MANUFACTURER

        return deviceProfiles.find {
            it.deviceModel == currentModel && it.deviceManufacturer == currentManufacturer
        } ?: deviceProfiles.find { it.deviceModel == "DEFAULT" }!!
    }

    /**
     * 获取设备启用的测试项列表
     */
    fun getEnabledTestItems(): List<TestItem> {
        val profile = getCurrentDeviceProfile()
        return testItems.filter { it.id in profile.enabledTestIds }
    }

    /**
     * 获取设备支持的测试项列表
     */
    fun getSupportedTestItems(): List<TestItem> {
        val profile = getCurrentDeviceProfile()
        return testItems.filter { it.id in profile.supportedTestIds }
    }

    /**
     * 获取测试项总数
     */
    fun getTotalTestCount(): Int = testItems.size

    /**
     * 获取默认启用的测试项数量
     */
    fun getDefaultEnabledCount(): Int = testItems.count { it.enabledByDefault }
}
