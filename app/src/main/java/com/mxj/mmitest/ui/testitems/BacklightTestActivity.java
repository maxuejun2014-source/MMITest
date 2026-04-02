package com.mxj.mmitest.ui.testitems;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

/**
 * 背光测试 - 自动亮度循环 10%-100%
 */
public class BacklightTestActivity extends BaseTestActivity {

    private TestRepository repository;
    private SeekBar mSeekBar;
    private TextView mBrightnessText;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsAutoCycling = true;
    private int mAutoBrightness = 10;
    private boolean mIncreasing = true;

    // 自动亮度循环任务
    private final Runnable mAutoCycleRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsAutoCycling) return;

            if (mIncreasing) {
                mAutoBrightness += 2;
                if (mAutoBrightness >= 100) mIncreasing = false;
            } else {
                mAutoBrightness -= 2;
                if (mAutoBrightness <= 10) mIncreasing = true;
            }

            updateBrightness(mAutoBrightness);
            if (mSeekBar != null) {
                mSeekBar.setProgress(mAutoBrightness);
            }

            mHandler.postDelayed(this, 50);
        }
    };

    // 延迟启用PASS按钮任务
    private final Runnable mEnablePassRunnable = new Runnable() {
        @Override
        public void run() {
            setPassEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);

        TextView textView = new TextView(this);
        textView.setText("背光自动循环测试 (10% - 100%)\n观察亮度自动循环变化:");
        textView.setTextSize(18);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(0, 0, 0, 50);
        layout.addView(textView);

        mSeekBar = new SeekBar(this);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(mAutoBrightness);
        mSeekBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 200));
        mSeekBar.setEnabled(false); // 用户不可点击，仅展示

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsAutoCycling = false;
                    updateBrightness(progress);
                    setPassEnabled(true);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        layout.addView(mSeekBar);

        mBrightnessText = new TextView(this);
        mBrightnessText.setText("亮度: 10%");
        mBrightnessText.setTextSize(16);
        mBrightnessText.setTextColor(0xFF666666);
        mBrightnessText.setGravity(android.view.Gravity.CENTER);
        mBrightnessText.setPadding(0, 20, 0, 0);
        layout.addView(mBrightnessText);

        setCustomContentView(layout);
    }

    /**
     * 更新当前窗口的亮度
     * @param brightnessPercent 亮度百分比 (0-100)
     */
    private void updateBrightness(int brightnessPercent) {
        Activity activity = this;
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightnessPercent / 100.0f;
        activity.getWindow().setAttributes(lp);
        if (mBrightnessText != null) {
            mBrightnessText.setText("亮度: " + brightnessPercent + "%");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsAutoCycling = true;
        mHandler.post(mAutoCycleRunnable);
        mHandler.postDelayed(mEnablePassRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsAutoCycling = false;
        mHandler.removeCallbacks(mAutoCycleRunnable);
        mHandler.removeCallbacks(mEnablePassRunnable);
        // 恢复默认亮度
        updateBrightness(-1);
    }

    @Override
    protected String getTestName() {
        return "背光测试";
    }

    @Override
    protected String getTestDescription() {
        return "背光自动循环测试 (10%-100%)\n\n观察背光是否正常循环变化";
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
        return false; // 延迟启用
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(6, "背光测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
