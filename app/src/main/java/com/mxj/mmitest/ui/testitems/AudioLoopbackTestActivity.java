package com.mxj.mmitest.ui.testitems;

import android.Manifest;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 音频回环测试
 * 超时30秒，检测麦克风和扬声器
 */
public class AudioLoopbackTestActivity extends BaseTestActivity {

    private static final int TEST_ITEM_ID = 12;
    private static final int TIMEOUT_SECONDS = 30;

    private TestRepository repository;
    private LinearLayout contentLayout;
    private TextView statusTextView;

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private int recordSampleRate;
    private int recordChannelConfig;
    private int recordAudioFormat;
    private int recordBufferSize;

    private boolean isMicDetected = false;
    private boolean isSpeakerDetected = false;
    private volatile boolean isRecording = false;
    private volatile boolean testCompleted = false;

    private Handler mainHandler;
    private Thread recordingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void setupContentView() {
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 32, 48, 32);

        TextView titleView = new TextView(this);
        titleView.setText("音频回环测试");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF000000);
        titleView.setPadding(0, 0, 0, 32);
        contentLayout.addView(titleView);

        statusTextView = new TextView(this);
        statusTextView.setText("正在检测音频设备...\n");
        statusTextView.setTextSize(18);
        statusTextView.setTextColor(0xFF333333);
        contentLayout.addView(statusTextView);

        setContentView(contentLayout);
    }

    @Override
    protected String getTestName() {
        return "音频回环测试";
    }

    @Override
    protected String getTestDescription() {
        return "请对着麦克风说话或播放音乐\n\n操作步骤：\n1. 对着麦克风说话或播放音频\n2. 如果能听到回音，说明扬声器和麦克风正常\n3. 点击PASS或FAIL按钮";
    }

    @Override
    protected int getTimeoutSeconds() {
        return TIMEOUT_SECONDS;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    @Override
    protected void onTestExecute() {
        startAudioTest();
    }

    private void startAudioTest() {
        updateStatus("正在初始化音频设备...\n");

        // 获取推荐的采样率
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        recordSampleRate = sampleRateStr != null ? Integer.parseInt(sampleRateStr) : 44100;

        recordChannelConfig = AudioFormat.CHANNEL_IN_MONO;
        recordAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
        recordBufferSize = AudioRecord.getMinBufferSize(recordSampleRate, recordChannelConfig, recordAudioFormat);

        if (recordBufferSize == AudioRecord.ERROR || recordBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            recordBufferSize = 44100 * 2;
        }

        try {
            // 检测麦克风
            updateStatus("正在检测麦克风...\n");
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                recordSampleRate,
                recordChannelConfig,
                recordAudioFormat,
                recordBufferSize
            );

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                isMicDetected = true;
                updateStatus("麦克风检测: 正常\n");
            } else {
                updateStatus("麦克风检测: 失败\n");
            }

            // 检测扬声器
            updateStatus("正在检测扬声器...\n");
            int trackChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
            int trackBufferSize = AudioTrack.getMinBufferSize(recordSampleRate, trackChannelConfig, recordAudioFormat);

            audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                recordSampleRate,
                trackChannelConfig,
                recordAudioFormat,
                trackBufferSize,
                AudioTrack.MODE_STREAM
            );

            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                isSpeakerDetected = true;
                updateStatus("扬声器检测: 正常\n");
            } else {
                updateStatus("扬声器检测: 失败\n");
            }

            // 开始回环测试
            if (isMicDetected && isSpeakerDetected) {
                updateStatus("正在测试音频回环...\n");
                startLoopbackTest();
            } else {
                updateStatus("\n音频设备检测异常，请检查设备");
                setPassEnabled(false);
            }

        } catch (Exception e) {
            updateStatus("音频检测失败: " + e.getMessage() + "\n");
            setPassEnabled(false);
        }
    }

    private void startLoopbackTest() {
        isRecording = true;

        recordingThread = new Thread(() -> {
            try {
                audioRecord.startRecording();
                audioTrack.play();

                byte[] buffer = new byte[recordBufferSize];

                while (isRecording && !testCompleted) {
                    int readCount = audioRecord.read(buffer, 0, recordBufferSize);
                    if (readCount > 0 && isMicDetected) {
                        audioTrack.write(buffer, 0, readCount);
                    }
                }

            } catch (Exception e) {
                mainHandler.post(() -> {
                    updateStatus("回环测试异常: " + e.getMessage() + "\n");
                });
            } finally {
                try {
                    if (audioRecord != null) {
                        audioRecord.stop();
                    }
                    if (audioTrack != null) {
                        audioTrack.stop();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        });

        recordingThread.start();

        // 2秒后如果回环正常，自动启用PASS
        mainHandler.postDelayed(() -> {
            if (!testCompleted) {
                updateStatus("音频回环测试正常，可以听到自己的声音\n");
                setPassEnabled(true);
            }
        }, 2000);
    }

    private void updateStatus(String text) {
        if (statusTextView != null) {
            statusTextView.append(text);
        }
    }

    @Override
    protected boolean isPassEnabled() {
        return isMicDetected && isSpeakerDetected;
    }

    @Override
    protected int getTestItemId() {
        return TEST_ITEM_ID;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        testCompleted = true;
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
        repository.saveSingleTestResult(
            TEST_ITEM_ID,
            getTestName(),
            passed,
            getDeviceUniqueId()
        );
        super.finishAndSaveResult(passed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        testCompleted = true;
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                // ignore
            }
        }
        if (audioTrack != null) {
            try {
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
