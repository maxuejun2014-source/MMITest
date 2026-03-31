package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * FM测试（简化版）
 * 注意：完整的FM功能需要厂商特定API，此处提供基础测试界面
 */
public class FmTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private TextView statusView;
    private boolean isFmAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);

        // FM需要硬件支持，这里简化处理
        isFmAvailable = true;

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("FM测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n测试FM收音功能\n\n请插入耳机作为天线\n然后手动检查FM是否能正常收音");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        statusView.setText("FM状态: 请手动检查");
        statusView.setTextSize(16);
        statusView.setTextColor(0xFF2196F3);
        statusView.setGravity(Gravity.CENTER);
        layout.addView(statusView);

        Button testBtn = new Button(this);
        testBtn.setText("确认测试");
        testBtn.setOnClickListener(v -> {
            isFmAvailable = true;
            statusView.setText("FM状态: 已确认");
            statusView.setTextColor(0xFF4CAF50);
            setPassEnabled(true);
        });
        layout.addView(testBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n请手动检查FM收音是否正常\n如有异常请点FAIL");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
    }

    @Override
    protected String getTestName() {
        return "FM测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试FM收音功能\n\n操作步骤：\n1. 插入耳机作为天线\n2. 打开系统FM应用检查\n3. 检查收音是否正常";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 45;
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
        return isFmAvailable;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(15, "FM测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
