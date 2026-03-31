package com.mxj.mmitest.ui.testitems;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 耳机测试
 */
public class HeadphoneTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private AudioManager audioManager;
    private Ringtone ringtone;
    private TextView statusView;
    private boolean isHeadphoneConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        checkHeadphoneStatus();
        setupContentView();
    }

    private void checkHeadphoneStatus() {
        isHeadphoneConnected = audioManager.isWiredHeadsetOn();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("耳机测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n请插入耳机进行测试\n\n检测耳机是否正常连接");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        updateStatusView();
        statusView.setTextSize(16);
        statusView.setGravity(Gravity.CENTER);
        layout.addView(statusView);

        Button playBtn = new Button(this);
        playBtn.setText("播放测试声音");
        playBtn.setOnClickListener(v -> playSound());
        layout.addView(playBtn);

        Button stopBtn = new Button(this);
        stopBtn.setText("停止");
        stopBtn.setOnClickListener(v -> stopSound());
        layout.addView(stopBtn);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("刷新状态");
        refreshBtn.setOnClickListener(v -> {
            checkHeadphoneStatus();
            updateStatusView();
        });
        layout.addView(refreshBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n请检查耳机是否有声音\n如有异常请点FAIL");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
    }

    private void updateStatusView() {
        checkHeadphoneStatus();
        if (isHeadphoneConnected) {
            statusView.setText("耳机状态: 已连接");
            statusView.setTextColor(0xFF4CAF50);
            setPassEnabled(true);
        } else {
            statusView.setText("耳机状态: 未连接\n请插入耳机");
            statusView.setTextColor(0xFFFF9800);
            setPassEnabled(false);
        }
    }

    private void playSound() {
        if (!isHeadphoneConnected) {
            statusView.setText("请先插入耳机");
            statusView.setTextColor(0xFFF44336);
            return;
        }
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSound() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
    }

    @Override
    protected String getTestName() {
        return "耳机测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试耳机功能\n\n操作步骤：\n1. 插入耳机\n2. 点击播放测试声音\n3. 检查耳机是否有声音";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 20;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return null;
    }

    @Override
    protected void onTestExecute() {
        setPassEnabled(isHeadphoneConnected);
    }

    @Override
    protected boolean isPassEnabled() {
        return isHeadphoneConnected;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(14, "耳机测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
