package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;
import java.util.List;

/**
 * WiFi测试
 * 超时30秒，检测WiFi功能
 */
public class WifiTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 19;
    private static final int TIMEOUT_SECONDS = 30;

    private TestRepository repository;
    private LinearLayout contentLayout;
    private TextView statusTextView;

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    private boolean isWifiEnabled = false;
    private boolean isWifiConnected = false;
    private String wifiSSID = "";
    private int wifiSignalStrength = 0;
    private int wifiLinkSpeed = 0;

    private ConnectivityManager.NetworkCallback networkCallback;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        TextView titleView = new TextView(this);
        titleView.setText("WiFi测试");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF000000);
        titleView.setPadding(0, 0, 0, 32);
        contentLayout.addView(titleView);

        statusTextView = new TextView(this);
        statusTextView.setText("正在检测WiFi...\n");
        statusTextView.setTextSize(16);
        statusTextView.setTextColor(0xFF333333);
        contentLayout.addView(statusTextView);

        setContentView(contentLayout);
    }

    @Override
    protected String getTestName() {
        return "WiFi测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查WiFi功能\n\n操作步骤：\n1. 确认WiFi已开启\n2. 检查是否能扫描到WiFi网络\n3. 点击PASS或FAIL按钮";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        };
    }

    @Override
    protected void onTestExecute() {
        checkWifiStatus();
    }

    private void checkWifiStatus() {
        try {
            // 检查WiFi状态
            isWifiEnabled = wifiManager.isWifiEnabled();
            updateStatus("WiFi状态: " + (isWifiEnabled ? "已开启" : "已关闭") + "\n");

            if (!isWifiEnabled) {
                updateStatus("\n请开启WiFi后重试\n");
                setPassEnabled(false);
                return;
            }

            // 获取WiFi连接信息
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiSSID = wifiInfo.getSSID();
                if (wifiSSID != null) {
                    // 移除SSID两端的引号
                    if (wifiSSID.startsWith("\"") && wifiSSID.endsWith("\"")) {
                        wifiSSID = wifiSSID.substring(1, wifiSSID.length() - 1);
                    }
                }

                wifiSignalStrength = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
                wifiLinkSpeed = wifiInfo.getLinkSpeed();

                updateStatus("当前网络: " + wifiSSID + "\n");
                updateStatus("信号强度: " + getSignalLevelString(wifiSignalStrength) + "\n");
                updateStatus("连接速度: " + wifiLinkSpeed + " Mbps\n");

                if (!"<unknown ssid>".equals(wifiSSID) && wifiSSID.length() > 0) {
                    isWifiConnected = true;
                }
            }

            // 扫描周围的WiFi网络
            updateStatus("\n正在扫描WiFi网络...\n");

            @SuppressWarnings("unchecked")
            List<ScanResult> scanResults = wifiManager.getScanResults();
            if (scanResults != null && !scanResults.isEmpty()) {
                updateStatus("扫描到 " + scanResults.size() + " 个WiFi网络:\n");
                int count = 0;
                for (ScanResult result : scanResults) {
                    if (count >= 5) {
                        updateStatus("... 还有 " + (scanResults.size() - 5) + " 个网络\n");
                        break;
                    }
                    String scanSSID = result.SSID;
                    if (scanSSID == null || scanSSID.isEmpty()) {
                        scanSSID = "<隐藏网络>";
                    }
                    int signal = WifiManager.calculateSignalLevel(result.level, 5);
                    updateStatus("  - " + scanSSID + " (" + getSignalLevelString(signal) + ")\n");
                    count++;
                }
            } else {
                updateStatus("未扫描到WiFi网络\n");
            }

            updateStatus("\n检测完成");
            updatePassButtonState();

        } catch (Exception e) {
            updateStatus("WiFi检测失败: " + e.getMessage() + "\n");
            setPassEnabled(false);
        }
    }

    private String getSignalLevelString(int level) {
        switch (level) {
            case 0:
                return "无信号";
            case 1:
                return "弱";
            case 2:
                return "较弱";
            case 3:
                return "中等";
            case 4:
                return "强";
            default:
                return "未知";
        }
    }

    private void updateStatus(String text) {
        if (statusTextView != null) {
            statusTextView.append(text);
        }
    }

    private void updatePassButtonState() {
        setPassEnabled(isWifiEnabled);
    }

    @Override
    protected boolean isPassEnabled() {
        return isWifiEnabled;
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
