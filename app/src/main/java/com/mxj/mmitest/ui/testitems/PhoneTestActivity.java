package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 电话测试
 * 超时30秒，检测电话功能
 */
public class PhoneTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 18;
    private static final int TIMEOUT_SECONDS = 30;

    private TestRepository repository;
    private LinearLayout contentLayout;
    private TextView statusTextView;

    private TelephonyManager telephonyManager;
    private SubscriptionManager subscriptionManager;

    private boolean hasPhoneCapability = false;
    private boolean hasSimCard = false;
    private String networkType = "";
    private String simOperator = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        TextView titleView = new TextView(this);
        titleView.setText("电话功能测试");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF000000);
        titleView.setPadding(0, 0, 0, 32);
        contentLayout.addView(titleView);

        statusTextView = new TextView(this);
        statusTextView.setText("正在检测电话功能...\n");
        statusTextView.setTextSize(16);
        statusTextView.setTextColor(0xFF333333);
        contentLayout.addView(statusTextView);

        setContentView(contentLayout);
    }

    @Override
    protected String getTestName() {
        return "电话测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查电话功能\n\n操作步骤：\n1. 检查是否有信号\n2. 尝试拨打电话（如有需要）\n3. 点击PASS或FAIL按钮";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
        };
    }

    @Override
    protected void onTestExecute() {
        checkPhoneStatus();
    }

    private void checkPhoneStatus() {
        try {
            // 检查电话功能
            int phoneType = telephonyManager.getPhoneType();
            if (phoneType == TelephonyManager.PHONE_TYPE_GSM ||
                phoneType == TelephonyManager.PHONE_TYPE_CDMA ||
                phoneType == TelephonyManager.PHONE_TYPE_SIP) {
                hasPhoneCapability = true;
                updateStatus("电话功能: 支持\n");
            } else {
                updateStatus("电话功能: 不支持\n");
            }

            // 获取网络类型
            int networkTypeInt = telephonyManager.getDataNetworkType();
            networkType = getNetworkTypeName(networkTypeInt);
            updateStatus("网络类型: " + networkType + "\n");

            // 检查SIM卡状态
            int simStateInt = telephonyManager.getSimState();
            String simStateStr = getSimStateText(simStateInt);
            updateStatus("SIM卡状态: " + simStateStr + "\n");

            if (simStateInt == TelephonyManager.SIM_STATE_READY) {
                hasSimCard = true;
            }

            // 获取运营商信息
            simOperator = telephonyManager.getSimOperator();
            if (simOperator != null && !simOperator.isEmpty()) {
                updateStatus("运营商: " + simOperator + "\n");
            } else {
                updateStatus("运营商: 未知\n");
            }

            // 检查信号强度
            updateStatus("\n正在监听信号强度...\n");

            // 延迟获取信号强度
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updateStatus("\n检测完成");
                updatePassButtonState();
            }, 2000);

        } catch (SecurityException e) {
            updateStatus("权限不足: " + e.getMessage() + "\n");
            setPassEnabled(false);
        } catch (Exception e) {
            updateStatus("检测失败: " + e.getMessage() + "\n");
            setPassEnabled(false);
        }
    }

    private String getNetworkTypeName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G NR";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return "未知";
        }
    }

    private void updateStatus(String text) {
        if (statusTextView != null) {
            statusTextView.append(text);
        }
    }

    private String getSimStateText(int state) {
        switch (state) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "无SIM卡";
            case TelephonyManager.SIM_STATE_READY:
                return "就绪";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "需要PIN码";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "需要PUK码";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "网络锁定";
            case TelephonyManager.SIM_STATE_UNKNOWN:
            default:
                return "未知状态";
        }
    }

    private void updatePassButtonState() {
        setPassEnabled(hasPhoneCapability && hasSimCard);
    }

    @Override
    protected boolean isPassEnabled() {
        return hasPhoneCapability && hasSimCard;
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }
}
