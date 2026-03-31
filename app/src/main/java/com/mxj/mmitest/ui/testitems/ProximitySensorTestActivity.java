package com.mxj.mmitest.ui.testitems;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 距离传感器测试
 */
public class ProximitySensorTestActivity extends BaseTestActivity implements SensorEventListener {

    private TestRepository repository;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView statusView;
    private TextView valueView;
    private boolean sensorOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("距离传感器测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n检测距离传感器\n\n请将手机靠近物体测试");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        statusView.setText(proximitySensor != null ? "状态: 就绪" : "状态: 传感器不可用");
        statusView.setTextSize(16);
        statusView.setTextColor(proximitySensor != null ? 0xFF4CAF50 : 0xFFF44336);
        statusView.setGravity(Gravity.CENTER);
        layout.addView(statusView);

        valueView = new TextView(this);
        valueView.setText("距离值: --");
        valueView.setTextSize(16);
        valueView.setTextColor(0xFF333333);
        valueView.setGravity(Gravity.CENTER);
        layout.addView(valueView);

        TextView hintView = new TextView(this);
        hintView.setText("\n用手遮住屏幕上方的传感器\n观察数值是否有变化");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            float maxRange = proximitySensor.getMaximumRange();

            valueView.setText(String.format("距离值: %.1f cm\n最大范围: %.1f cm", distance, maxRange));

            // 检测传感器是否有响应
            if (distance < maxRange) {
                statusView.setText("状态: 检测到近距离");
                statusView.setTextColor(0xFF4CAF50);
                sensorOk = true;
                setPassEnabled(true);
            } else {
                statusView.setText("状态: 远距离(正常)");
                statusView.setTextColor(0xFF4CAF50);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不需要处理
    }

    @Override
    protected String getTestName() {
        return "距离传感器测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试距离传感器功能\n\n操作步骤：\n1. 将手靠近手机上方的传感器\n2. 观察数值是否有变化";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 20;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return null;
    }

    @Override
    protected void onTestExecute() {
        setPassEnabled(false);
    }

    @Override
    protected boolean isPassEnabled() {
        return sensorOk;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(23, "距离传感器测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
