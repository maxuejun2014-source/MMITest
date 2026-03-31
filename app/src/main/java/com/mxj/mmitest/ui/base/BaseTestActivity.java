package com.mxj.mmitest.ui.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.mxj.mmitest.R;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试项Activity基类
 * 处理倒计时、权限请求、PASS/FAIL按钮逻辑
 * 子类只需实现抽象方法即可
 */
public abstract class BaseTestActivity extends BaseActivity {

    // ===== 抽象方法 - 子类必须实现 =====

    /**
     * 获取测试名称
     */
    protected abstract String getTestName();

    /**
     * 获取测试描述
     */
    protected abstract String getTestDescription();

    /**
     * 获取超时秒数
     */
    protected abstract int getTimeoutSeconds();

    /**
     * 获取所需权限列表
     */
    protected abstract String[] getRequiredPermissions();

    /**
     * 执行测试逻辑
     * 在此方法中实现具体的测试检测逻辑
     */
    protected abstract void onTestExecute();

    /**
     * 是否启用PASS按钮
     * 返回true时PASS按钮可点击
     */
    protected abstract boolean isPassEnabled();

    // ===== 可选重写方法 =====

    /**
     * 是否自动保存结果（默认true）
     * 如果为false，需要子类手动调用finishAndSaveResult()
     */
    protected boolean isAutoSaveResult() {
        return true;
    }

    /**
     * 获取测试项ID（用于结果记录）
     */
    protected int getTestItemId() {
        return 0;
    }

    /**
     * 测试结果回调（可选重写）
     */
    protected void onTestResult(boolean passed) {
        // 默认空实现，子类可重写
    }

    // ===== 视图组件 =====
    private TextView tvTestName;
    private TextView tvTestDescription;
    private TextView tvCountdown;
    private Button btnPass;
    private Button btnFail;
    private FrameLayout contentContainer;

    // ===== 倒计时 =====
    private int remainingSeconds;
    private Handler handler;
    private Runnable countdownRunnable;
    private boolean isCountdownRunning = false;
    private boolean testCompleted = false;

    // ===== 权限请求 =====
    private ActivityResultLauncher<String[]> permissionLauncher;
    private List<String> missingPermissions = new ArrayList<>();

