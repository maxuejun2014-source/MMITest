package com.mxj.mmitest.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private List<String> missingPermissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 注册权限请求
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    missingPermissions.clear();
                    for (String permission : permissions.keySet()) {
                        if (!permissions.get(permission)) {
                            allGranted = false;
                            missingPermissions.add(permission);
                        }
                    }
                    if (!allGranted) {
                        showPermissionDeniedDialog();
                    }
                }
        );

        initViews();
        setupListeners();
        updateUI();

        // 检查权限
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        // 每次恢复时也检查权限
        checkPermissions();
    }

    private void checkPermissions() {
        missingPermissions.clear();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            showPermissionRationaleDialog();
        }
    }

    private void showPermissionRationaleDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("应用需要以下权限才能正常运行所有测试功能：\n\n");
        for (String permission : missingPermissions) {
            sb.append("- ").append(getPermissionDisplayName(permission)).append("\n");
        }
        sb.append("\n是否授予这些权限？");

        new AlertDialog.Builder(this)
                .setTitle("权限请求")
                .setMessage(sb.toString())
                .setPositiveButton("授予权限", (d, w) -> requestPermissions())
                .setNegativeButton("稍后", null)
                .setCancelable(false)
                .show();
    }

    private void showPermissionDeniedDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("以下权限未被授予，功能可能受限：\n\n");
        for (String permission : missingPermissions) {
            sb.append("- ").append(getPermissionDisplayName(permission)).append("\n");
        }
        sb.append("\n请点击确定前往设置页面手动授予权限，或取消退出应用。");

        new AlertDialog.Builder(this)
                .setTitle("权限不足")
                .setMessage(sb.toString())
                .setPositiveButton("前往设置", (d, w) -> openAppSettings())
                .setNegativeButton("取消", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void requestPermissions() {
        permissionLauncher.launch(missingPermissions.toArray(new String[0]));
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private String getPermissionDisplayName(String permission) {
        if (permission == null) return "未知权限";
        switch (permission) {
            case Manifest.permission.READ_PHONE_STATE:
                return "读取手机状态";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "读取存储";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "写入存储";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "精确定位";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "模糊定位";
            case Manifest.permission.CAMERA:
                return "相机";
            case Manifest.permission.RECORD_AUDIO:
                return "录音";
            case Manifest.permission.CALL_PHONE:
                return "打电话";
            case Manifest.permission.BLUETOOTH:
                return "蓝牙";
            case Manifest.permission.BLUETOOTH_ADMIN:
                return "蓝牙管理";
            case Manifest.permission.ACCESS_WIFI_STATE:
                return "WiFi状态";
            case Manifest.permission.CHANGE_WIFI_STATE:
                return "WiFi控制";
            case Manifest.permission.RECEIVE_BOOT_COMPLETED:
                return "开机启动";
            case Manifest.permission.VIBRATE:
                return "震动";
            default:
                return permission;
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
