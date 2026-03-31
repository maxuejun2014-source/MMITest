package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * 按键测试
 */
public class ButtonTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private TextView statusView;
    private Map<Integer, Boolean> buttonStates = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);

        // 初始化按键状态
        buttonStates.put(KeyEvent.KEYCODE_VOLUME_UP, false);
        buttonStates.put(KeyEvent.KEYCODE_VOLUME_DOWN, false);
        buttonStates.put(KeyEvent.KEYCODE_POWER, false);
        buttonStates.put(KeyEvent.KEYCODE_HOME, false);
        buttonStates.put(KeyEvent.KEYCODE_BACK, false);

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("按键测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n请依次按下各个按键进行测试\n\n状态：未测试(灰色) / 已通过(绿色)");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        statusView.setText(getButtonStatusText());
        statusView.setTextSize(12);
        statusView.setTextColor(0xFF333333);
        statusView.setPadding(16, 16, 16, 16);
        layout.addView(statusView);

        Button resetBtn = new Button(this);
        resetBtn.setText("重置");
        resetBtn.setOnClickListener(v -> {
            buttonStates.put(KeyEvent.KEYCODE_VOLUME_UP, false);
            buttonStates.put(KeyEvent.KEYCODE_VOLUME_DOWN, false);
            buttonStates.put(KeyEvent.KEYCODE_POWER, false);
            buttonStates.put(KeyEvent.KEYCODE_HOME, false);
            buttonStates.put(KeyEvent.KEYCODE_BACK, false);
            updateStatus();
        });
        layout.addView(resetBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n按下手机上的各个按键\n测试是否正常响应");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
    }

    private String getButtonStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append("音量上键: ").append(buttonStates.get(KeyEvent.KEYCODE_VOLUME_UP) ? "[OK]" : "[--]").append("\n");
        sb.append("音量下键: ").append(buttonStates.get(KeyEvent.KEYCODE_VOLUME_DOWN) ? "[OK]" : "[--]").append("\n");
        sb.append("电源键: ").append(buttonStates.get(KeyEvent.KEYCODE_POWER) ? "[OK]" : "[--]").append("\n");
        sb.append("HOME键: ").append(buttonStates.get(KeyEvent.KEYCODE_HOME) ? "[OK]" : "[--]").append("\n");
        sb.append("返回键: ").append(buttonStates.get(KeyEvent.KEYCODE_BACK) ? "[OK]" : "[--]").append("\n");
        return sb.toString();
    }

    private void updateStatus() {
        statusView.setText(getButtonStatusText());
        checkAllButtons();
    }

    private void checkAllButtons() {
        boolean allPassed = true;
        for (boolean state : buttonStates.values()) {
            if (!state) {
                allPassed = false;
                break;
            }
        }
        setPassEnabled(allPassed);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (buttonStates.containsKey(keyCode)) {
            buttonStates.put(keyCode, true);
            updateStatus();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected String getTestName() {
        return "按键测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试手机按键功能\n\n操作步骤：\n1. 依次按下各个按键\n2. 检查状态显示是否正确";
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
        boolean allPassed = true;
        for (boolean state : buttonStates.values()) {
            if (!state) {
                allPassed = false;
                break;
            }
        }
        return allPassed;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(7, "按键测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
