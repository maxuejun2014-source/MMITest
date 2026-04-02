package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

import java.util.List;

/**
 * WiFi测试 - 扫描并显示WiFi网络
 */
public class WifiTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 19;
    private static final int TIMEOUT_SECONDS = 30;

    private TestRepository repository;
    private TextView mStatusView;
    private WifiManager mWifiManager;
    private boolean mIsReceiverRegistered = false;

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = mWifiManager.getScanResults();
            if (results != null && !results.isEmpty()) {
                mStatusView.setText("Wi-Fi Scan Success!\nFound " + results.size() + " networks.");
                setPassEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        mStatusView = new TextView(this);
        mStatusView.setTextSize(20);
        mStatusView.setGravity(Gravity.CENTER);
        mStatusView.setPadding(40, 40, 40, 40);
        mStatusView.setText("Initializing Wi-Fi test...");

        layout.addView(mStatusView);
        setCustomContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doWifiScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsReceiverRegistered) {
            try {
                unregisterReceiver(mWifiReceiver);
            } catch (Exception e) {
            }
            mIsReceiverRegistered = false;
        }
    }

    private void doWifiScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mStatusView.setText("Permission required: Location\n\nPlease grant location permission for WiFi scan.");
            setPassEnabled(false);
            return;
        }
        if (mWifiManager != null && !mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiReceiver, filter);
        mIsReceiverRegistered = true;
        mWifiManager.startScan();
        mStatusView.setText("Scanning Wi-Fi networks...");
    }

    @Override
    protected String getTestName() {
        return "WiFi测试";
    }

    @Override
    protected String getTestDescription() {
        return "WiFi扫描测试\n\n检查是否能扫描到WiFi网络";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    @Override
    protected void onTestExecute() {
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return false; // 由WiFi扫描结果决定
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsReceiverRegistered) {
            try {
                unregisterReceiver(mWifiReceiver);
            } catch (Exception e) {
            }
            mIsReceiverRegistered = false;
        }
    }
}
