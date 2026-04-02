package com.mxj.mmitest.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.mxj.mmitest.R;
import com.mxj.mmitest.config.TestConfig;
import com.mxj.mmitest.ui.autotest.AutoTestActivity;
import com.mxj.mmitest.ui.base.BaseActivity;
import com.mxj.mmitest.ui.result.ResultActivity;
import com.mxj.mmitest.ui.singletest.SingleTestActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity {

    private TextView tvTestCount;
    private Button btnAutoTest;
    private Button btnSingleTest;
    private Button btnResult;
    private Button btnExit;
    private TextView tvVersion;

    // 所有需要检查的权限
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.VIBRATE
    };

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 注册权限请求
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // 系统会自动处理，用户同意或拒绝后继续
                }
        );

        initViews();
        setupListeners();
        updateUI();

        // 检查并请求权限
        checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void checkAndRequestPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            permissionLauncher.launch(missingPermissions.toArray(new String[0]));
        }
    }

    private void initViews() {
        tvTestCount = findViewById(R.id.tv_test_count);
        btnAutoTest = findViewById(R.id.btn_auto_test);
        btnSingleTest = findViewById(R.id.btn_single_test);
        btnResult = findViewById(R.id.btn_result);
        btnExit = findViewById(R.id.btn_exit);
        tvVersion = findViewById(R.id.tv_version);
    }

    private void setupListeners() {
        btnAutoTest.setOnClickListener(v -> {
            startActivity(new Intent(this, AutoTestActivity.class));
        });

        btnSingleTest.setOnClickListener(v -> {
            startActivity(new Intent(this, SingleTestActivity.class));
        });

        btnResult.setOnClickListener(v -> {
            startActivity(new Intent(this, ResultActivity.class));
        });

        btnExit.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateUI() {
        int totalCount = TestConfig.getTotalTestCount();
        int enabledCount = TestConfig.getDefaultEnabledCount();
        tvTestCount.setText("共 " + enabledCount + "/" + totalCount + " 项测试已启用");

        try {
            String versionName = getPackageManager()
                .getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText("版本 " + versionName);
        } catch (Exception e) {
            tvVersion.setText("版本 1.0.0");
        }
    }
}
