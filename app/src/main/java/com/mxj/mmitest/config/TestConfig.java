package com.mxj.mmitest.config;

import android.app.Activity;
import com.mxj.mmitest.ui.testitems.*;

/**
 * 测试项配置
 * 包含23个测试项的定义和设备配置管理
 */
public class TestConfig {

    /**
     * 测试设置
     */
    public static class TestSettings {
        // PASS按钮是否需要长按（false=单击即触发，true=需要长按）
        public static final boolean PASS_REQUIRE_LONG_PRESS = true;

        // PASS按钮长按时长（毫秒）
        public static final long PASS_LONG_PRESS_DURATION_MS = 500L;
    }

    /**
     * 测试项数据类
     */
    public static class TestItem {
        private final int id;
        private final String name;
        private final String description;
        private final Class<? extends Activity> activityClass;
        private final boolean enabledByDefault;
        private final boolean supportedByDefault;
        private final String[] requiredPermissions;
        private final int timeoutSeconds;

        public TestItem(int id, String name, String description,
                       Class<? extends Activity> activityClass,
                       boolean enabledByDefault, boolean supportedByDefault,
                       String[] requiredPermissions, int timeoutSeconds) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.activityClass = activityClass;
            this.enabledByDefault = enabledByDefault;
            this.supportedByDefault = supportedByDefault;
            this.requiredPermissions = requiredPermissions;
            this.timeoutSeconds = timeoutSeconds;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Class<? extends Activity> getActivityClass() { return activityClass; }
        public boolean isEnabledByDefault() { return enabledByDefault; }
        public boolean isSupportedByDefault() { return supportedByDefault; }
        public String[] getRequiredPermissions() { return requiredPermissions; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
    }

    /**
     * 设备配置
     */
    public static class DeviceProfile {
        private final String deviceModel;
        private final String deviceManufacturer;
        private final int[] enabledTestIds;
        private final int[] supportedTestIds;
        private final String description;

        public DeviceProfile(String deviceModel, String deviceManufacturer,
                           int[] enabledTestIds, int[] supportedTestIds,
                           String description) {
            this.deviceModel = deviceModel;
            this.deviceManufacturer = deviceManufacturer;
            this.enabledTestIds = enabledTestIds;
            this.supportedTestIds = supportedTestIds;
            this.description = description;
        }

        public String getDeviceModel() { return deviceModel; }
        public String getDeviceManufacturer() { return deviceManufacturer; }
        public int[] getEnabledTestIds() { return enabledTestIds; }
        public int[] getSupportedTestIds() { return supportedTestIds; }
        public String getDescription() { return description; }
    }

    /**
     * 测试项列表
     */
    public static final TestItem[] TEST_ITEMS = {
        new TestItem(1, "SIM卡测试", "检测SIM卡状态",
            SimTestActivity.class, true, true,
            new String[]{android.Manifest.permission.READ_PHONE_STATE}, 15),
        new TestItem(2, "存储测试", "检测内部存储和SD卡",
            StorageTestActivity.class, true, true,
            new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 30),
        new TestItem(3, "震动测试", "测试振动马达功能",
            VibrationTestActivity.class, true, true, null, 15),
        new TestItem(4, "版本号测试", "显示设备版本信息",
            VersionTestActivity.class, true, true, null, 10),
        new TestItem(5, "LCD测试", "屏幕显示测试（纯色检测）",
            LcdTestActivity.class, true, true, null, 60),
        new TestItem(6, "背光测试", "屏幕背光调节测试",
            BacklightTestActivity.class, true, true, null, 30),
        new TestItem(7, "按键测试", "物理按键和虚拟按键测试",
            ButtonTestActivity.class, true, true, null, 30),
        new TestItem(8, "TP测试", "触摸屏测试",
            TpTestActivity.class, true, true, null, 45),
        new TestItem(9, "充电测试", "充电接口和充电状态测试",
            ChargingTestActivity.class, true, true,
            new String[]{android.Manifest.permission.READ_PHONE_STATE}, 20),
        new TestItem(10, "重力传感器测试", "加速度计功能测试",
            GravitySensorTestActivity.class, true, true, null, 20),
        new TestItem(11, "铃声测试", "扬声器播放铃声测试",
            RingtoneTestActivity.class, true, true, null, 20),
        new TestItem(12, "音频回环测试", "麦克风和扬声器回环测试",
            AudioLoopbackTestActivity.class, true, true,
            new String[]{android.Manifest.permission.RECORD_AUDIO}, 30),
        new TestItem(13, "听筒测试", "听筒功能测试",
            EarpieceTestActivity.class, true, true, null, 20),
        new TestItem(14, "耳机回环测试", "耳机接口测试",
            HeadphoneTestActivity.class, true, true, null, 20),
        new TestItem(15, "FM测试", "FM收音机功能测试",
            FmTestActivity.class, false, false,
            new String[]{android.Manifest.permission.RECEIVE_BOOT_COMPLETED}, 45),
        new TestItem(16, "前摄测试", "前置摄像头测试",
            FrontCameraTestActivity.class, true, true,
            new String[]{android.Manifest.permission.CAMERA}, 45),
        new TestItem(17, "后摄测试", "后置摄像头测试（含闪光灯）",
            RearCameraTestActivity.class, true, true,
            new String[]{android.Manifest.permission.CAMERA}, 60),
        new TestItem(18, "电话测试", "通话功能测试",
            PhoneTestActivity.class, true, true,
            new String[]{
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.CALL_PHONE
            }, 30),
        new TestItem(19, "WiFi测试", "检测附近可用WiFi网络",
            WifiTestActivity.class, true, true,
            new String[]{
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE
            }, 30),
        new TestItem(20, "蓝牙测试", "检测附近可用蓝牙设备",
            BluetoothTestActivity.class, true, true,
            new String[]{
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            }, 30),
        new TestItem(21, "GPS测试", "GPS定位测试（搜到星大于等于3）",
            GpsTestActivity.class, true, true,
            new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 120),
        new TestItem(22, "OTG测试", "USB OTG功能测试",
            OtgTestActivity.class, false, false, null, 30),
        new TestItem(23, "距离传感器测试", "距离传感器功能测试",
            ProximitySensorTestActivity.class, true, true, null, 20)
    };

