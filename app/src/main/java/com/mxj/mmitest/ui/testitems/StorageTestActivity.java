package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.Html;
import android.text.format.Formatter;
import android.view.Gravity;
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
    private TextView internalStorageText;
    private TextView sdCardText;
    private ProgressBar internalProgressBar;
    private ProgressBar sdCardProgressBar;
    private TextView internalPercentText;
    private TextView sdCardPercentText;
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
        // 权限授予后读取存储信息
        if (viewsInitialized) {
            readStorageInfo();
        }
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        // 内部存储标题
        TextView internalTitle = new TextView(this);
        internalTitle.setText("手机内部存储");
        internalTitle.setTextSize(18);
        internalTitle.setTextColor(0xFF333333);
        internalTitle.setPadding(0, 20, 0, 10);
        layout.addView(internalTitle);

        // 内部存储进度条
        internalProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        internalProgressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 30));
        layout.addView(internalProgressBar);

        // 内部存储百分比和详情
        internalPercentText = new TextView(this);
        internalPercentText.setTextSize(14);
        internalPercentText.setTextColor(0xFF666666);
        internalPercentText.setPadding(0, 5, 0, 20);
        layout.addView(internalPercentText);

        // 内部存储详情
        internalStorageText = new TextView(this);
        internalStorageText.setTextSize(14);
        internalStorageText.setTextColor(0xFF333333);
        internalStorageText.setPadding(0, 0, 0, 30);
        layout.addView(internalStorageText);

        // SD卡标题
        TextView sdTitle = new TextView(this);
        sdTitle.setText("SD卡");
        sdTitle.setTextSize(18);
        sdTitle.setTextColor(0xFF333333);
        sdTitle.setPadding(0, 20, 0, 10);
        layout.addView(sdTitle);

        // SD卡进度条
        sdCardProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        sdCardProgressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 30));
        layout.addView(sdCardProgressBar);

        // SD卡百分比和详情
        sdCardPercentText = new TextView(this);
        sdCardPercentText.setTextSize(14);
        sdCardPercentText.setTextColor(0xFF666666);
        sdCardPercentText.setPadding(0, 5, 0, 20);
        layout.addView(sdCardPercentText);

        // SD卡详情
        sdCardText = new TextView(this);
        sdCardText.setTextSize(14);
        sdCardText.setTextColor(0xFF333333);
        sdCardText.setPadding(0, 0, 0, 30);
        layout.addView(sdCardText);

        setCustomContentView(layout);
        viewsInitialized = true;

        // 如果权限已经授予，立即读取存储信息
        readStorageInfo();
    }

    private void readStorageInfo() {
        if (!viewsInitialized) return;

        // 获取内部存储信息
        File internalDir = Environment.getExternalStorageDirectory();
        updateInternalStorageInfo(internalDir);

        // 获取SD卡信息
        File sdCard = getPhysicalSdCard(this);
        sdCardInserted = (sdCard != null && sdCard.exists());
        updateSdCardInfo(sdCard);

        // SD卡未插入时PASS按钮不可用
        setPassEnabled(sdCardInserted);
    }

    private void updateInternalStorageInfo(File path) {
        try {
            if (path == null || !path.exists()) {
                internalStorageText.setText("无法读取内部存储");
                internalPercentText.setText("");
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

            internalProgressBar.setMax(100);
            internalProgressBar.setProgress(percent);
            internalPercentText.setText(percent + "% 已使用");
            internalStorageText.setText(Html.fromHtml(
                    String.format("总容量: %s<br/>已使用: %s<br/>可用空间: %s",
                            Formatter.formatFileSize(this, total),
                            Formatter.formatFileSize(this, used),
                            Formatter.formatFileSize(this, available)),
                    Html.FROM_HTML_MODE_COMPACT));

        } catch (Exception e) {
            internalStorageText.setText("读取失败: " + e.getMessage());
            internalPercentText.setText("");
        }
    }

    private void updateSdCardInfo(File path) {
        try {
            if (path == null || !path.exists()) {
                sdCardProgressBar.setMax(100);
                sdCardProgressBar.setProgress(0);
                sdCardPercentText.setText("未插入");
                sdCardText.setText("请插入SD卡进行测试");
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

            sdCardProgressBar.setMax(100);
            sdCardProgressBar.setProgress(percent);
            sdCardPercentText.setText(percent + "% 已使用");
            sdCardText.setText(Html.fromHtml(
                    String.format("总容量: %s<br/>已使用: %s<br/>可用空间: %s",
                            Formatter.formatFileSize(this, total),
                            Formatter.formatFileSize(this, used),
                            Formatter.formatFileSize(this, available)),
                    Html.FROM_HTML_MODE_COMPACT));

        } catch (Exception e) {
            sdCardProgressBar.setMax(100);
            sdCardProgressBar.setProgress(0);
            sdCardPercentText.setText("读取失败");
            sdCardText.setText("读取失败: " + e.getMessage());
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
