package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * GPS测试
 * 超时120秒，检测GPS功能
 */
public class GpsTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private LinearLayout contentLayout;
    private TextView statusTextView;
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private boolean isLocationEnabled = false;
    private boolean hasLocationPermission = false;
    private boolean isFirstLocationReceived = false;
    private Location lastLocation = null;
    private Handler mainHandler;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        TextView titleView = new TextView(this);
        titleView.setText("GPS测试");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF000000);
        titleView.setPadding(0, 0, 0, 32);
        contentLayout.addView(titleView);

        statusTextView = new TextView(this);
        statusTextView.setText("正在检测GPS...\n");
        statusTextView.setTextSize(16);
        statusTextView.setTextColor(0xFF333333);
        contentLayout.addView(statusTextView);

        setCustomContentView(contentLayout);
    }

    @Override
    protected String getTestName() {
        return "GPS测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查GPS定位功能\n\n操作步骤：\n1. 在空旷室外环境下测试效果更佳\n2. 等待获取GPS信号\n3. 点击PASS或FAIL按钮";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 120;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    @Override
    protected void onTestExecute() {
        checkGpsStatus();
    }

    private void checkGpsStatus() {
        hasLocationPermission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasLocationPermission) {
            updateStatus("定位权限: 未授予\n");
            setPassEnabled(false);
            return;
        }
        updateStatus("定位权限: 已授予\n");

        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            updateStatus("GPS模块: " + (isGpsEnabled ? "已启用" : "已禁用") + "\n");

            isLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            updateStatus("网络定位: " + (isLocationEnabled ? "已启用" : "已禁用") + "\n");

            if (!isGpsEnabled && !isLocationEnabled) {
                updateStatus("\n请开启定位服务后重试\n");
                showLocationSettingsDialog();
                setPassEnabled(false);
                return;
            }

            startLocationUpdates();

        } catch (Exception e) {
            updateStatus("GPS检测失败: " + e.getMessage() + "\n");
            setPassEnabled(false);
        }
    }

    private void startLocationUpdates() {
        updateStatus("\n正在获取位置信息...\n");
        updateStatus("请在空旷环境下等待GPS信号...\n");

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                lastLocation = location;
                isFirstLocationReceived = true;

                String provider = location.getProvider();
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                float accuracy = location.getAccuracy();

                updateStatus("\n=== 位置信息 ===\n");
                updateStatus("定位方式: " + provider + "\n");
                updateStatus("经度: " + lng + "\n");
                updateStatus("纬度: " + lat + "\n");
                updateStatus("精度: " + accuracy + "米\n");

                setPassEnabled(true);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                String statusStr;
                switch (status) {
                    case LocationProvider.AVAILABLE:
                        statusStr = "可用";
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        statusStr = "暂时不可用";
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        statusStr = "服务不可用";
                        break;
                    default:
                        statusStr = "未知";
                }
                updateStatus("GPS状态: " + statusStr + "\n");
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                updateStatus(provider + " 已启用\n");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                updateStatus(provider + " 已禁用\n");
            }
        };

        try {
            if (hasLocationPermission && isGpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, 1,
                    locationListener,
                    Looper.getMainLooper()
                );
            }

            if (hasLocationPermission && isLocationEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000, 1,
                    locationListener,
                    Looper.getMainLooper()
                );
            }

            mainHandler.postDelayed(() -> {
                if (!isFirstLocationReceived) {
                    updateStatus("\n未获取到位置信息，请检查：\n");
                    updateStatus("1. 是否在空旷环境下\n");
                    updateStatus("2. GPS是否启用\n");
                    updateStatus("3. 定位权限是否授予\n");
                }
            }, 30000);

        } catch (Exception e) {
            updateStatus("位置更新请求失败: " + e.getMessage() + "\n");
        }
    }

    private void showLocationSettingsDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("定位服务")
            .setMessage("定位服务已禁用，是否打开设置？")
            .setPositiveButton("打开设置", (d, w) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void updateStatus(String text) {
        if (statusTextView != null) {
            statusTextView.append(text);
        }
    }

    @Override
    protected boolean isPassEnabled() {
        return isFirstLocationReceived;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
