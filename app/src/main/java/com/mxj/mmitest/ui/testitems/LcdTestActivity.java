package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * LCD测试
 */
public class LcdTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private View colorView;
    private int currentColorIndex = 0;
    private int[] colors = {0xFFFFFFFF, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF000000};
    private String[] colorNames = {"白色", "红色", "绿色", "蓝色", "黑色"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("LCD测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n点击按钮切换纯色显示\n\n检查屏幕是否有坏点或异常");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        colorView = new View(this);
        colorView.setBackgroundColor(colors[0]);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 400);
        params.setMargins(0, 32, 0, 32);
        colorView.setLayoutParams(params);
        layout.addView(colorView);

        TextView colorLabel = new TextView(this);
        colorLabel.setText("当前: " + colorNames[0]);
        colorLabel.setTextSize(16);
        colorLabel.setTextColor(0xFF333333);
        colorLabel.setGravity(Gravity.CENTER);
        layout.addView(colorLabel);

        Button nextBtn = new Button(this);
        nextBtn.setText("切换颜色");
        nextBtn.setOnClickListener(v -> {
            currentColorIndex = (currentColorIndex + 1) % colors.length;
            colorView.setBackgroundColor(colors[currentColorIndex]);
            colorLabel.setText("当前: " + colorNames[currentColorIndex]);
        });
        layout.addView(nextBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n请仔细检查屏幕显示是否正常\n如有异常请点FAIL");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
        setPassEnabled(true);
    }

    @Override
    protected String getTestName() {
        return "LCD测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试LCD显示屏功能\n\n操作步骤：\n1. 点击切换颜色按钮\n2. 检查屏幕显示是否正常";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 60;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return null;
    }

    @Override
    protected void onTestExecute() {
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return true;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(5, "LCD测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
