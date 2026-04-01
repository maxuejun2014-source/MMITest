package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

import java.io.File;

/**
 * 存储测试
 */
public class StorageTestActivity extends BaseTestActivity {

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
        layout.setGravity(android.view.Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("存储测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n检测存储设备状态\n\n请检查存储是否正常");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(android.view.Gravity.CENTER);
        layout.addView(descView);

        boolean storageOk = isStorageAvailable();
        if (storageOk) {
            descView.setText("\n存储设备正常！\n\n点击PASS或FAIL结束测试");
            descView.setTextColor(0xFF4CAF50);
            setPassEnabled(true);
        } else {
            descView.setText("\n存储设备不可用");
            descView.setTextColor(0xFFF44336);
            setPassEnabled(false);
        }

        setCustomContentView(layout);
    }

    private boolean isStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected String getTestName() {
        return "存储测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试存储设备功能\n\n检查外部存储是否可访问";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    protected void onTestExecute() {
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return isStorageAvailable();
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(2, "存储测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