    /**
     * 预定义设备配置列表
     */
    public static final DeviceProfile[] DEVICE_PROFILES = {
        new DeviceProfile(
            "DEFAULT", "通用",
            getDefaultEnabledTestIds(), getDefaultSupportedTestIds(),
            "默认配置，包含大多数标准测试项"
        )
    };

    private static int[] getDefaultEnabledTestIds() {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (TestItem item : TEST_ITEMS) {
            if (item.isEnabledByDefault()) {
                ids.add(item.getId());
            }
        }
        int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.get(i);
        }
        return result;
    }

    private static int[] getDefaultSupportedTestIds() {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (TestItem item : TEST_ITEMS) {
            if (item.isSupportedByDefault()) {
                ids.add(item.getId());
            }
        }
        int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.get(i);
        }
        return result;
    }

    /**
     * 获取当前设备的配置
     */
    public static DeviceProfile getCurrentDeviceProfile() {
        String currentModel = android.os.Build.MODEL;
        String currentManufacturer = android.os.Build.MANUFACTURER;

        for (DeviceProfile profile : DEVICE_PROFILES) {
            if (profile.getDeviceModel().equals(currentModel)
                && profile.getDeviceManufacturer().equals(currentManufacturer)) {
                return profile;
            }
        }
        return DEVICE_PROFILES[0]; // 返回默认配置
    }

    /**
     * 获取设备启用的测试项列表
     */
    public static TestItem[] getEnabledTestItems() {
        DeviceProfile profile = getCurrentDeviceProfile();
        java.util.List<TestItem> items = new java.util.ArrayList<>();
        for (TestItem item : TEST_ITEMS) {
            for (int id : profile.getEnabledTestIds()) {
                if (item.getId() == id) {
                    items.add(item);
                    break;
                }
            }
        }
        return items.toArray(new TestItem[0]);
    }

    /**
     * 获取设备支持的测试项列表
     */
    public static TestItem[] getSupportedTestItems() {
        DeviceProfile profile = getCurrentDeviceProfile();
        java.util.List<TestItem> items = new java.util.ArrayList<>();
        for (TestItem item : TEST_ITEMS) {
            for (int id : profile.getSupportedTestIds()) {
                if (item.getId() == id) {
                    items.add(item);
                    break;
                }
            }
        }
        return items.toArray(new TestItem[0]);
    }

    /**
     * 获取测试项总数
     */
    public static int getTotalTestCount() {
        return TEST_ITEMS.length;
    }

    /**
     * 获取默认启用的测试项数量
     */
    public static int getDefaultEnabledCount() {
        int count = 0;
        for (TestItem item : TEST_ITEMS) {
            if (item.isEnabledByDefault()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 根据ID获取测试项
     */
    public static TestItem getTestItemById(int id) {
        for (TestItem item : TEST_ITEMS) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
