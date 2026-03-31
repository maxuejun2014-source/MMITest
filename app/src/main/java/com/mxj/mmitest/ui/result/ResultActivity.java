package com.mxj.mmitest.ui.result;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mxj.mmitest.R;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.domain.model.TestResultSummary;
import com.mxj.mmitest.domain.model.TestStatistics;
import com.mxj.mmitest.ui.base.BaseActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果查看界面
 */
public class ResultActivity extends BaseActivity {

    private ResultViewModel viewModel;
    private TabHost tabHost;
    private ListView historyListView;
    private LinearLayout statisticsLayout;
    private ImageView qrCodeImage;
    private ProgressBar progressBar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        viewModel = new ViewModelProvider(this).get(ResultViewModel.class);
        viewModel.setRepository(TestRepository.getInstance(this));

        initViews();
        setupTabs();
        observeViewModel();
        viewModel.loadSessions();
    }

    private void initViews() {
        tabHost = findViewById(R.id.tab_host);
        historyListView = findViewById(R.id.history_list);
        statisticsLayout = findViewById(R.id.statistics_layout);
        qrCodeImage = findViewById(R.id.qr_code_image);
        progressBar = findViewById(R.id.progress_bar);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("历史记录");
        spec1.setContent(R.id.tab_history);
        spec1.setIndicator("历史记录");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("统计");
        spec2.setContent(R.id.tab_statistics);
        spec2.setIndicator("统计");
        tabHost.addTab(spec2);

        TabHost.TabSpec spec3 = tabHost.newTabSpec("二维码");
        spec3.setContent(R.id.tab_qr_code);
        spec3.setIndicator("二维码");
        tabHost.addTab(spec3);

        tabHost.setOnTabChangedListener(tabId -> {
            if ("统计".equals(tabId)) {
                viewModel.setTab(ResultViewModel.ResultTab.STATISTICS);
            } else if ("二维码".equals(tabId)) {
                viewModel.setTab(ResultViewModel.ResultTab.QR_CODE);
            } else {
                viewModel.setTab(ResultViewModel.ResultTab.HISTORY);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getSessions().observe(this, this::updateHistoryList);

        viewModel.getStatistics().observe(this, this::updateStatistics);

        viewModel.getQrCodeData().observe(this, this::updateQrCode);

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void updateHistoryList(List<TestResultSummary> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            historyListView.setAdapter(null);
            return;
        }

        List<Map<String, String>> data = new ArrayList<>();
        for (TestResultSummary session : sessions) {
            Map<String, String> item = new HashMap<>();
            item.put("device", session.getDeviceModel());
            item.put("time", dateFormat.format(new Date(session.getStartTime())));
            item.put("result", session.getPassedCount() + "/" + session.getTotalCount());
            item.put("status", session.isAllPassed() ? "PASS" : "FAIL");
            item.put("sessionId", session.getSessionId());
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item_history,
            new String[]{"device", "time", "result", "status"},
            new int[]{R.id.tv_device, R.id.tv_time, R.id.tv_result, R.id.tv_status});

        historyListView.setAdapter(adapter);

        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> item = data.get(position);
            viewModel.selectSession(item.get("sessionId"));
            tabHost.setCurrentTab(2); // 切换到二维码Tab显示详情
        });

        historyListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Map<String, String> item = data.get(position);
            new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("确定要删除这条记录吗？")
                .setPositiveButton("删除", (d, w) -> viewModel.deleteSession(item.get("sessionId")))
                .setNegativeButton("取消", null)
                .show();
            return true;
        });
    }

    private void updateStatistics(TestStatistics stats) {
        if (stats == null) return;

        TextView tvTotalSessions = statisticsLayout.findViewById(R.id.tv_total_sessions);
        TextView tvTotalTests = statisticsLayout.findViewById(R.id.tv_total_tests);
        TextView tvPassRate = statisticsLayout.findViewById(R.id.tv_pass_rate);
        TextView tvPassed = statisticsLayout.findViewById(R.id.tv_pass_count);
        TextView tvFailed = statisticsLayout.findViewById(R.id.tv_fail_count);

        if (tvTotalSessions != null) tvTotalSessions.setText(String.valueOf(stats.getTotalSessions()));
        if (tvTotalTests != null) tvTotalTests.setText(String.valueOf(stats.getTotalTests()));
        if (tvPassRate != null) tvPassRate.setText(String.format("%.1f%%", stats.getPassRate()));
        if (tvPassed != null) tvPassed.setText(String.valueOf(stats.getPassedTests()));
        if (tvFailed != null) tvFailed.setText(String.valueOf(stats.getFailedTests()));
    }

    private void updateQrCode(String data) {
        if (data == null || data.isEmpty()) {
            qrCodeImage.setImageBitmap(null);
            return;
        }

        try {
            // 生成二维码
            int size = 300;
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data,
                BarcodeFormat.QR_CODE, size, size);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pixels[y * size + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
