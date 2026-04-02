package com.mxj.mmitest.ui.testitems;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 充电测试 - 显示电池电量、状态、电压、温度、健康状态、充电方式
 */
public class ChargingTestActivity extends BaseTestActivity {

    private TestRepository repository;

    private LinearLayout mContainer;
    private TextView mLevelText;
    private TextView mStatusText;
    private TextView mVoltageText;
    private TextView mTemperatureText;
    private TextView mHealthText;
    private ProgressBar mBatteryProgress;
    private View mBatteryIcon;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
    }

    private void setupContentView() {
        mContainer = new LinearLayout(this);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setGravity(Gravity.CENTER);
        mContainer.setPadding(40, 60, 40, 40);

        // 标题
        TextView titleText = new TextView(this);
        titleText.setText("Battery & Charging Test");
        titleText.setTextSize(22);
        titleText.setTextColor(Color.parseColor("#333333"));
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 40);
        mContainer.addView(titleText);

        // 电池图标区域
        LinearLayout batteryIconLayout = new LinearLayout(this);
        batteryIconLayout.setOrientation(LinearLayout.VERTICAL);
        batteryIconLayout.setGravity(Gravity.CENTER);
        batteryIconLayout.setPadding(0, 0, 0, 30);

        // 电池外壳（外框）
        View batteryOutline = new View(this);
        batteryOutline.setBackgroundColor(Color.parseColor("#666666"));
        LinearLayout.LayoutParams outlineParams = new LinearLayout.LayoutParams(120, 60);
        outlineParams.setMargins(0, 0, 0, 5);
        batteryOutline.setLayoutParams(outlineParams);
        batteryIconLayout.addView(batteryOutline);

        // 电池电量填充
        mBatteryIcon = new View(this);
        mBatteryIcon.setBackgroundColor(Color.parseColor("#4CAF50"));
        LinearLayout.LayoutParams fillParams = new LinearLayout.LayoutParams(0, 50);
        fillParams.setMargins(10, 5, 10, 5);
        mBatteryIcon.setLayoutParams(fillParams);
        batteryIconLayout.addView(mBatteryIcon);

        mContainer.addView(batteryIconLayout);

        // 电量进度条
        mBatteryProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        mBatteryProgress.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 40));
        mBatteryProgress.setMax(100);
        mBatteryProgress.setProgress(0);
        mBatteryProgress.getProgressDrawable().setColorFilter(Color.parseColor("#4CAF50"), PorterDuff.Mode.SRC_IN);
        mContainer.addView(mBatteryProgress);

        // 电量百分比文本
        mLevelText = new TextView(this);
        mLevelText.setText("Battery Level: 0%");
        mLevelText.setTextSize(20);
        mLevelText.setTextColor(Color.parseColor("#333333"));
        mLevelText.setGravity(Gravity.CENTER);
        mLevelText.setPadding(0, 15, 0, 10);
        mContainer.addView(mLevelText);

        // 创建信息容器
        LinearLayout infoContainer = new LinearLayout(this);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
        infoContainer.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.setMargins(0, 20, 0, 0);
        infoContainer.setLayoutParams(infoParams);

        // 状态信息
        mStatusText = createInfoRow(this, infoContainer, "Status", "Checking...");
        mVoltageText = createInfoRow(this, infoContainer, "Voltage", "Unknown");
        mTemperatureText = createInfoRow(this, infoContainer, "Temperature", "Unknown");
        mHealthText = createInfoRow(this, infoContainer, "Health", "Unknown");

        mContainer.addView(infoContainer);

        // 提示文本
        TextView hintText = new TextView(this);
        hintText.setText("Connect charger to test charging function");
        hintText.setTextSize(16);
        hintText.setTextColor(Color.parseColor("#666666"));
        hintText.setGravity(Gravity.CENTER);
        hintText.setPadding(0, 30, 0, 0);
        mContainer.addView(hintText);

        setCustomContentView(mContainer);
    }

    private TextView createInfoRow(Context context, LinearLayout container, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView labelView = new TextView(context);
        labelView.setText(label + ": ");
        labelView.setTextSize(16);
        labelView.setTextColor(Color.parseColor("#666666"));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView valueView = new TextView(context);
        valueView.setText(value);
        valueView.setTextSize(16);
        valueView.setTextColor(Color.parseColor("#333333"));
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        valueView.setGravity(Gravity.END);

        row.addView(labelView);
        row.addView(valueView);
        container.addView(row);

        // 添加分隔线
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        container.addView(divider);

        return valueView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBatteryStatus(intent);
            }
        };
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void updateBatteryStatus(Intent intent) {
        // 获取电池电量
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        // 获取充电状态
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        String statusText = getStatusText(status);

        // 获取电压（单位：mV）
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        String voltageText = voltage > 0 ? String.format("%.2fV", voltage / 1000.0) : "Unknown";

        // 获取温度（单位：0.1°C）
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        String temperatureText = temperature > 0 ? String.format("%.1f°C", temperature / 10.0) : "Unknown";

        // 获取健康状态
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        String healthText = getHealthText(health);

        // 获取充电方式
        int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String plugText = getPlugTypeText(plugType);

        // 更新UI
        updateUI(level, statusText, voltageText, temperatureText, healthText, plugText, isCharging);

        // 如果有充电器连接或电池已满，自动启用PASS按钮
        boolean hasPowerSource = (plugType == BatteryManager.BATTERY_PLUGGED_AC ||
                plugType == BatteryManager.BATTERY_PLUGGED_USB ||
                plugType == BatteryManager.BATTERY_PLUGGED_WIRELESS);
        boolean testPassed = hasPowerSource || status == BatteryManager.BATTERY_STATUS_FULL;
        setPassEnabled(testPassed);
    }

    private void updateUI(int level, String status, String voltage, String temperature, String health, String plugType, boolean isCharging) {
        // 安全检查
        if (mBatteryProgress == null || mBatteryIcon == null ||
                mLevelText == null || mStatusText == null ||
                mVoltageText == null || mTemperatureText == null || mHealthText == null) {
            return;
        }

        // 更新电量进度条
        mBatteryProgress.setProgress(level);

        // 更新电池图标宽度（根据电量百分比）
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBatteryIcon.getLayoutParams();
        if (params != null) {
            params.width = (int) (100 * level / 100.0);
            mBatteryIcon.setLayoutParams(params);
        }

        // 根据电量改变颜色
        int batteryColor;
        if (level > 50) {
            batteryColor = Color.parseColor("#4CAF50"); // 绿色
        } else if (level > 20) {
            batteryColor = Color.parseColor("#FF9800"); // 橙色
        } else {
            batteryColor = Color.parseColor("#F44336"); // 红色
        }
        mBatteryIcon.setBackgroundColor(batteryColor);
        mBatteryProgress.getProgressDrawable().setColorFilter(batteryColor, PorterDuff.Mode.SRC_IN);

        // 更新文本
        mLevelText.setText(String.format("Battery Level: %d%%", level));
        mStatusText.setText(String.format("%s%s", status, !plugType.isEmpty() ? " (" + plugType + ")" : ""));
        mVoltageText.setText(voltage);
        mTemperatureText.setText(temperature);
        mHealthText.setText(health);
    }

    private String getStatusText(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "Not Charging";
            default:
                return "Unknown";
        }
    }

    private String getHealthText(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "Dead";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return "Unspecified Failure";
            case BatteryManager.BATTERY_HEALTH_COLD:
                return "Cold";
            default:
                return "Unknown";
        }
    }

    private String getPlugTypeText(int plugType) {
        switch (plugType) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "AC Charger";
            case BatteryManager.BATTERY_PLUGGED_USB:
                return "USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return "Wireless";
            default:
                return "";
        }
    }

    @Override
    protected String getTestName() {
        return "充电测试";
    }

    @Override
    protected String getTestDescription() {
        return "电池和充电测试\n\n连接充电器检查充电功能";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return null;
    }

    @Override
    protected void onTestExecute() {
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return false; // 由电池状态决定
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(9, "充电测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
