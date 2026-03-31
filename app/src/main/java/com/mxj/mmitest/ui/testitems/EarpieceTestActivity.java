package com.mxj.mmitest.ui.testitems;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 听筒测试
 */
public class EarpieceTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private AudioManager audioManager;
    private Ringtone ringtone;
    private TextView statusView;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setupContentView();
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        layout.setGravity(Gravity.CENTER);

        TextView titleView = new TextView(this);
        titleView.setText("听筒测试");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF000000);
        layout.addView(titleView);

        TextView descView = new TextView(this);
        descView.setText("\n测试听筒播放\n\n请将手机贴近耳朵聆听");
        descView.setTextSize(14);
        descView.setTextColor(0xFF666666);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        statusView = new TextView(this);
        statusView.setText("状态: 未播放");
        statusView.setTextSize(16);
        statusView.setTextColor(0xFF666666);
        statusView.setGravity(Gravity.CENTER);
        layout.addView(statusView);

        Button playBtn = new Button(this);
        playBtn.setText("播放声音");
        playBtn.setOnClickListener(v -> playSound());
        layout.addView(playBtn);

        Button stopBtn = new Button(this);
        stopBtn.setText("停止");
        stopBtn.setOnClickListener(v -> stopSound());
        layout.addView(stopBtn);

        TextView hintView = new TextView(this);
        hintView.setText("\n请检查听筒是否有声音\n如有异常请点FAIL");
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF999999);
        hintView.setGravity(Gravity.CENTER);
        layout.addView(hintView);

        setCustomContentView(layout);
    }

    private void playSound() {
        try {
            // 使用通知铃声
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) {
                // 切换到听筒模式
                audioManager.setMode(AudioManager.MODE_NORMAL);
                ringtone.play();
                isPlaying = true;
                statusView.setText("状态: 播放中");
                statusView.setTextColor(0xFF4CAF50);
                setPassEnabled(true);
            }
        } catch (Exception e) {
            statusView.setText("状态: 播放失败");
            statusView.setTextColor(0xFFF44336);
            e.printStackTrace();
        }
    }

    private void stopSound() {
        if (ringtone != null) {
            ringtone.stop();
            isPlaying = false;
            statusView.setText("状态: 已停止");
            statusView.setTextColor(0xFF666666);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
    }

    @Override
    protected String getTestName() {
        return "听筒测试";
    }

    @Override
    protected String getTestDescription() {
        return "测试听筒功能\n\n操作步骤：\n1. 点击播放声音按钮\n2. 将手机贴近耳朵检查是否有声音";
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
        setPassEnabled(false);
    }

    @Override
    protected boolean isPassEnabled() {
        return isPlaying;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(13, "听筒测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