    // ===== 测试结果 =====
    private boolean lastResult = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_item);

        handler = new Handler(Looper.getMainLooper());

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
                if (allGranted) {
                    onPermissionsGranted();
                } else {
                    showPermissionDeniedDialog();
                }
            }
        );

        initViews();
        checkPermissions();
    }

    private void initViews() {
        tvTestName = findViewById(R.id.tv_test_name);
        tvTestDescription = findViewById(R.id.tv_test_description);
        tvCountdown = findViewById(R.id.tv_countdown);
        btnPass = findViewById(R.id.btn_pass);
        btnFail = findViewById(R.id.btn_fail);
        contentContainer = findViewById(R.id.content_container);

        tvTestName.setText(getTestName());
        tvTestDescription.setText(getTestDescription());
        remainingSeconds = getTimeoutSeconds();
        updateCountdownDisplay();

        btnPass.setOnClickListener(v -> handleTestResult(true));
        btnFail.setOnClickListener(v -> handleTestResult(false));

        updatePassButtonState();
    }

    private void checkPermissions() {
        String[] permissions = getRequiredPermissions();
        if (permissions != null && permissions.length > 0) {
            missingPermissions.clear();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
            if (!missingPermissions.isEmpty()) {
                if (shouldShowRequestPermissionRationale(missingPermissions.get(0))) {
                    showPermissionRationaleDialog();
                } else {
                    permissionLauncher.launch(missingPermissions.toArray(new String[0]));
                }
                return;
            }
        }
        onPermissionsGranted();
    }

    /**
     * 权限授予后执行测试
     */
    protected void onPermissionsGranted() {
        onTestExecute();
        startCountdown();
    }

    private void startCountdown() {
        if (isCountdownRunning) return;
        isCountdownRunning = true;

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                remainingSeconds--;
                updateCountdownDisplay();
                updatePassButtonState();

                if (remainingSeconds <= 0) {
                    // 超时自动判定为失败
                    if (!testCompleted) {
                        handleTestResult(false);
                    }
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(countdownRunnable, 1000);
    }

    private void stopCountdown() {
        isCountdownRunning = false;
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }

    private void updateCountdownDisplay() {
        tvCountdown.setText("剩余时间: " + remainingSeconds + "秒");
        if (remainingSeconds <= 5) {
            tvCountdown.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            tvCountdown.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    private void updatePassButtonState() {
        boolean enabled = isPassEnabled();
        btnPass.setEnabled(enabled);
        btnPass.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**
     * 供子类调用的方法 - 更新PASS按钮状态
     */
    protected void setPassEnabled(boolean enabled) {
        runOnUiThread(() -> {
            btnPass.setEnabled(enabled);
            btnPass.setAlpha(enabled ? 1.0f : 0.5f);
        });
    }

    /**
     * 供子类调用的方法 - 设置自定义内容视图
     */
    protected void setCustomContentView(View view) {
        if (contentContainer != null) {
            contentContainer.removeAllViews();
            contentContainer.addView(view);
        }
    }

    /**
     * 供子类调用的方法 - 设置描述文本
     */
    protected void setDescription(String description) {
        if (tvTestDescription != null) {
            tvTestDescription.setText(description);
        }
    }

    /**
     * 处理测试结果
     */
    private void handleTestResult(boolean passed) {
        if (testCompleted) return;
        testCompleted = true;
        lastResult = passed;

        stopCountdown();
        onTestResult(passed);

        if (isAutoSaveResult()) {
            finishAndSaveResult(passed);
        } else {
            // 子类手动调用finishAndSaveResult
        }
    }

    /**
     * 完成测试并保存结果
     */
    protected void finishAndSaveResult(boolean passed) {
        // 保存结果由子类实现
        finish();
    }

    // ===== 权限相关方法 =====

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
            .setTitle("权限请求")
            .setMessage("此测试需要以下权限，请授予权限以继续测试")
            .setPositiveButton("授予", (d, w) -> {
                permissionLauncher.launch(missingPermissions.toArray(new String[0]));
            })
            .setNegativeButton("取消", (d, w) -> handleTestResult(false))
            .setCancelable(false)
            .show();
    }

    private void showPermissionDeniedDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("以下权限是测试所必需的，但未被授予：\n\n");
        for (String permission : missingPermissions) {
            sb.append("- ").append(getPermissionDisplayName(permission)).append("\n");
        }

        new AlertDialog.Builder(this)
            .setTitle("权限不足")
            .setMessage(sb.toString())
            .setPositiveButton("打开设置", (d, w) -> openAppSettings())
            .setNegativeButton("取消", (d, w) -> handleTestResult(false))
            .setCancelable(false)
            .show();
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
            case Manifest.permission.CAMERA:
                return "相机";
            case Manifest.permission.RECORD_AUDIO:
                return "录音";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "精确定位";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "模糊定位";
            case Manifest.permission.BLUETOOTH:
                return "蓝牙";
            case Manifest.permission.BLUETOOTH_ADMIN:
                return "蓝牙管理";
            case Manifest.permission.ACCESS_WIFI_STATE:
                return "WiFi状态";
            case Manifest.permission.CHANGE_WIFI_STATE:
                return "WiFi控制";
            case Manifest.permission.CALL_PHONE:
                return "打电话";
            case Manifest.permission.RECEIVE_BOOT_COMPLETED:
                return "开机启动";
            default:
                return permission;
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    /**
     * 获取设备ID
     */
    protected String getDeviceUniqueId() {
        return Settings.Secure.getString(
            getContentResolver(),
            Settings.Secure.ANDROID_ID
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
    }
}
