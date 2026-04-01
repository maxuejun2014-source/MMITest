package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * SIM卡测试
 */
public class SimTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private TelephonyManager telephonyManager;
    private LinearLayout contentLayout;
    private boolean hasSim = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        repository = TestRepository.getInstance(this);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        setupContentView();

        super.onCreate(savedInstanceState);
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        updateSimDisplay();

        setContentView(contentLayout);
    }

    private void updateSimDisplay() {
        contentLayout.removeAllViews();

        TextView titleView = new TextView(this);
        titleView.setText("SIM卡检测");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        contentLayout.addView(titleView);

        try {
            String simState = telephonyManager.getSimState() + "";
            int simStateInt = telephonyManager.getSimState();

            TextView stateView = new TextView(this);
            stateView.setText("\nSIM卡状态: " + getSimStateText(simStateInt));
            stateView.setTextSize(16);
            stateView.setTextColor(0xFF333333);
            contentLayout.addView(stateView);

            // 检查是否有SIM卡
            if (simStateInt == TelephonyManager.SIM_STATE_READY) {
                hasSim = true;

                TextView operatorView = new TextView(this);
                String operatorName = telephonyManager.getSimOperatorName();
                operatorView.setText("\n运营商: " + (operatorName != null ? operatorName : "未知"));
                operatorView.setTextSize(14);
                operatorView.setTextColor(0xFF333333);
                contentLayout.addView(operatorView);

                TextView infoView = new TextView(this);
                infoView.setText("\nSIM卡正常识别，可以进行通话和上网");
                infoView.setTextSize(14);
                infoView.setTextColor(0xFF4CAF50);
                contentLayout.addView(infoView);
            } else {
                hasSim = false;
                TextView noSimView = new TextView(this);
                noSimView.setText("\n未检测到SIM卡或SIM卡不可用\n请检查SIM卡是否正确插入");
                noSimView.setTextSize(16);
                noSimView.setTextColor(0xFFF44336);
                contentLayout.addView(noSimView);
            }
        } catch (SecurityException e) {
            hasSim = false;
            TextView errorView = new TextView(this);
            errorView.setText("\n需要READ_PHONE_STATE权限\n请授予权限后重试");
            errorView.setTextSize(14);
            errorView.setTextColor(0xFFF44336);
            contentLayout.addView(errorView);
        } catch (Exception e) {
            hasSim = false;
            TextView errorView = new TextView(this);
            errorView.setText("\n检测失败: " + e.getMessage());
            errorView.setTextSize(14);
            errorView.setTextColor(0xFFF44336);
            contentLayout.addView(errorView);
        }

        setPassEnabled(hasSim);
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

    @Override
    protected String getTestName() {
        return "SIM卡测试";
    }

    @Override
    protected String getTestDescription() {
        return "请检查SIM卡是否正常识别\n\n操作步骤：\n1. 确认SIM卡已插入\n2. 检查信号强度\n3. 点击PASS或FAIL按钮";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 15;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.READ_PHONE_STATE};
    }

    @Override
    protected void onTestExecute() {
        updateSimDisplay();
    }

    @Override
    protected boolean isPassEnabled() {
        return hasSim;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(1, "SIM卡测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
