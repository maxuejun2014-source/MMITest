package com.mxj.mmitest.ui.testitems;

import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.R;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 版本号测试
 */
public class VersionTestActivity extends BaseTestActivity {

    private TestRepository repository;

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

        String[] labels = {
            "设备型号: " + Build.MODEL,
            "设备制造商: " + Build.MANUFACTURER,
            "Android版本: " + Build.VERSION.RELEASE,
            "SDK版本: " + Build.VERSION.SDK_INT,
            "主板: " + Build.BOARD,
            "硬件: " + Build.HARDWARE,
            "产品名: " + Build.PRODUCT,
            "版本号: " + Build.DISPLAY
        };

        for (String label : labels) {
            TextView view = new TextView(this);
            view.setText(label);
            view.setTextSize(14);
            view.setTextColor(0xFF333333);
            view.setPadding(0, 8, 0, 8);
            layout.addView(view);
        }

        setCustomContentView(layout);
        setPassEnabled(true); // 版本信息总是可以获取
    }

    @Override
    protected String getTestName() {
        return "版本号测试";
    }

    @Override
    protected String getTestDescription() {
        return "显示设备版本信息\n\n请检查上述信息是否正确";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 10;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return null;
    }

    @Override
    protected void onTestExecute() {
        // 版本信息总是可获取
    }

    @Override
    protected boolean isPassEnabled() {
        return true;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(4, "版本号测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
