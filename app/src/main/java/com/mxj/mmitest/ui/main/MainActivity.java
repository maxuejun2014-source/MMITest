package com.mxj.mmitest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.mxj.mmitest.R;
import com.mxj.mmitest.config.TestConfig;
import com.mxj.mmitest.ui.autotest.AutoTestActivity;
import com.mxj.mmitest.ui.base.BaseActivity;
import com.mxj.mmitest.ui.result.ResultActivity;
import com.mxj.mmitest.ui.singletest.SingleTestActivity;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity {

    private TextView tvTestCount;
    private Button btnAutoTest;
    private Button btnSingleTest;
    private Button btnResult;
    private Button btnExit;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void initViews() {
        tvTestCount = findViewById(R.id.tv_test_count);
        btnAutoTest = findViewById(R.id.btn_auto_test);
        btnSingleTest = findViewById(R.id.btn_single_test);
        btnResult = findViewById(R.id.btn_result);
        btnExit = findViewById(R.id.btn_exit);
        tvVersion = findViewById(R.id.tv_version);
    }

    private void setupListeners() {
        btnAutoTest.setOnClickListener(v -> {
            startActivity(new Intent(this, AutoTestActivity.class));
        });

        btnSingleTest.setOnClickListener(v -> {
            startActivity(new Intent(this, SingleTestActivity.class));
        });

        btnResult.setOnClickListener(v -> {
            startActivity(new Intent(this, ResultActivity.class));
        });

        btnExit.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateUI() {
        int totalCount = TestConfig.getTotalTestCount();
        int enabledCount = TestConfig.getDefaultEnabledCount();
        tvTestCount.setText("共 " + enabledCount + "/" + totalCount + " 项测试已启用");

        try {
            String versionName = getPackageManager()
                .getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText("版本 " + versionName);
        } catch (Exception e) {
            tvVersion.setText("版本 1.0.0");
        }
    }
}
