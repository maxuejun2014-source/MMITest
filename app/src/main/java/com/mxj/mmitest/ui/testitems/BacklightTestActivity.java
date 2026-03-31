package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 背光测试
 */
public class BacklightTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private int currentLevel = 0;
    private int[] brightnessLevels = {0, 50, 100, 150, 200, 255};
    private TextView levelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("背光测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n点击按钮调节背光亮度\n\n检查背光是否正常");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        levelView = new TextView(this);
        levelView.setText("当前亮度: " + brightnessLevels[currentLevel]);
        levelView.setTextSize(24);
        levelView.setTextColor(0xFF333333);
        levelView.setGravity(Gravity.CENTER);
        layout.addView(levelView);

        Button upBtn = new Button(this);
        upBtn.setText("增加亮度");
        upBtn.setOnClickListener(v -> {
            if (currentLevel < brightnessLevels.length - 1) {
                currentLevel++;
                updateBrightness();
            }
        });
        layout.addView(upBtn);

        Button downBtn = new Button(this);
        downBtn.setText("降低亮度");
        downBtn.setOnClickListener(v -> {
            if (currentLevel > 0) {
                currentLevel--;
                updateBrightness();
            }
        });
        layout.addView(downBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n请检查背光调节是否正常\n如有异常请点FAIL");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
        setPassEnabled(true);
    }

    private void updateBrightness() {
        levelView.setText("当前亮度: " + brightnessLevels[currentLevel]);
        try {
            Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightnessLevels[currentLevel]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getTestName() {
        return "背光测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试背光调节功能\n\n操作步骤：\n1. 点击增加/降低亮度按钮\n2. 检查背光是否正常调节";
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
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return true;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(6, "背光测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
