package com.mxj.mmitest.ui.testitems;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 充电测试
 */
public class ChargingTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private TextView statusView;
    private TextView levelView;
    private boolean isCharging = false;
    private int batteryLevel = 0;

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryStatus(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("充电测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n检测电池充电状态\n\n请连接充电器进行测试");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        statusView.setText("充电状态: 检测中...");
        statusView.setTextSize(18);
        statusView.setTextColor(0xFF333333);
        statusView.setGravity(Gravity.CENTER);
        layout.addView(statusView);

        levelView = new TextView(this);
        levelView.setText("电池电量: --");
        levelView.setTextSize(16);
        levelView.setTextColor(0xFF666666);
        levelView.setGravity(Gravity.CENTER);
        layout.addView(levelView);

        setCustomContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryReceiver);
    }

    private void updateBatteryStatus(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                     status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            statusView.setText("充电状态: 充电中");
            statusView.setTextColor(0xFF4CAF50);
        } else {
            statusView.setText("充电状态: 未充电");
            statusView.setTextColor(0xFFFF9800);
        }

        levelView.setText("电池电量: " + batteryLevel + "%");
        setPassEnabled(isCharging);
    }

    @Override
    protected String getTestName() {
        return "充电测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试充电功能\n\n请连接充电器，检查是否显示充电中";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 20;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{android.Manifest.permission.READ_PHONE_STATE};
    }

    @Override
    protected void onTestExecute() {
        setPassEnabled(false);
    }

    @Override
    protected boolean isPassEnabled() {
        return isCharging;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(9, "充电测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
