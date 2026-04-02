package com.mxj.mmitest.ui.testitems;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseTestActivity;

import java.io.File;

/**
 * 存储测试 - 显示内置存储和外置SD卡详细信息
 */
public class StorageTestActivity extends BaseTestActivity {

    private TestRepository repository;

    private LinearLayout internalCard;
    private LinearLayout sdCardCard;
    private ProgressBar internalProgressBar;
    private ProgressBar sdCardProgressBar;
    private TextView internalPercentText;
    private TextView sdCardPercentText;
    private TextView internalStatusText;
    private TextView sdCardStatusText;
    private TextView internalDetailsText;
    private TextView sdCardDetailsText;

    private boolean sdCardInserted = false;
    private boolean viewsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
        setupContentView();
    }

    @Override
    protected void onPermissionsGranted() {
        if (viewsInitialized) {
            readStorageInfo();
        }
    }

    private void setupContentView() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        // ===== 内部存储卡片 =====
        internalCard = createStorageCard("📱 手机内部存储", Color.parseColor("#2196F3"));
        mainLayout.addView(internalCard);

        // ===== SD卡卡片 =====
        sdCardCard = createStorageCard("💾 SD卡", Color.parseColor("#4CAF50"));
        mainLayout.addView(sdCardCard);

        setCustomContentView(mainLayout);
        viewsInitialized = true;

        readStorageInfo();
    }

    private LinearLayout createStorageCard(String title, int accentColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        card.setGravity(Gravity.CENTER_HORIZONTAL);

        // 卡片背景
        int cardBackground = Color.parseColor("#F5F5F5");
        card.setBackgroundColor(cardBackground);

        // 标题行
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        // 状态指示点
        View statusDot = new View(this);
        statusDot.setLayoutParams(new LinearLayout.LayoutParams(12, 12));
        statusDot.setBackgroundColor(accentColor);

        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(18);
        titleText.setTextColor(Color.parseColor("#333333"));
        titleText.setPadding(12, 0, 0, 0);

        titleRow.addView(statusDot);
        titleRow.addView(titleText);

        // 进度条容器
        LinearLayout progressContainer = new LinearLayout(this);
        progressContainer.setOrientation(LinearLayout.VERTICAL);
        progressContainer.setPadding(0, 20, 0, 10);

        // 进度条
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20));

        // 进度条颜色
        progressBar.setProgressDrawable(ContextCompat.getDrawable(this,
                android.R.drawable.progress_horizontal));

        // 百分比文本
        TextView percentText = new TextView(this);
        percentText.setTextSize(24);
        percentText.setTextColor(accentColor);
        percentText.setGravity(Gravity.CENTER);
        percentText.setPadding(0, 10, 0, 5);

        // 详情文本
        TextView detailsText = new TextView(this);
        detailsText.setTextSize(14);
        detailsText.setTextColor(Color.parseColor("#666666"));
        detailsText.setGravity(Gravity.CENTER);

        // 状态文本
        TextView statusText = new TextView(this);
        statusText.setTextSize(12);
        statusText.setTextColor(Color.parseColor("#999999"));
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 5, 0, 0);

        progressContainer.addView(progressBar);
        progressContainer.addView(percentText);
        progressContainer.addView(detailsText);
        progressContainer.addView(statusText);

        card.addView(titleRow);
        card.addView(progressContainer);

        // 保存引用
        if (internalCard == null) {
            // 内部存储
            internalProgressBar = progressBar;
            internalPercentText = percentText;
            internalDetailsText = detailsText;
            internalStatusText = statusText;
        } else {
            // SD卡
            sdCardProgressBar = progressBar;
            sdCardPercentText = percentText;
            sdCardDetailsText = detailsText;
            sdCardStatusText = statusText;
        }

        return card;
    }

    private void readStorageInfo() {
        if (!viewsInitialized) return;

        // 获取内部存储信息
        File internalDir = Environment.getExternalStorageDirectory();
        updateStorageInfo(internalDir, internalProgressBar, internalPercentText,
                internalDetailsText, internalStatusText, true);

        // 获取SD卡信息
        File sdCard = getPhysicalSdCard(this);
        sdCardInserted = (sdCard != null && sdCard.exists());
        updateStorageInfo(sdCard, sdCardProgressBar, sdCardPercentText,
                sdCardDetailsText, sdCardStatusText, false);

        // SD卡未插入时PASS按钮不可用
        setPassEnabled(sdCardInserted);
    }

    private void updateStorageInfo(File path, ProgressBar progressBar,
                                   TextView percentText, TextView detailsText,
                                   TextView statusText, boolean isInternal) {
        try {
            if (path == null || !path.exists()) {
                if (isInternal) {
                    percentText.setText("--%");
                    detailsText.setText("无法读取内部存储");
                    statusText.setText("");
                } else {
                    percentText.setText("--%");
                    detailsText.setText("SD卡未插入");
                    statusText.setText("请插入SD卡进行测试");
                    statusText.setTextColor(Color.parseColor("#F44336"));
                }
                progressBar.setProgress(0);
                return;
            }

            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            long total = totalBlocks * blockSize;
            long available = availableBlocks * blockSize;
            long used = total - available;
            int percent = total > 0 ? (int) (used * 100 / total) : 0;

            progressBar.setMax(100);
            progressBar.setProgress(percent);
            percentText.setText(percent + "%");
            detailsText.setText(String.format("总容量: %s | 已用: %s | 可用: %s",
                    Formatter.formatFileSize(this, total),
                    Formatter.formatFileSize(this, used),
                    Formatter.formatFileSize(this, available)));

            if (isInternal) {
                statusText.setText("内部存储正常");
                statusText.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                statusText.setText("SD卡已识别");
                statusText.setTextColor(Color.parseColor("#4CAF50"));
            }

        } catch (Exception e) {
            percentText.setText("--%");
            detailsText.setText("读取失败: " + e.getMessage());
            statusText.setText("");
            progressBar.setProgress(0);
        }
    }

    private File getPhysicalSdCard(android.content.Context context) {
        File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
        if (dirs != null && dirs.length > 1 && dirs[1] != null) {
            String path = dirs[1].getAbsolutePath();
            if (path.contains("/Android/data")) {
                return new File(path.split("/Android/data")[0]);
            }
            return dirs[1];
        }
        return null;
    }

    @Override
    protected String getTestName() {
        return "存储测试";
    }

    @Override
    protected String getTestDescription() {
        return "检查手机内部存储和SD卡状态\n\n请检查存储信息是否正常显示";
    }

    @Override
    protected int getTimeoutSeconds() {
        return 30;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    protected void onTestExecute() {
        // 测试执行
    }

    @Override
    protected boolean isPassEnabled() {
        return sdCardInserted;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(2, "存储测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
