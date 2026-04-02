package com.mxj.mmitest.ui.testitems;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.Html;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private TextView storageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupContentView();
        super.onCreate(savedInstanceState);
        repository = TestRepository.getInstance(this);
    }

    private void setupContentView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

        storageTextView = new TextView(this);
        storageTextView.setTextSize(18);
        storageTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        storageTextView.setPadding(50, 50, 50, 50);
        storageTextView.setLineSpacing(0, 1.2f);

        StringBuilder sb = new StringBuilder();

        // 1. 内置存储
        File internalDir = Environment.getExternalStorageDirectory();
        sb.append("<b>[ 内置存储 ]</b><br/>");
        sb.append(getStorageInfo(this, internalDir));
        sb.append("<br/><br/>");

        // 2. 外置 SD 卡
        File sdCard = getPhysicalSdCard(this);
        boolean sdPresent = sdCard != null;
        sb.append("<b>[ 外置 SD 卡 ]</b><br/>");
        if (sdPresent) {
            sb.append("状态: 已插入<br/>");
            sb.append(getStorageInfo(this, sdCard));
        } else {
            sb.append("状态: 未插入");
        }

        storageTextView.setText(Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_COMPACT));

        // 只要内置存储能读取即认为测试正常
        boolean storageOk = internalDir != null && internalDir.exists();
        if (sdPresent) {
            setPassEnabled(true);
        } else {
            // 没有SD卡但内置存储正常也算PASS
            setPassEnabled(storageOk);
        }

        layout.addView(storageTextView);
        setCustomContentView(layout);
    }

    private File getPhysicalSdCard(android.content.Context context) {
        File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
        // dirs[0] 是内置存储的 Android/data/...
        // 如果有外置 SD 卡，dirs[1] 通常是外置卡的路径
        if (dirs != null && dirs.length > 1 && dirs[1] != null) {
            String path = dirs[1].getAbsolutePath();
            if (path.contains("/Android/data")) {
                return new File(path.split("/Android/data")[0]);
            }
            return dirs[1];
        }
        return null;
    }

    private String getStorageInfo(android.content.Context context, File path) {
        try {
            if (path == null || !path.exists()) return "无法读取路径";
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            long total = totalBlocks * blockSize;
            long available = availableBlocks * blockSize;
            long used = total - available;

            int percent = total > 0 ? (int) (used * 100 / total) : 0;

            return String.format("总容量: %s<br/>" +
                    "已使用: %s (%d%%)<br/>" +
                    "可用空间: %s",
                    Formatter.formatFileSize(context, total),
                    Formatter.formatFileSize(context, used),
                    percent,
                    Formatter.formatFileSize(context, available));
        } catch (Exception e) {
            return "读取存储信息失败: " + e.getMessage();
        }
    }

    @Override
    protected String getTestName() {
        return "存储测试";
    }

    @Override
    protected String getTestDescription() {
        return "检查内置存储和外置SD卡状态\n\n请检查存储信息是否正常显示";
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
        return true;
    }

    @Override
    protected void finishAndSaveResult(boolean passed) {
        repository.saveSingleTestResultSync(2, "存储测试", passed, getDeviceUniqueId());
        super.finishAndSaveResult(passed);
    }
}
