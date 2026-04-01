package com.mxj.mmitest.ui.testitems;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.R;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 震动测试
 */
public class VibrationTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private Vibrator vibrator;
    private boolean vibrationOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(android.view.Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("震动测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n点击下方按钮测试震动功能\n\n如果手机震动，说明测试通过");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(android.view.Gravity.CENTER);
        layout.addView(descView);

        Button testBtn = new Button(this);
        testBtn.setText("测试震动");
        testBtn.setOnClickListener(v -> {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(500);
                vibrationOk = true;
                setPassEnabled(true);
                descView.setText("\n震动正常！\n\n点击PASS或FAIL结束测试");
                descView.setTextColor(0xFF4CAF50);
            } else {
                descView.setText("\n震动功能不可用");
                descView.setTextColor(0xFFF44336);
                vibrationOk = false;
                setPassEnabled(false);
            }
        });
        layout.addView(testBtn);

        setCustomContentView(layout);
    }

    @Override
    protected String getTestName() {
        return "震动测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试振动马达功能\n\n操作步骤：\n1. 点击【测试震动】按钮\n2. 感受手机是否震动";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 15;
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
        return vibrationOk;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(3, "震动测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
