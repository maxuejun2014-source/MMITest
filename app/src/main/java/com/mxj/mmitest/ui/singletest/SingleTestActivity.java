package com.mxj.mmitest.ui.singletest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.mxj.mmitest.R;
import com.mxj.mmitest.config.TestConfig;
import com.mxj.mmitest.data.local.TestHistoryStore;
import com.mxj.mmitest.data.repository.TestRepository;
import com.mxj.mmitest.ui.base.BaseActivity;
import com.mxj.mmitest.ui.testitems.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 单项测试选择界面
 */
public class SingleTestActivity extends BaseActivity {

    private ListView listView;
    private TestItemAdapter adapter;
    private TestRepository repository;
    private TestConfig.TestItem[] testItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_test);

        repository = TestRepository.getInstance(this);
        testItems = TestConfig.getEnabledTestItems();

        initViews();
        loadTestStatuses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTestStatuses();
    }

    private void initViews() {
        listView = findViewById(R.id.list_view);
        adapter = new TestItemAdapter();
        listView.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTestStatuses() {
        Map<Integer, TestHistoryStore.TestStatus> statuses = new HashMap<>();
        for (TestConfig.TestItem item : testItems) {
            TestHistoryStore.TestStatus status = repository.getSingleTestStatusSync(item.getId());
            statuses.put(item.getId(), status);
        }
        adapter.setStatuses(statuses);
    }

    private void startTest(TestConfig.TestItem item) {
        Intent intent = new Intent(this, item.getActivityClass());
        startActivity(intent);
    }

    private class TestItemAdapter extends BaseAdapter {
        private Map<Integer, TestHistoryStore.TestStatus> statuses = new HashMap<>();

        public void setStatuses(Map<Integer, TestHistoryStore.TestStatus> statuses) {
            this.statuses = statuses;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return testItems.length;
        }

        @Override
        public TestConfig.TestItem getItem(int position) {
            return testItems[position];
        }

        @Override
        public long getItemId(int position) {
            return testItems[position].getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_test, parent, false);
            }

            TestConfig.TestItem item = getItem(position);
            TextView tvName = convertView.findViewById(R.id.test_name);
            TextView tvStatus = convertView.findViewById(R.id.test_status);

            tvName.setText(item.getName());

            TestHistoryStore.TestStatus status = statuses.get(item.getId());
            if (status != null && status.result != null) {
                if (status.isPassed()) {
                    tvStatus.setText("✅ PASS");
                    tvStatus.setTextColor(0xFF4CAF50);
                } else if (status.isFailed()) {
                    tvStatus.setText("❌ FAIL");
                    tvStatus.setTextColor(0xFFF44336);
                } else {
                    tvStatus.setText("⚪ " + status.result);
                    tvStatus.setTextColor(0xFF9E9E9E);
                }
            } else {
                tvStatus.setText("⚪ 未测试");
                tvStatus.setTextColor(0xFF9E9E9E);
            }

            convertView.setOnClickListener(v -> startTest(item));

            return convertView;
        }
    }
}
