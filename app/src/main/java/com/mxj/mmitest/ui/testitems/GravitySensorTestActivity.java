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
 * 重力传感器测试
 */
public class GravitySensorTestActivity extends BaseTestActivity implements SensorEventListener {

    private TestRepository repository;
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private TextView xView, yView, zView, statusView;
    private boolean sensorOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("重力传感器测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n检测重力传感器数据\n\n请晃动手机观察数值变化");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        xView = new TextView(this);
        xView.setText("X轴: --");
        xView.setTextSize(16);
        xView.setTextColor(0xFF333333);
        layout.addView(xView);

        yView = new TextView(this);
        yView.setText("Y轴: --");
        yView.setTextSize(16);
        yView.setTextColor(0xFF333333);
        layout.addView(yView);

        zView = new TextView(this);
        zView.setText("Z轴: --");
        zView.setTextSize(16);
        zView.setTextColor(0xFF333333);
        layout.addView(zView);

        statusView = new TextView(this);
        statusView.setText("状态: 检测中...");
        statusView.setTextSize(14);
        statusView.setTextColor(0xFF666666);
        layout.addView(statusView);

        setCustomContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gravitySensor != null) {
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            statusView.setText("状态: 传感器不可用");
            statusView.setTextColor(0xFFF44336);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            xView.setText(String.format("X轴: %.2f", x));
            yView.setText(String.format("Y轴: %.2f", y));
            zView.setText(String.format("Z轴: %.2f", z));

            // 检测是否有明显变化
            if (Math.abs(x) > 1 || Math.abs(y) > 1 || Math.abs(z) > 9.8) {
                sensorOk = true;
                statusView.setText("状态: 传感器正常");
                statusView.setTextColor(0xFF4CAF50);
                setPassEnabled(true);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不需要处理
    }

    @Override
    protected String getTestName() {
        return "重力传感器测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试重力传感器功能\n\n请晃动手机，观察数值是否有变化";
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
        repository.saveSingleTestResultSync(10, "重力传感器测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
