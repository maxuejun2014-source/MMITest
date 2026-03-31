package com.mxj.mmitest.ui.testitems;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * OTG测试
 */
public class OtgTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private UsbManager usbManager;
    private TextView statusView;
    private boolean isOtgConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("OTG测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n测试OTG功能\n\n请连接OTG设备(U盘、鼠标等)");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        statusView.setText("OTG状态: 未检测");
        statusView.setTextSize(16);
        statusView.setTextColor(0xFF666666);
        statusView.setGravity(Gravity.CENTER);
        layout.addView(statusView);

        Button checkBtn = new Button(this);
        checkBtn.setText("检测OTG设备");
        checkBtn.setOnClickListener(v -> checkOtgDevice());
        layout.addView(checkBtn);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("刷新状态");
        refreshBtn.setOnClickListener(v -> {
            checkOtgDevice();
        });
        layout.addView(refreshBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n请连接OTG设备进行测试\n如有异常请点FAIL");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
    }

    private void checkOtgDevice() {
        if (usbManager == null) {
            statusView.setText("OTG状态: 不可用\n(USB服务为空)");
            statusView.setTextColor(0xFFF44336);
            isOtgConnected = false;
            setPassEnabled(false);
            return;
        }

        // 检查USB设备列表
        int deviceCount = usbManager.getDeviceList().size();
        if (deviceCount > 0) {
            isOtgConnected = true;
            statusView.setText("OTG状态: 已连接\n设备数量: " + deviceCount);
            statusView.setTextColor(0xFF4CAF50);
            setPassEnabled(true);
        } else {
            isOtgConnected = false;
            statusView.setText("OTG状态: 未连接\n请插入OTG设备");
            statusView.setTextColor(0xFFFF9800);
            setPassEnabled(false);
        }
    }

    @Override
    protected String getTestName() {
        return "OTG测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试OTG功能\n\n操作步骤：\n1. 连接OTG设备\n2. 点击检测按钮\n3. 检查是否能识别设备";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return null;
    }

    @Override
    protected void onTestExecute() {
        setPassEnabled(false);
    }

    @Override
    protected boolean isPassEnabled() {
        return isOtgConnected;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(22, "OTG测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
