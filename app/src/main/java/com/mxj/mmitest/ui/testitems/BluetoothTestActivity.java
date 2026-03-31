package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙测试
 * 超时30秒，检测蓝牙功能
 */
public class BluetoothTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 20;
    private static final int TIMEOUT_SECONDS = 30;

    private TestRepository repository;
    private LinearLayout contentLayout;
    private TextView statusTextView;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private boolean isBluetoothEnabled = false;
    private boolean isBluetoothSupported = false;
    private List<String> pairedDevices = new ArrayList<>();
    private List<String> discoveredDevices = new ArrayList<>();

    private BroadcastReceiver bluetoothReceiver;
    private Handler mainHandler;
    private boolean isDiscovering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        setupContentView();
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        TextView titleView = new TextView(this);
        titleView.setText("蓝牙测试");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF000000);
        titleView.setPadding(0, 0, 0, 32);
        contentLayout.addView(titleView);

        statusTextView = new TextView(this);
        statusTextView.setText("正在检测蓝牙...\n");
        statusTextView.setTextSize(16);
        statusTextView.setTextColor(0xFF333333);
        contentLayout.addView(statusTextView);

        setContentView(contentLayout);
    }

    @Override
    protected String getTestName() {
        return "蓝牙测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查蓝牙功能\n\n操作步骤：\n1. 确认蓝牙已开启\n2. 检查是否能扫描到蓝牙设备\n3. 点击PASS或FAIL按钮";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            };
        }
    }

    @Override
    protected void onTestExecute() {
        checkBluetoothStatus();
    }

    private void checkBluetoothStatus() {
        try {
            // 检查蓝牙是否支持
            if (bluetoothManager == null) {
                updateStatus("蓝牙: 不支持\n");
                setPassEnabled(false);
                return;
            }

            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                updateStatus("蓝牙适配器: 不可用\n");
                setPassEnabled(false);
                return;
            }

            isBluetoothSupported = true;
            updateStatus("蓝牙: 支持\n");

            // 检查蓝牙状态
            isBluetoothEnabled = bluetoothAdapter.isEnabled();
            updateStatus("蓝牙状态: " + (isBluetoothEnabled ? "已开启" : "已关闭") + "\n");

            if (!isBluetoothEnabled) {
                updateStatus("\n请开启蓝牙后重试\n");
                setPassEnabled(false);
                return;
            }

            // 获取已配对设备
            updateStatus("\n已配对设备:\n");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                if (bondedDevices != null && !bondedDevices.isEmpty()) {
                    for (BluetoothDevice device : bondedDevices) {
                        String name = device.getName();
                        if (name == null || name.isEmpty()) {
                            name = "未知设备";
                        }
                        String info = "  - " + name + " (" + device.getAddress() + ")\n";
                        pairedDevices.add(info);
                        updateStatus(info);
                    }
                } else {
                    updateStatus("  无已配对设备\n");
                }
            }

            // 开始扫描
            startBluetoothDiscovery();

        } catch (Exception e) {
            updateStatus("蓝牙检测失败: " + e.getMessage() + "\n");
            setPassEnabled(false);
        }
    }

    private void startBluetoothDiscovery() {
        if (isDiscovering) {
            return;
        }

        isDiscovering = true;
        updateStatus("\n正在扫描蓝牙设备...\n");

        // 注册广播接收器
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        String name = device.getName();
                        if (name == null || name.isEmpty()) {
                            name = "未知设备";
                        }
                        String info = "  - " + name + "\n";
                        if (!discoveredDevices.contains(info)) {
                            discoveredDevices.add(info);
                            updateStatus(info);
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    updateStatus("扫描开始...\n");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    updateStatus("\n扫描完成\n");
                    isDiscovering = false;
                    finishDiscovery();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        // 开始扫描
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
        }

        // 5秒后自动停止扫描
        mainHandler.postDelayed(() -> {
            if (ActivityCompat.checkSelfPermission(BluetoothTestActivity.this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
            }
        }, 5000);
    }

    private void finishDiscovery() {
        updateStatus("\n检测完成");
        updatePassButtonState();
    }

    private void updateStatus(String text) {
        if (statusTextView != null) {
            statusTextView.append(text);
        }
    }

    private void updatePassButtonState() {
        setPassEnabled(isBluetoothEnabled && isBluetoothSupported);
    }

    @Override
    protected boolean isPassEnabled() {
        return isBluetoothEnabled && isBluetoothSupported;
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothReceiver != null) {
            try {
                unregisterReceiver(bluetoothReceiver);
            } catch (Exception e) {
                // ignore
            }
        }
        if (bluetoothAdapter != null && isDiscovering) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }
}
