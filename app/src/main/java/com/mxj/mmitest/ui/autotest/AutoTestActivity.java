package com.mxj.mmitest.ui.autotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.mxj.mmitest.R;
import com.mxj.mmitest.config.TestConfig;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动测试界面
 */
public class AutoTestActivity extends BaseActivity {

    private AutoTestViewModel viewModel;
    private TestRepository repository;

    private TextView tvCurrentTest;
    private TextView tvProgress;
    private TextView tvPassed;
    private TextView tvFailed;
    private ProgressBar progressBar;
    private ListView logListView;
    private Button btnStart;
    private Button btnPause;
    private Button btnStop;
    private Button btnConfig;
    private Button btnBack;

    private ActivityResultLauncher<Intent> testResultLauncher;

    // 待执行的测试队列
    private List<TestConfig.TestItem> pendingTests = new ArrayList<>();
    private int pendingIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autotest);

        repository = TestRepository.getInstance(this);
        viewModel = new ViewModelProvider(this).get(AutoTestViewModel.class);
        viewModel.setRepository(repository);
        viewModel.setDeviceId(getDeviceUniqueId());

        testResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    boolean passed = result.getData() != null &&
                        result.getData().getBooleanExtra("passed", false);
                    onTestComplete(passed);
                } else {
                    onTestComplete(false);
                }
            }
        );

        initViews();
        setupListeners();
        observeViewModel();

        // 默认准备所有启用的测试项
        viewModel.prepareTest(TestConfig.getEnabledTestItems());
    }

    private void initViews() {
        tvCurrentTest = findViewById(R.id.tv_current_test);
        tvProgress = findViewById(R.id.tv_progress);
        tvPassed = findViewById(R.id.tv_passed);
        tvFailed = findViewById(R.id.tv_failed);
        progressBar = findViewById(R.id.progress_bar);
        logListView = findViewById(R.id.log_list);
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        btnConfig = findViewById(R.id.btn_config);
        btnBack = findViewById(R.id.btn_back);

        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> {
            TestConfig.TestItem[] items = TestConfig.getEnabledTestItems();
            if (items.length == 0) {
                showMessage("没有可执行的测试项");
                return;
            }
            viewModel.prepareTest(items);
            viewModel.startTest();
            runNextPendingTest();
        });

        btnPause.setOnClickListener(v -> {
            AutoTestViewModel.TestState state = viewModel.getTestState().getValue();
            if (state == AutoTestViewModel.TestState.RUNNING) {
                viewModel.pauseTest();
                btnPause.setText("继续");
            } else if (state == AutoTestViewModel.TestState.PAUSED) {
                viewModel.resumeTest();
                btnPause.setText("暂停");
                runNextPendingTest();
            }
        });

        btnStop.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("停止确认")
                .setMessage("确定要停止自动测试吗？")
                .setPositiveButton("确定", (d, w) -> viewModel.stopTest())
                .setNegativeButton("取消", null)
                .show();
        });

        btnConfig.setOnClickListener(v -> showConfigDialog());

        btnBack.setOnClickListener(v -> {
            AutoTestViewModel.TestState state = viewModel.getTestState().getValue();
            if (state == AutoTestViewModel.TestState.RUNNING ||
                state == AutoTestViewModel.TestState.PAUSED) {
                new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("测试正在进行中，确定要退出吗？")
                    .setPositiveButton("确定", (d, w) -> finish())
                    .setNegativeButton("取消", null)
                    .show();
            } else {
                finish();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getTestState().observe(this, state -> {
            switch (state) {
                case IDLE:
                    btnStart.setEnabled(true);
                    btnPause.setEnabled(false);
                    btnStop.setEnabled(false);
                    btnPause.setText("暂停");
                    break;
                case RUNNING:
                    btnStart.setEnabled(false);
                    btnPause.setEnabled(true);
                    btnStop.setEnabled(true);
                    btnPause.setText("暂停");
                    break;
                case PAUSED:
                    btnStart.setEnabled(false);
                    btnPause.setEnabled(true);
                    btnStop.setEnabled(true);
                    btnPause.setText("继续");
                    break;
                case COMPLETED:
                case STOPPED:
                    btnStart.setEnabled(true);
                    btnPause.setEnabled(false);
                    btnStop.setEnabled(false);
                    showCompletionDialog();
                    break;
            }
        });

        viewModel.getCurrentTestName().observe(this, name -> {
            tvCurrentTest.setText("当前: " + name);
        });

        viewModel.getProgress().observe(this, progress -> {
            Integer total = viewModel.getTotalCount().getValue();
            if (total != null && total > 0) {
                tvProgress.setText(progress + "/" + total);
                progressBar.setMax(total);
                progressBar.setProgress(progress);
            }
        });

        viewModel.getPassedCount().observe(this, count -> {
            tvPassed.setText("通过: " + count);
        });

        viewModel.getFailedCount().observe(this, count -> {
            tvFailed.setText("失败: " + count);
        });

        viewModel.getLogs().observe(this, logList -> {
            if (logList != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, logList);
                logListView.setAdapter(adapter);
                logListView.setSelection(logList.size() - 1);
            }
        });
    }

    private void runNextPendingTest() {
        TestConfig.TestItem item = viewModel.getCurrentTest();
        if (item == null) return;

        Intent intent = new Intent(this, item.getActivityClass());
        testResultLauncher.launch(intent);
    }

    private void onTestComplete(boolean passed) {
        TestConfig.TestItem item = viewModel.getCurrentTest();
        if (item != null) {
            viewModel.onTestResult(item.getId(), passed);
        }

        // 如果还在运行，自动执行下一项
        AutoTestViewModel.TestState state = viewModel.getTestState().getValue();
        if (state == AutoTestViewModel.TestState.RUNNING) {
            runNextPendingTest();
        }
    }

    private void showConfigDialog() {
        TestConfig.TestItem[] allItems = TestConfig.TEST_ITEMS;
        boolean[] enabledItems = new boolean[allItems.length];
        TestConfig.TestItem[] enabled = TestConfig.getEnabledTestItems();

        for (int i = 0; i < allItems.length; i++) {
            enabledItems[i] = false;
            for (TestConfig.TestItem item : enabled) {
                if (item.getId() == allItems[i].getId()) {
                    enabledItems[i] = true;
                    break;
                }
            }
        }

        String[] names = new String[allItems.length];
        for (int i = 0; i < allItems.length; i++) {
            names[i] = allItems[i].getName();
        }

        new AlertDialog.Builder(this)
            .setTitle("选择测试项")
            .setMultiChoiceItems(names, enabledItems, (d, which, isChecked) -> {
                enabledItems[which] = isChecked;
            })
            .setPositiveButton("确定", (d, w) -> {
                pendingTests.clear();
                for (int i = 0; i < allItems.length; i++) {
                    if (enabledItems[i]) {
                        pendingTests.add(allItems[i]);
                    }
                }
                viewModel.prepareTest(pendingTests.toArray(new TestConfig.TestItem[0]));
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showCompletionDialog() {
        Integer passed = viewModel.getPassedCount().getValue();
        Integer failed = viewModel.getFailedCount().getValue();

        String message = "自动测试已完成！\n\n";
        message += "通过: " + passed + " 项\n";
        message += "失败: " + failed + " 项";

        new AlertDialog.Builder(this)
            .setTitle("测试完成")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .setNeutralButton("查看结果", (d, w) -> {
                // 可以跳转到结果页面
            })
            .show();
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show();
    }

    private String getDeviceUniqueId() {
        return Settings.Secure.getString(
            getContentResolver(),
            Settings.Secure.ANDROID_ID
        );
    }
}
